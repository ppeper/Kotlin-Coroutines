# 7장 코루틴 컨텍스트

- 코루틴 빌더의 정의에서 첫 번째 파라미터가 CoroutineContext라는 것을 알 수 있다.

```kotlin
public fun CoroutineScope.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    val newContext = newCoroutineContext(context)
    val coroutine = if (start.isLazy)
        LazyStandaloneCoroutine(newContext, block) else
        StandaloneCoroutine(newContext, active = true)
    coroutine.start(start, coroutine, block)
    return coroutine
}
```

- 리시버뿐만 아니라 마지막 인자도 CoroutineScope의 확장함수의 형태로 되어있다.

### 📌 CoroutineScope And Continuation

```kotlin
public interface CoroutineScope {
    /**
     * The context of this scope.
     * Context is encapsulated by the scope and used for implementation of coroutine builders that are extensions on the scope.
     * Accessing this property in general code is not recommended for any purposes except accessing the [Job] instance for advanced usages.
     *
     * By convention, should contain an instance of a [job][Job] to enforce structured concurrency.
     */
    public val coroutineContext: CoroutineContext
}
```

- CoroutineScope는 `CoroutineContext` 를 감싸는 Wrapper 처럼 보인다.
- Continuation 또한 CoroutineContext를 포함하고 있다.

```kotlin
public interface Continuation<in T> {
    /**
     * The context of the coroutine that corresponds to this continuation.
     */
    public val context: CoroutineContext

    /**
     * Resumes the execution of the corresponding coroutine passing a successful or failed [result] as the
     * return value of the last suspension point.
     */
    public fun resumeWith(result: Result<T>)
}
```

### 📌 CoroutineContext Interface

- CoroutineContext는 원소나 원소들의 집합을 나타내는 인터페이스 이다.
- `Job`, `CoroutineName`, `CoroutineDispatcher`와 같은 Element 객체들이 인덱싱된 집합이라는 점에서 Map, Set과 같은 컬렉션 개념과 비슷하다. 특이한 점은 각 Element 또한 CoroutineContext하는 점이다
- Context의 정보들은 LinkedList 형태로 연결되어 있다.
- Context에서 모든 원소는 식별할 수 있는 유일한 Key를 가지고 있다. 각 키는 주소로 비교가 된다.

```kotlin
fun main() {
    val name: CoroutineName = CoroutineName("A name")
    val element: CoroutineContext.Element = name
    val context: CoroutineContext = element
    
    val job: Job = Job()
    val jobElement: CoroutineContext.Element = job
    val jobContext: CoroutineContext = jobElement
    val ctx: CoroutineContext = CoroutineName("A name")
}
```

### 📌 CoroutineContext에서 원소 찾기

- CoroutineContext는 컬렉션과 비슷하여 `get`을 이용해 유일한 Key를 가진 원소를 찾을 수 있다. Kotlin에선 대괄호를 사용해 실행할 수 있다.

```kotlin
fun main() {
    val ctx: CoroutineContext = CoroutineName("A name")

    val coroutineName: CoroutineName? = ctx[CoroutineName]
    println(coroutineName?.name)
    val job: Job? = ctx[Job]
    println(job)
    // A name
    // null
}
```

- CoroutineName을 찾기 위해서는 CoroutineName을 사용하기만 하면 된다. 이는 Companion 객체로 되어있다.

```kotlin
public data class CoroutineName(
    /**
     * User-defined coroutine name.
     */
    val name: String
) : AbstractCoroutineContextElement(CoroutineName) {
    /**
     * Key for [CoroutineName] instance in the coroutine context.
     */
    public companion object Key : CoroutineContext.Key<CoroutineName>

    /**
     * Returns a string representation of the object.
     */
    override fun toString(): String = "CoroutineName($name)"
}

```

### 📌 코루틴 컨텍스트

1. 더하기
- CoroutineContext는 서로 합쳐 하나의 Context로 만들 수 있다.
- 다른 키를 가진 두 원소를 더하면 만들어진 Context는 두 가지 키를 모두 가진다.

```kotlin
fun main() {
    val ctx1: CoroutineContext = CoroutineName("Name1")
    println(ctx1[CoroutineName]?.name)  // Name1
    println(ctx1[Job]?.isActive)        // null

    val ctx2: CoroutineContext = Job()
    println(ctx2[CoroutineName]?.name)  // null
    println(ctx2[Job]?.isActive)        // true
    
    val ctx3 = ctx1 + ctx2
    println(ctx3[CoroutineName]?.name)  // Name1
    println(ctx3[Job]?.isActive)        // true
}
```

- 만약 같은 키를 가진 또 다른 원소가 더해지면 Map의 key 처럼 새로운 원소가 기존 원소를 대체한다.

```kotlin
fun main() {
    val ctx1: CoroutineContext = CoroutineName("Name1")
    println(ctx1[CoroutineName]?.name)  // Name1

    val ctx2: CoroutineContext = CoroutineName("Name2")
    println(ctx2[CoroutineName]?.name)  // Name2
    
    val ctx3 = ctx1 + ctx2
    println(ctx3[CoroutineName]?.name)  // Name2
}
```

- 비어 있는 코루틴 Context도 생성이 가능하다. 이때는 다른 Context와 더해도 원소가 없으므로 아무런 변화가 없다.
1. 원소 제거
- `minusKey` 함수에 키를 넣는 방식으로 원소를 Context에서 제거할 수 있다.

```kotlin
fun main() {
    val ctx = CoroutineName("Name1") + Job()
    println(ctx[CoroutineName]?.name)  // Name1
    println(ctx[Job]?.isActive)        // true
    
    val ctx2 = ctx.minusKey(CoroutineName)
    println(ctx2[CoroutineName]?.name)  // null
    println(ctx2[Job]?.isActive)        // true
}
```

### 📌 Context Folding

- Context와 각 원소를 조작해야 하는 경우 다른 컬렉션의 fold 함수와 유사하게 사용할 수 있다.

```kotlin
fun main() {
    val ctx = CoroutineName("Name1") + Job()

    ctx.fold("") { acc, element -> "$acc$element " }
        .also(::println)     // CoroutineName(Name1) JobImpl{Active}@5fa7e7ff 

    val empty = emptyList<CoroutineContext>()
    ctx.fold(empty) { acc, element -> acc + element }
        .joinToString()
        .also(::println)		 // CoroutineName(Name1), JobImpl{Active}@5fa7e7ff
}
```

### 📌 코루틴 Context와 Builder

- CoroutineContext는 코루틴의 데이터를 저장하고 전달하는 방법이다. 이는 부모-자식 관계의 영향 중 하나로 부모는 기본적으로 Context를 자식에서 전달하고, 자식은 부모로부터 Context를 상속 받는다고 할 수 있다.
- 코루틴 Context를 계산하는 간단한 공식은 아래와 같다.

```kotlin
defaultContext + parentContext + childContext
```

- 따라서 새로운 원소가 같은 키를 가진 이전 원소를 대체하므로, 자식의 Context는 부모로부터 상속받은 Context 중 같은 Key를 가진 원소를 대체한다.

```kotlin
fun CoroutineScope.log(msg: String) {
    val name = coroutineContext[CoroutineName]?.name
    println("[$name] $msg")
}

fun main() = runBlocking(CoroutineName("main")) {
    log("Started")
    val v1 = async {
        delay(500)
        log("Running async")
        42
    }
    launch {
        delay(1000)
        log("Running launch")
    }
    log("The answer is ${v1.await()}")
    // 부모의 Context 대체
    launch(CoroutineName("Child")) {
        delay(1000)
        log("Running Child launch")
    }
}
// [main] Started
// [main] Running async
// [main] The answer is 42
// [main] Running launch
// [Child] Running Child launch
```

### 📌 중단 함수에서 Context에 접근하기

- CoroutineScope는 Context를 접근할 때 사용하는 coroutineContext 프로퍼티를 가지고 있다. Context는 중단 함수 사이에 전달되는 Continuation 객체가 참조하고 있어, 중단 함수에서 부모의 Context에 접근하는 것이 가능하다.

```kotlin
suspend fun printName() {
    println(coroutineContext[CoroutineName]?.name)
}

suspend fun main() = withContext(CoroutineName("Outer")) {
    printName()     // Outer
    launch(CoroutineName("Inner")) {
        printName()     // Inner
    }
    delay(100)
    printName()     // Outer
}
```

### 📌 Context를 개별적으로 생성하기

- 코루틴 Context를 커스텀하게 만드는 경우는 흔치 않지만 가장 쉬운 방법은 `CoroutineContext.Element` 인터페이스를 구현하는 클래스를 만드는 것이다.
- 이러한 클래스는 CoroutineContext.Key<*> 타입의 Key 프로퍼티를 필요로 하고, Key는 Context를 식별하는데 사용된다.

```kotlin
class CounterContext(
    private val name: String
): CoroutineContext.Element {

    override val key: CoroutineContext.Key<*> = Key
    private var nextNumber = 0

    fun printNext() {
        println("$name: ${nextNumber++}")
    }

    companion object Key: CoroutineContext.Key<CounterContext>
}

suspend fun printNext() {
    coroutineContext[CounterContext]?.printNext()
}

suspend fun main() = withContext(CounterContext("Outer")) {
    printNext()             // Outer: 0
    launch {
        printNext()         // Outer: 1
        launch {
            printNext()     // Outer: 2
        }
        launch(CounterContext("Inner")) {
            printNext()     // Inner: 0
            printNext()     // Inner: 1
            launch {
                printNext() // Inner: 2
            }
        }
    }
    printNext()             // Outer: 3
}
```