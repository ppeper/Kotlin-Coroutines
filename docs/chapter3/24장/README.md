# 24장 공유플로우와 상태플로우

- 일반적으로 플로우는 `콜드 데이터`이기 떄문에 요청할 때마다 값이 계산된다.
- 하지만 여러 개의 수신자가 하나의 데이터가 변경되는지 감지하는 경우가 있다. 이럴때 메일링 리스트와 비슷한 개념인 공유플로우를 사용한다. 상태플로우는 감지 가능한 값과 비슷하다.

### 📌 SharedFlow

- 브로드캐스트 채널과 비슷한 MutableStateFlow를 메시지를 보내면 대기하고 있는 모든 코루틴이 수신하게 된다.

```kotlin
suspend fun main(): Unit = coroutineScope {
    val mutableSharedFlow = MutableSharedFlow<String>()

    launch {
        mutableSharedFlow.collect {
            println("#1 received $it")
        }
    }
    launch {
        mutableSharedFlow.collect {
            println("#2 received $it")
        }
    }
    delay(1000)
    mutableSharedFlow.emit("Message1")
    mutableSharedFlow.emit("Message2")
}
// #1 received Message1
// #2 received Message1
// #1 received Message2
// #2 received Message2
// 프로그램은 꺼지지 않는다
```

> 💡 위 프로그램은 coroutineScope의 자식 코루틴이 시작한 launch로 시작된후 공유플로우를 감지하고 있는 상태라 종료되지 않는다.

- MutableSharedFlow는 메시지 보내는 작업을 유지할 수도 있다. (기본값은 0) `replay` 인자를 설정하면 마지막으로 전송한 값들이 정해진 수만큼 저장된다.
- `resetReplayCache` 를 사용하면 값을 저장한 캐시를 초기화할 수 있다.

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun main(): Unit = coroutineScope {
    val mutableSharedFlow = MutableSharedFlow<String>(replay = 2)
    repeat(3) {
        mutableSharedFlow.emit("Message${it + 1}")
    }
    println(mutableSharedFlow.replayCache)
    // [Message2, Message3]

    launch {
        mutableSharedFlow.collect {
            println("#1 received $it")
        }
    }
    // #1 received Message2
    // #1 received Message3

    delay(100)
    mutableSharedFlow.resetReplayCache()
    println(mutableSharedFlow.replayCache)
    // []
}
```

- 코틀린에서는 감지만 하는 인터페이스, 변경하는 인터페이스를 구분하는 것이 관행이다. 앞에서 SendChannel, ReceiveChannel, Channel로 구분 한것을 예시로 볼 수 있다.
- MutableSharedFlow는 SharedFlow와 FlowCollector 모두는 상속한다. SharedFlow는 Flow를 상속하고 감지하는 목적으로 사용되며, FlowCollector는 값을 내보내는 목적으로 사용된다.
- SharedFlow와 FlowCollector 인터페이스는 값을 내보내거나 또는 수집하는 함수만 노출하기 위해 자주 사용된다.

```kotlin
suspend fun main(): Unit = coroutineScope {
    val mutableSharedFlow = MutableSharedFlow<String>()
    val sharedFlow: SharedFlow<String> = mutableSharedFlow
    val collector: FlowCollector<String> = mutableSharedFlow

    launch {
        mutableSharedFlow.collect {
            println("#1 received $it")
        }
    }
    launch {
        sharedFlow.collect {
            println("#2 received $it")
        }
    }

    delay(1000)
    mutableSharedFlow.emit("Message1")
    collector.emit("Message2")
}
// (1초후)
// #2 received Message1
// #1 received Message1
// #2 received Message2
// #1 received Message2
```

### 📌 sharedIn

- 플로우는 사용자 액션, 데이터베이스 변경, 또는 새로운 메시지와 같은 변화를 감지할 때 주로 사용한다.
- 다양한 클래스가 변화를 감지하는 상황에서 하나의 플로우로 여러 개의 플로우를 만들고 싶다면, Flow를 SharedFlow로 바꾸는 쉬운 방법으로 `shareIn` 함수를 사용하는 것이다.

```kotlin
suspend fun main(): Unit = coroutineScope {
    val flow = flowOf("A", "B", "C")
        .onEach { delay(1000) }

    val sharedFlow = flow.shareIn(
        scope = this,
        started = SharingStarted.Eagerly
    )
    delay(500)
    launch { sharedFlow.collect { println("#1 $it") } }
    delay(1000)
    launch { sharedFlow.collect { println("#2 $it") } }
    delay(1000)
    launch { sharedFlow.collect { println("#3 $it") } }
}
// (1초후)
// #1 A
// (1초후)
// #2 B
// #1 B
// (1초후)
// #1 C
// #2 C
// #3 C
```

- sharedIn 함수는 3개의 인자가 있다.
    - `scope`: 코루틴 스코프를 받는다. (안드로이드에선 대부분 ViewModel에서 사용하게되면 viewModelScope를 사용한다.)
    - `started`: 리스너의 수에 따라 값을 언제부터 감지할지 결정한다.
        - SharingStarted.Eagerly
            - 즉시 값을 감지하기 시작하고 플로우로 값을 전송한다. replay 값에 제한이 있고 감지를 시작하기 전에 값이 나오면 일부를 유실할 수 있다. (Hot Flow이기 때문)
            - (만약 replay가 0이라면 먼저 들어온 값이 전부 유실된다.)

            ```kotlin
            suspend fun main(): Unit = coroutineScope {
                val flow = flowOf("A", "B", "C")
            
                val sharedFlow = flow.shareIn(
                    scope = this,
                    started = SharingStarted.Eagerly
                )
                delay(100)
                launch {
                    sharedFlow.collect {
                        println("#1 $it")
                    }
                }
                println("Done!")
            }
            // Done!
            // (모두 유실됨)
            ```

        - SharingStarted.Lazily
            - 첫 번째 구독자가 나올 때 감지하기 시작한다. 첫 번째 구독자는 내보내진 모든 값을 수신하는 것이 보장되며, 이후의 구독자는 replay 수만큼 가장 최근에 저장된 값을 받게 된다.

            ```kotlin
            suspend fun main(): Unit = coroutineScope {
                val flow1 = flowOf("A", "B", "C")
                val flow2 = flowOf("D")
                    .onEach { delay(1000) }
            
                val sharedFlow = merge(flow1, flow2).shareIn(
                    scope = this,
                    started = SharingStarted.Lazily,
                )
                delay(100)
                launch {
                    sharedFlow.collect {
                        println("#1 $it")
                    }
                }
                delay(1000)
                launch {
                    sharedFlow.collect {
                        println("#2 $it")
                    }
                }
            }
            // (0.1초후)
            // #1 A
            // #1 B
            // #1 C
            // (1초후)
            // #1 D
            // #2 D
            ```

        - SharingStarted.WhileSubscribed
            - 첫 번째 구독자가 나올 때 감지하기 시작하며, 마지막 구독자가 사라지면 플로우도 멈춘다. SharedFlow가 멈췄을 때 새로운 구독자가 나오면 플로우가 다시 시작된다.

            ```kotlin
            suspend fun main(): Unit = coroutineScope {
                val flow = flowOf("A", "B", "C", "D")
                    .onStart { println("Started") }
                    .onCompletion { println("Finished") }
                    .onEach { delay(1000) }
            
                val sharedFlow = flow.shareIn(
                    scope = this,
                    started = SharingStarted.WhileSubscribed(),
                )
                delay(3000)
                launch {
                    println("#1 ${sharedFlow.first()}")
                }
                launch {
                    println("#2 ${sharedFlow.take(2).toList()}")
                }
                delay(3000)
                launch {
                    println("#3 ${sharedFlow.first()}")
                }
            }
            // (3초후)
            // Started
            // (1초후)
            // #1 A
            // (1초후)
            // #2 [A, B]
            // Finished
            // (1초후)
            // Started
            // (1초후)
            // #3 A
            // Finished
            
            ```

        - SharingStarted 인터페이스를 구현하여 커스텀화된 전략을 정의하는 것도 가능하다.
    - `replay`: 기본값이 0

### 📌 StateFlow

- 상태플로우는 공유플로우의 개념을 확장시킨 것으로 replay 인자 값이 1인 공유플로우와 비슷하게 작동한다. 상태플로우는 value 프로퍼티로 접근 가능한 값 하나를 항상 가지고 있다.

```kotlin
suspend fun main(): Unit = coroutineScope {
    val state = MutableStateFlow("A")
    println(state.value)
    launch {
        state.collect {
            println("Value Changed to $it")
        }
    }
    delay(1000)
    state.value = "B"
    delay(1000)
    launch {
        state.collect {
            println("And now it is $it")
        }
    }
    delay(1000)
    state.value = "C"
}
// A
// Value Changed to A
// Value Changed to B
// And now it is B
// And now it is C
// Value Changed to C
```

- StateFlow는 안드로이드에서 LiveData를 대체하는 방식으로 사용되고 있다. 코루틴을 완벽하게 지원하고, 값을 가지고 있기 때문에 Null일 필요가 없다.
- 상태플로우는 데이터가 덮어 씌워지기 떄문에, 관찰이 느린 경우 상태의 중간 변화를 받을 수 없는 경우도 있다. 따라서 모든 이벤트를 다 받으려면 공유플로우를 사용해야 한다.

```kotlin
suspend fun main(): Unit = coroutineScope {
    val state = MutableStateFlow('C')
    launch {
        for (c in 'A'..'E') {
            delay(300)
            state.value = c
        }
    }
    state.collect {
        delay(1000)
        println(it)
    }
}
// C
// D
// E
```

### 📌 stateIn

- stateIn은 Flow<T>를 StateFlow<T>로 변환하는 함수이다. 스코프에서만 호출가능하지만 중단 함수이기도 하다. StateFlow는 항상 값을 가져야 한다. 따라서 값을 명시하지 않았을 때는 첫 번째 값이 계산될 때까지 기다려야한다.

```kotlin
suspend fun main(): Unit = coroutineScope {
    val flow = flowOf("A", "B", "C")
        .onEach { delay(1000) }
        .onEach { println("Produced $it") }

    val stateFlow = flow.stateIn(this)
    println("Listening")
    println(stateFlow.value)
    stateFlow.collect {
        println("Received $it")
    }
}
// (1초후)
// Produced A
// Listening
// A
// Received A
// (1초후)
// Produced B
// Received B
// (1초후)
// Produced C
// Received C
```

- 두 번째 형태는 중단 함수가 아니지만 초기 값과 started 모드를 지정해야 한다.

```kotlin
suspend fun main(): Unit = coroutineScope {
    val flow = flowOf("A", "B")
        .onEach { delay(1000) }
        .onEach { println("Produced $it") }

    val stateFlow = flow.stateIn(
        scope = this,
        started = SharingStarted.Lazily,
        initialValue = "Empty"
    )
    println(stateFlow.value)
    delay(2000)
    stateFlow.collect {
        println("Received $it")
    }
}
// Empty
// (2초후)
// Received Empty
// (1초후)
// Produced A
// Received A
// (1초후)
// Produced B
// Received B
```