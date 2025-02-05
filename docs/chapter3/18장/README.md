# 18장 핫 데이터 스스, 콜드 데이터 소스

### 📌 Hot vs Cold

- Hot 데이터 스트림은 데이터를 소비하는 것과 무관하게 원소를 생성하지만, Cold 데이터 스트림은 요청이 있을 때만 작업을 수행하며 아무것도 저장하지 않는다.
- buildList (Hot), Sequece (Cold)를 통해 비교하면 L만 출력된다.

```kotlin
fun main() {
    val l = buildList {
        repeat(3) {
            add("User$it")
            println("L: Added User")
        }
    }

    l.map {
        println("L: Processing")
        "Processed $it"
    }

    val s = sequence {
        repeat(3) {
            yield("User$it")
            println("S: Added User")
        }
    }

    s.map {
        println("S: Processing")
        "Processed $it"
    }
}
```

- Cold 데이터 스트림
    - 무한할 수 있다.
    - 최소한의 연산만 수행한다.
    - 메모리를 적게 사용한다.
- Hot 데이터 스트림
    - 항상 사용 가능한 생태이다.
    - 여러 번 사용되었을 때 매번 결과를 다시 계산할 필요가 없다.
- Sequence는 원소를 지연 처리하기 때문에 더 적은 연산을 수행한다. 따라서 일반적인 List (Hot)과 Sequece에서의 중간 연산자를 수행할때는 차이점을 잘 알고 실행해야한다.

### 📌 Hot 채널, Cold 플로우

- Flow를 생성하는 가장 일반적인 방법은 produce 함수와 비슷한 형태의 빌더인 `flow` 를 사용하는 것이다.

```kotlin
val flow = flow { 
    while (true) {
        emit(1)
    }
}
```

- 채널과 플로우 빌더에서는 많은 차이가 있다. 채널은 Hot으로 값을 곧바로 계산한다. 별도의 코루틴에서 계산을 수행하여 produce는 CoroutineScope의 확장 함수로 정의되어 있는 코루틴 빌더가 되어야 한다.
- 채널은 Hot 데이터 스트림이기 때문에 소비되는 것과 상관 없이 값을 생성한 뒤에 가지게 된다. 각 원소는 단 한 번만 받을 수 있기 때문에, 첫 번째 수신자가 모든 원소를 소비하고 나면 두번째 소비자는 채널이 비어 있으며 어떤 원소도 받을 수가 없다.

```kotlin
private fun CoroutineScope.makeChannel() = produce {
    println("Channel started")
    (1..3).forEach {
        delay(1000)
        send(it)
    }
}

suspend fun main() = coroutineScope {
    val channel = makeChannel()

    delay(1000)
    println("Calling channel...")
    for (value in channel) {
        println(value)
    }
    println("Consuming again...")
    for (value in channel) {
        println(value)
    }
}
Channel started
Calling channel...
1
2
3
Consuming again...
```

- Flow는 Cold 데이터 소스이기 때문에 값이 필요할 때만 생성한다. 따라서 flow는 빌더가 아니면 어떤 처리도 하지 않는다. flow는 최종 연산자 (ex. collect) 가 호출될 때 원소가 어떻게 생성되어야 하는지 정의한 것에 불과하다.

```kotlin
private fun makeFlow() = flow {
    println("Flow started")
    (1..3).forEach {
        delay(1000)
        emit(it)
    }
}

suspend fun main() = coroutineScope {
    val flow = makeFlow()

    delay(1000)
    println("Calling flow...")
    flow.collect { println(it) }
    println("Consuming again...")
    flow.collect { println(it) }
}
Calling flow...
Flow started
1
2
3
Consuming again...
Flow started
1
2
3
```