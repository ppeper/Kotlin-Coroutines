# 3장 중단은 어떻게 작동할까?

> 💡기본적으로 코루틴은 중단시 Continuation 객체를 반환한다. 이 객체는 게임을 하다 저장을 하는 것과 비슷하다고 보면 된다. 따라서 게임과 같이 Continuation을 이용하면 멈췄던 곳에서 다시 코루틴을 실행 할 수 있다.

### 📌 suspendCoroutine

- Continuation 객체를 인자로 받아 중단점을 알 수 있도록 한다.
- 코루틴을 중단시키는 함수로, 람다를 넘겨주면 로루틴이 중단되기 이전에 실행된다.
    - suspendCoroutine이 호출된 뒤에는 이미 중단되어 Continuation 객체를 사용할 수 없기 때문에, 람다 표현식의 인자로 들어가 중단되기 이전에 실행되는 것.

```kotlin
suspend fun main() {
    println("Before")
    suspendCoroutine<Unit> { continuation ->
        println("Suspended")
        continuation.resume(Unit)
    }
    println("After")
}

// 출력 결과:
// Before
// Suspended
// After
```

### 📌 Continuation

> 💡Kotlin 1.3 이후로 Continutation 클래스의 형태가 달라졌다.
> 
> 기존에는 resume, resumeWithException을 사용했지만, 현재는 Result를 반환하는 resumeWith 함수 하나로 확장 함수의 형태로 변경되었다.

```kotlin
@SinceKotlin("1.3")
@InlineOnly
public inline fun <T> Continuation<T>.resume(value: T): Unit =
    resumeWith(Result.success(value))

/**
 * Resumes the execution of the corresponding coroutine so that the [exception] is re-thrown right after the
 * last suspension point.
 */
@SinceKotlin("1.3")
@InlineOnly
public inline fun <T> Continuation<T>.resumeWithException(exception: Throwable): Unit =
    resumeWith(Result.failure(exception))
```

- 스레드를 사용하여  잠시 정지(sleep) 이후 코루틴을 다시 재개하는 함수를 만들 수 있지만 그렇게 효율 적이지 않다.
    - JVM에서 제공하는 ScheduledExecutorService 를 사용하여 “알람 시계” 를 만들어 정해진 시간이 지나면 continuation.resume()을 호출하도록 알람을 설정할 수 있다.

```kotlin
private val executor =
    Executors.newSingleThreadScheduledExecutor {
        Thread(it, "scheduler").apply { isDaemon = true }
    }
    
suspend fun delay(timeMillis: Long) =
    suspendCoroutine { cont ->
        executor.schedule({
            cont.resume(Unit)
        }, timeMillis, TimeUnit.MILLISECONDS)
    }

suspend fun main() {
    println("Before")
		
    delay(1000L)
		
    println("After")
}
    
// Before
// (1초 후)
// After
```

- 여기서 delay 함수는 코틀린 라이브러리에서 제공하는 delay의 구현 방식이랑 정확히 일치한다. 현재 delay의 구현은 테스트를 지원하기 위해 많이 바뀌었지만 핵심 로직은 동일하다고 한다.

### 📌 값으로 재개하기

- Continuation의 제네릭 타입의 인자로 resume 함수도 동일하게 지정해 주어야 한다.
- API를 호출해 네트워크 응답을 기자리는 것처럼 특정 데이터를 기다리고 중단하는 상황은 자주 발생한다.
    - 코루틴이 없다면 스레드는 응답을 기다리고 있을 수 밖에 없다. (자원 낭비)
    - 코루틴이 있으면 “데이터를 받으면, 해당 결과 값을 resume 함수를 통해 보내줘” 라고 Continuation 객체를 통해 전달 할 수 있고, 스레드는 다른 일을 할 수 있는 장점이 생긴다.

```kotlin
// 외부에서 구현된 requestUser 콜백 함수가 있다고 하자
fun requestUser(callback: (User) -> Unit) {
    callback(User(name = "Text"))
}

suspend fun main() {
    println("Before")
    val user = suspendCoroutine { continuation ->
        requestUser { user ->
            continuation.resume(user)
        }
    }
    println(user)
    println("After")
} 
 
// suspendCoroutine 직접 호출하지 않고 중단 함수를 호출하자.
suspend fun requestUser(): User {
    return suspendCoroutine { continuation ->
        requestUser { user ->
            continuation.resume(user)
        }
    }
}
    
suspend fun main() {
    println("Before")
    val user = requestUser()
    println(user)
    println("After")
}  
```

### 📌 예외로 재개하기

- 우리가 사용하는 모든 함수를 값을 반환 또는 예외를 던진다.
- resume이 호출될 때 suspendCoroutine 또한 인자로 들어온 데이터를 반환한다. resumeWithException이 호출되면 중단된 지점에서 인자로 넣어준 예외를 던진다.
    - 이러한 방법은 네트워크 관련 예외 등을 알릴 때 사용 할 수 있다.

```kotlin
class MyException: Throwable("Just an Exception")

suspend fun main() {
    try {
        suspendCoroutine<Unit> {  continuation ->
            continuation.resumeWithException(MyException())
        }
    } catch (e: MyException) {
        println("Caught!")
    }
}

// Caught!
```