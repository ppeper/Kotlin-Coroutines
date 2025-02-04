# 17장 셀렉트

- 코루틴은 가장 먼저 완료되는 코루틴의 결과를 기다리는 select 함수를 제공한다. 이는 많이 필요로하지는 않지만 개념을 파악해보자.

### 📌 지연되는 값 선택하기

- 여러 개의소스에 데이터를 요청한 뒤, 가장 빠른 응답만 얻는 경우를 생각해 보자.  가장 쉬운 방법으로 요청을 여러 개의 비동기 프로세스로 시작한 뒤, select 함수를 표현식으로 사용하여 내부에서 값을 기다리는 것이다.
- select 내부에서는 셀렉트 표현식에서 나올 수 있는 결과값을 명시하는 Deferred 값의 onWait 함수를 호출한다.

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select

suspend fun requestData1(): String {
    delay(100000)
    return "Data 1"
}

suspend fun requestData2(): String {
    delay(1000)
    return "Data 2"
}

val scope = CoroutineScope(SupervisorJob())

suspend fun asMultipleForData(): String {
    val deferredData1 = scope.async { requestData1() }
    val deferredData2 = scope.async { requestData2() }
    return select {
        deferredData1.onAwait { it }
        deferredData2.onAwait { it }
    }
}

suspend fun main() = coroutineScope {
    println(asMultipleForData())
}
// (1초 후)
// Data 2
```

- 위 예시에서 외부의 스코프로부터 async가 시작한다. 따라서 asMultipleForData 를 시작하는 코루틴을 취소하면, 외부의 스코프인 비동기 테스크는 취소가 되지 않는다. coroutineScope를 사용하면 자식 코루틴도 기다리게 되며, 예제에서 1초가 아닌 100초를 기다리게 된다. 따라서 select가 값을 생성하고 나서 also 스코프 함수를 호출하여다른 코루틴을 취소하도록 하여 해결할 수 있다.

```kotlin
suspend fun asMultipleForData(): String = coroutineScope {
    select {
        async { requestData1() }.onAwait { it }
        async { requestData2() }.onAwait { it }
    }.also { coroutineContext.cancelChildren() }
}
```

### 📌 채널에서 값 선택하기

- 채널에서 셀럭트 표현식에서 사용하는 주요 함수는 다음과 같다.
    - `onReceive`: 채널이 값을 가지고 있을 때 선택된다. 선택되었을 때, select는 람다식의 결괏값을 반환한다.
    - `onReceiveCatching`: 채널이 값을 가지고 있거나 닫혔을 때 선택된다. 값을 나타내거나 채널이 닫혔다는 걸 알려 주는 ChannelResult를 받으며, 이 값을 람다식의 인자로 사용한다.  선택되었을 때, select는 람다식의 결괏값을 반환한다.
    - `onSend`: 채널의 버퍼에 공간이 있을 때 선택된다.  선택되었을 때, select는 Unit을 반환한다.

```kotlin
fun CoroutineScope.produceString(
    string: String,
    time: Long
) = produce {
    while (true) {
        delay(time)
        send(string)
    }
}

fun main() = runBlocking {
    val fooChannel = produceString("foo", 210L)
    val barChannel = produceString("bar", 500L)

    repeat(7) {
        select {
            fooChannel.onReceive {
                println("From fooChannel: $it")
            }
            barChannel.onReceive {
                println("From barChannel: $it")
            }
        }
    }
    coroutineContext.cancelChildren()
}

From fooChannel: foo
From fooChannel: foo
From barChannel: bar
From fooChannel: foo
From fooChannel: foo
From barChannel: bar
From fooChannel: foo
```