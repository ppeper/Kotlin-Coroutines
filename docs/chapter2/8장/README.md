# 9장 취소

### 📌 기본적인 취소

- Job 인터페이스는 취소하게 하는 `cancel` 메서드를 가지고 있다.
    - 호출한 코루틴은 첫 번째 중단점에서 Job을 끝낸다.
    - Job이 자식을 가지고 있다면, 그들 또한 취소된다. 하지만 부모는 영향을 받지 않는다. (아래로(자식)만 전파됨)
    - Job이 취소되면, 취소된 Job은 새로운 코루틴의 부모로 사용될 수 없다.
- kotlinx.coroutines 라이브러리는 cancel과 join을 함께 호출할 수 있는 `cancelAndJoin` 이라는 편리한 확장함수를 제공한다.

```kotlin

public suspend fun Job.cancelAndJoin() {
    cancel()
    return join()
}

suspend fun main() = coroutineScope { 
    val job = launch {
        repeat(1000) {
            delay(200)
            println("Printing $it")
        }
    }
    delay(1100)
    job.run {
        cancel()
        join()
    }
    // job.cancelAndJoin()
    println("Cancelled successfully")
}
// Printing 0
// Printing 1
// Printing 2
// Printing 3
// Printing 4
// Cancelled successfully
```

- cancel 함수에 각기 다른 예외를 인자로 넣어 사용하면 취소된 원인을 명확하게 할 수 있다.
- 코루틴을 취소하기 위해서 사용되는 예외는 CancellationException이어야 하기 때문에 인자로 사용되는 예외 또한 CancellationException의 서브 타입이어야한다.
- calcel이 호출된 뒤 다음 작업을 진행하기 전에 취소 과정이 완료되는 걸 기다리기 위해서는 join을 사용하는 것이 일반적이다. 따라서 join을 호출하지 않으면 `경쟁 상태(race condition)` 가 될 수 도 있다.

```kotlin
suspend fun main() = coroutineScope {
    val job = launch {
        repeat(1000) {
            delay(100)
            Thread.sleep(100)
            println("Printing $it")
        }
    }
    delay(1000)
    job.cancel()
    println("Cancelled successfully")
}
// Printing 0
// Printing 1
// Printing 2
// Printing 3
// Cancelled successfully
// Printing 4
```

- 한꺼번에 취소하는 기능은 유용하다. 안드로이드로 예를 들면 View에서 나갔을때 시작된 모든 코루틴을 취소하는 경우가 있을 수 있다.
- 과거엔 ViewModel의 `onClear()` 함수에서 취소를 해주었지만 `viewModelScope` 가 생겨 편하게 사용할 수 있게 되었다.

```kotlin
 class TestViewModel: ViewModel() {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ...

    override fun onCleared() {
        scope.coroutineContext.cancelChildren()
    }
}
```

### 📌 취소는 어떻게 작동하는가?

- Job이 취소되면 `Cancelling` 상태로 바뀐다. 상태가 바뀐 뒤 첫 번째 중단점에서 CancellationException 예외를 던지게 되는데, try-catch 구문으로 잡을 수 도 있지만 다시 던지는 것이 좋다.
- 취소된 코루틴은 내부적으로 예외를 사용해 취소되기 때문에 finally 블록 안에서 모든 것을 정리할 수 있다. (ex. DB의 연결)

```kotlin
suspend fun main() = coroutineScope {
    val job = Job()
    launch(job) {
        try {
            repeat(1000) {
                delay(200)
                println("Printing $it")
            }
        } catch (e: CancellationException) {
            println(e)
            throw e
        } finally {
            println("Will always printed")
        }
    }
    delay(1100)
    job.cancelAndJoin()
    println("Cancelled success")
    delay(1000)
}
// Printing 0
// Printing 1
// Printing 2
// Printing 3
// Printing 4
// kotlinx.coroutines.JobCancellationException: Job was cancelled; job=JobImpl{Cancelling}@1686b41d
// Will always printed
// Cancelled success
```

### 📌 취소 중 코루틴 한 번 더 호출하기

- Job이 이미 `Cancelling` 상태가 되면 중단되거나 다른 코루틴을 시작하는 건 절대 불가능하다.
- 다른 코루틴을 시작하려고 하면 그냥 무시해버린다. 중단하려고 하면 CancellationException을 던진다.

```kotlin
suspend fun main() = coroutineScope {
    val job = Job()
    launch(job) {
        try {
            delay(2000)
            println("Job is done")
        } finally {
            println("Finally")
            launch { // 무시
                println("Will not be printed")
            }
        }
    }
    delay(1000) // 여기서 에러가 발생
    job.cancelAndJoin()
    println("Cancelled done")
}
// Finally
// Cancelled done
```

- 가끔 코루틴이 이미 취소되었을 때 중단 함수를 반드시 호출해야하는 경우들이 있다. (ex. DB 변경 사항을록백하는 경우)
- 이런 경우 함수 콜을 `withContext(NonCancellable)` 로 포장하는 방법이 사용되고 있다.

### 📌 invokeOnCompletion

- 자원을 해제하는 데 자주 사용되는 또 다른 방법은 invokeOnCompletion 메서드를 호출하는 것이다.
- invokeOnCompletion 메서드는 Job이 `Completed`, `Cancelled` 화 같이 마지막 상태에 도달했을 때 호출된 핸들러를 지정하는 역할을 한다.
    - 핸들러의 파라미터 중 하나인 예외의 종류는 아래와 같다.
        - Job이 예외 없이 끝나면 null이 된다.
        - 코루틴이 취소되었으면 CancellationException이 된다.
        - 코루틴을 종료시킨 예외일 수 있다.

```kotlin
@InternalCoroutinesApi
public fun invokeOnCompletion(
    onCancelling: Boolean = false,
    invokeImmediately: Boolean = true,
    handler: CompletionHandler): DisposableHandle
    
suspend fun main() = coroutineScope {
    val job = launch {
        delay(1000)
    }
    job.invokeOnCompletion { cause: Throwable? ->
        println("Finished")
    }
    delay(400)
    job.cancelAndJoin()
}
```

- invkoeOnCompletion은 취소하는 중에 동기적으로 호출되며, 파라미ㅓ를 사용하면 핸들러의 동작 방식을 변경할 수도 있다.

### 📌 중단될 수 없는 걸 중단하기

- 취소는 중단점에서 일어나기 때문에 중단점이 없으면 취소를 할 수 없다.
- 이를 대채하기 위해서는 몇 가지 방버이 있다.
    1. `yield()` 를 주기적으로 호출하는 것
        - 이 함수는 코루틴을 중단하고 즉시 재실행한다.
        - 중단 가능하지 않으면서 CPU 집약적이거나 시간 집약적인 연산들이 중단 함수에 있다면 사용하는 것이 좋다.

    ```kotlin
    suspend fun cpuIntensiveOperation() = 
        withContext(Dispatchers.Default) {
            cpuIntensiveOperation1()
            yield()
            cpuIntensiveOperation2()
            yield()
            cpuIntensiveOperation3()
        }
    ```

    1. Job의 상태를 추적하는 것
        - `isActive`프로퍼티를 사용해 Job이 액티브한지 확인할 수 있고 액티브하지 않을 때는 연산을 중단할 수 있다.

        ```kotlin
        launch {
            do {
                // 작업들
            } while (isActive)
        }
        ```

        - 또는 `ensureActive` 함수를 사용할 수 있다.

        ```kotlin
        launch {
            // 작업들
            ensureActive()
        }
        ```


### 📌 suspendCancellableCoroutine

- 코루틴이 취소되었을 때 행동을 정의하는 데 사용하는 invokeOnCancellation 메서드 이다.

```kotlin
suspend fun someTask() = suspendCancellableCoroutine { cont ->
    cont.invokeOnCancellation { 
        // 정리 작업을 수행한다.
    }
    // 나머지 구현 부분이다.
}
```