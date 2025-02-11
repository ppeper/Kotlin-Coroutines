# 19장 플로우란 무엇인가

### 📌 플로우와 값들을 나타내는 다른 방법들 비교

- 플로우의 개념은 RxJava나 Reator와 비슷하다.
- 한번에 모든 값을 만들 때는 List나 Set과 같은 컬렉션을 사용한다. 이때 명심해야 할 점은 List와 Set이 모든 원소의 계산이 완료된 컬렉션이라는 것이다.
    - 즉, 값을 계산하는 과정에 시간이 걸리기 때문에 원소들이 채워질 때까지 기다려야 한다.

```kotlin
fun getList(): List<String> = List(3) {
    Thread.sleep(1000)
    "User$it"
}

fun main() {
    val list = getList()
    println("Function started")
    list.forEach(::println)
}
// (3초후)
// Function started
// User0
// User1
// User2
```

- 반면에 시퀀스를 사용하면 하나씩 계산하여 출력이 가능하다.

```kotlin
fun getSequence(): Sequence<String> = sequence {
    repeat(3) {
        Thread.sleep(1000)
        yield("User$it")
    }
}

fun main() {
    val list = getSequence()
    println("Function started")
    list.forEach(::println)
}
// Function started
// (1초후)
// User0
// (1초후)
// User1
// (1초후)
// User2
```

- 시퀀스는 CPU 집약적인 연산 또는 블로킹 연산일 때 필요할 때마다 값을 계산하는 플로우를 나타내기에 적절하다.
- 시퀀스 빌더 내부에 중단점이 있다면 값을 기다리는 스레드가 블로킹된다. 따라서 sequence 빌더의 스코프내에서는 SequenceScope의 리시버에서 호출되는 함수(yield와 yieldAll) 외에 다른 중단 함수를 사용할 수 없다.

### 📌 플로우 특징

- 플로우의 최종 연산은 스레드를 블로킹하는 대신 코루틴을 중단시킨다. 플로우는 코루틴 컨텍스트를 활용하고 예외를 처리하는 등의 코루틴 기능도 제공한다.
- 플로 처리는 취소 가능하며, 구조화된 동시성을 기본적으로 갖추고 있다.

```kotlin
fun userFlow(): Flow<String> = flow {
    repeat(3) {
        delay(1000)
        val ctx = currentCoroutineContext()
        val name = ctx[CoroutineName]?.name
        emit("User$it in $name")
    }
}

suspend fun main() {
    val users = userFlow()

    withContext(CoroutineName("Flow")) {
        val job = launch {
            users.collect(::println)
        }
        launch {
            delay(2100)
            println("I got enough")
            job.cancel()
        }
    }
}
// (1초후)
// User0 in Flow
// (1초후)
// User1 in Flow
// (0.1초후)
// I got enough

```

### 📌 플로우 명명법

- 플로우는 어딘가에서 시작되어야 한다. 플로우 빌더, 다른 객체에서의 변환 또는 헬퍼 함수로부터 시작된다.
- 플로우의 마지막 연산은 `최종 연산` 이라 불리며, 중단 가능하거나 스코프를 필요로 하는 유일한 연산이라는 점에서 아주 중요하다. 최종 연산은 주로 람다 표현식을 가진 또는 가지지 않는 collect가 된다.
- 시작 연산과 최종 연산 사이에 플로우를 변경하는 `중간 연산` 을 가질 수 있다.