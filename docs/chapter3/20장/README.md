# 20장 플로우의 실제 구현

### 📌 Flow 이해하기

- 간단한 람다식에 suspend을 만들어 보자. 람다식은 순차적으로 호출되기 때문에, 이전 호출이 완료되기 전에 같은 람다식을 추가적으로 호출할 수 없다.

```kotlin
suspend fun main() {
    val f: suspend () -> Unit = {
        println("A")
        delay(1000)
        println("B")
        delay(1000)
        println("C")
    }
    f()
    f()
}
// A
// (1초후)
// B
// (1초후)
// C
// A
// (1초후)
// B
// (1초후)
// C
```

- 람다식은 함수를 나타내는 파라미터를 가질 수 있다. 이 파라미터를 emit이라고 해보자.

```kotlin
suspend fun main() {
    val f: suspend ((String) -> Unit) -> Unit = { emit ->
        emit("A")
        emit("B")
        emit("C")
    }
    f { println(it) }
    f { println(it) }
}
```

- 이때 emit은 중단 함수가 되어야 한다. 함수형이 많이 복잡해진 상태로 emit이라는 추상 메서드를 가진 FlowCollector 함수형 인터페이스를 정의하여 간단하게 만들어 보자

```kotlin
fun interface FlowCollector {
    suspend fun emit(value: String)
}

suspend fun main() {
    val f: suspend (FlowCollector) -> Unit = {
        it.emit("A")
        it.emit("B")
        it.emit("C")
    }
    f { println(it) }
    f { println(it) }
}
```

- it에서 emit을 호출하는 것 또한 불편하므로, FlowCollector를 리시버로 만든다. 이렇게 하면 람다식 내부에 FlowCollector 타입의 리시버가 생긴다.

```kotlin
suspend fun main() {
    val f: suspend FlowCollector.() -> Unit = {
        emit("A")
        emit("B")
        emit("C")
    }
    f { println(it) }
    f { println(it) }
}
```

- 이제 람다식을 전달하는 대신에, 인터페이스를 구현한 객체를 만드는 것이 더 낫다. 이때 인터페이스를 Flow라 하고, 해당 인터페이스의 정의는 객체 표현식으로 래핑하면 된다.

```kotlin
interface Flow {
    suspend fun collect(collector: FlowCollector)
}

suspend fun main() {
    val builder: suspend FlowCollector.() -> Unit = {
        emit("A")
        emit("B")
        emit("C")
    }
    val flow = object : chapter19.Flow {
        override suspend fun collect(collector: FlowCollector) {
            collector.builder()
        }

    }
    flow.collect { println(it) }
    flow.collect { println(it) }
}
```

- 마지막으로 플로우 생성을 간단하게 만들기 위해 flow 빌더와 타입에 상관없이 값을 방출하고 모으기 위해 String을 제네릭 타입으로 바꾼다.

```kotlin
fun interface FlowCollector<T> {
    suspend fun emit(value: T)
}

interface Flow<T> {
    suspend fun collect(collector: FlowCollector<T>)
}

fun <T> flow(
    builder: suspend FlowCollector<T>.() -> Unit
) = object : chapter19.Flow<T> {
    override suspend fun collect(collector: FlowCollector<T>) {
        collector.builder()
    }
}

suspend fun main() {
    val f: chapter19.Flow<String> = chapter19.flow {
        emit("A")
        emit("B")
        emit("C")
    }
    f.collect { println(it) }
    f.collect { println(it) }
}
```

- 폴로우를 구현한 방식은 실제 Flow, FlowCollector, flow가 실제 구현된 방식과 거의 동일하다.

### 📌 Flow 처리방식

- 플로우의 각 원소를 변환하는 map 함수를 보자. 이 함수는 새로운 플로우를 만들기 때문에, flow 빌더를 사용한다. 플로우가 시작되면 래핑하고 있는 플로우를 시작하게 되므로, 빌더 내부에서 collect 메서드를 호출하고, 원소를 받을 때마다 map은 원소를 변환하고 새로운 플로루를 방출한다.

```kotlin
public inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = transform { value ->
    return@transform emit(transform(value))
}

suspend fun main() {
    flowOf("A", "B", "C")
        .map {
            delay(1000)
            it.lowercase()
        }
        .collect(::println)
}
```

### 📌 동기로 작동하는 Flow

- 플로우 또한 중단 함수처럼 동기로 작동하기 때문에, 플로우가 완료될 때까지 collect 호출이 중단된다. 즉, 플로우는 새로운 코루틴을 시작하지 않는다.
- 중단 함수가 코루틴을 시작할 수 있는 것처럼, 플로우의 각 단계에서도 코루틴을 시작할 수 있지만 중단 함수의 기본 동작은 아니다.
- 플로우에서 각각의 처리 단계는 동기로 실행되기 때문에, onEach 내부에 delay가 있으면 모든 원소가 처리되기 전이 아닌 각 원소 사이에 지연이 생긴다.

```kotlin
suspend fun main() {
    flowOf("A", "B", "C")
        .onEach { delay(1000) }
        .collect(::println)
}
// (1초후)
// A
// (1초후)
// B
// (1초후)
// C
```

### 📌 Flow와 공유 상태

- 플로우 처리를 통해 좀더 복잡한 알고리즘을 구현할 때는 언제 변수에 대한 접근을 동기화해야 하는지 알아야 한다.
- 커스텀한 플로우 처리 함수를 구현할 때, 플로우의 각 단계가 동기로 작동하기 때문에 동기화 없이도 플로우 내부에 변경 가능한 상태를 정의할 수 있다.

```kotlin
fun Flow<*>.counter() = flow<Int> {
    var counter = 0
    collect {
        counter++
        // 잠깐 바쁘게 만든다.
        List(100) { Random.nextLong() }.shuffled().sorted()
        emit(counter)
    }
}

suspend fun main(): Unit = coroutineScope {
    val f1 = List(1000) { "$it" }.asFlow()
    val f2 = List(1000) { "$it" }.asFlow().counter()

    launch { println(f1.counter().last()) }
    launch { println(f1.counter().last()) }
    launch { println(f2.last()) }
    launch { println(f2.last()) }
}
// 1000
// 1000
// 1000
// 1000
```

- 플로우 단계 외부의 변수를 추출해서 함수에서 사용하는 것이 흔히 저지르는 실수 중 하나다. 외부 변수는 같은 플로우가 모으는 모든 코루틴이 공유하게 되어, **동기화가 필수이며 플로우 컬렉션이 아니라 플로우에 종속되게 된다.** 따라서 두 개의 코루틴이 병렬로 원소를 세게 되고 값이 달라지게 된다.

```kotlin
fun Flow<*>.counter(): Flow<Int> {
    var counter = 0
    return this.map {
        counter++
        // 잠깐 바쁘게 만든다.
        List(100) { Random.nextLong() }.shuffled().sorted()
        counter
    }
}

suspend fun main(): Unit = coroutineScope {
    val f1 = List(1000) { "$it" }.asFlow()
    val f2 = List(1000) { "$it" }.asFlow().counter()

    launch { println(f1.counter().last()) }
    launch { println(f1.counter().last()) }
    launch { println(f2.last()) }
    launch { println(f2.last()) }
}
```