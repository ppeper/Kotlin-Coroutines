# 6장 코루틴 빌더

### 📌 코루틴 빌더

- 중단 함수는 Continuation 객체를 다른 중단 함수로 전달해야 한다.
- 중단 함수를 연속적으로 호출하면 시작되는 지점이 반드시 있다. 코루틴 빌터가 그 역할을 한다.

### 📌 launch 빌더

- launch가 작동하는 방식은 Thread 함수를 호출하여 새로운 스레드를 시작하는 것과 비슷하다
- launch 함수는 CoroutineScope 인터페이스의 확장 함수이다. 이는 부모 코루틴과 자식 코루틴 사이의 관계를 정립하기 위한 목적으로 사용되는 `구조화된 동시성 (Structured concurrency)`의 핵심이다.

```kotlin
fun main() {
    GlobalScope.launch {
        delay(1000L)
        println("World")
    }
    GlobalScope.launch {
        delay(1000L)
        println("World")
    }
    GlobalScope.launch {
        delay(1000L)
        println("World")
    }
    GlobalScope.launch {
        delay(1000L)
        println("World")
    }
    println("Hello,")
    Thread.sleep(2000L)
}
// Hello,
// (1초 delay)
// World
// World
// World
```

### 📌 runBlocking

- runBlocking은 블로킬이 필요할 때 사용된다. 이는 둥단 메인 함수와 마찬가지로 시작한 스레드를 중단 시킨다.
- 특수한 경우 사용하게 되며, 유닛 테스트 혹은 프로그램이 끝나는 걸 방지하기 위해 스레드를 블로킬할 필요가 있을 때 사용한다.
    - 유닛 테스트의 경우 코루틴을 가상 시간으로 실행시키는 `runTest` 를 주로 사용한다.

```kotlin
fun main() = runBlocking {
    //...
}

class Tests {
    @Test
    fun `name_functions`() = runBlocking {
    //...
    }
}
```

### 📌 async 빌더

- async 코루틴 빌더는 launch와 비슷하지만 값을 생성하도록 설계되어 있다.
- 이 값은 람다 표현식으로 반환되며, `Deffered<T>` 타입의 객체를 리턴한다.
- launch와 다르게 `await()` 함수를 통해 작업이 끝나면 값을 반환하는 중단 메서드가 있으며 awaitAll()과 같은 함수도 지원한다.

```kotlin
fun main() = runBlocking {
    val deferred: Deferred<Int> = GlobalScope.async {
        delay(1000L)
        29
    }
    // 다른 작업 진행..
    val result = deferred.await() // (1초 후)
    println(result) // 29
    println(deferred.await()) // 함께 작성이 가능
}
```

> 💡값을 반환한다는 추가 특징이 있어, 값을 사용하지 않는다면, launch를 써야 한다.
> 
> async 빌터는 두 가지 다른 곳에서 데이터를 얻어와 합치는 경우와 같이 병렬로 실행해야할 때 주로 사용된다.

```kotlin
fun main() = runBlocking {
    val first = GlobalScope.async {
        delay(1000L)
        "Text 1"
    }
    val second = GlobalScope.async {
        delay(3000L)
        "Text 2"
    }
    val third = GlobalScope.async {
        delay(2000L)
        "Text 3"
    }
    println(first.await())
    println(second.await())
    println(third.await())
    println(awaitAll(first, second, third)) // awaitAll() 함수로 여러 async 함수 한번에 호출가능
}
```

### 📌 구조화된 동시성

- 코루틴이 GlobalScope에서 시작되었다면 프로그램은 해당 코루틴을 기다리지 않는다 (GlobalScope는 사용을 지양해야한다)
- 부모는 자식들을 위한 소코프를 제공하고 자식들을 해당 스코프 내에서 호출한다. 이를 통해 `구조화된 동시성` 이라는 관계가 성립된다. 부모 -자식 관계의 가장 중요한 특징은 다음과 같다.
    - 자식은 부모로부터 Context를 상속받는다 (자식이 재정의 할 수도 있다)
    - 부모는 모든 자식이 작업을 마칠 때까지 기다린다.
    - 부모 코루틴이 취소되면 자식 코루틴도 취소된다.
    - 자식 코루틴에서 예외가 발생하면, 부모 코루틴 또한 예외로 소멸한다.

### 📌 coroutineScope 사용

- launch, async 는 CoroutineScope의 확장 함수이기 때문에, scope가 필요하다. 이때 스코프를 인자로 넘기는 건 좋은 방법이 아니다.
- 이때 사용할 수 있는 것이 스코프를 만들어 주는 중단 함수인 `coroutineScope` 함수이다.

```kotlin
suspend fun getArticleForUser(
		userToken: String?,
): List<ArticleJson> = coroutineScope {
    val articles = async { articleRepository.getArticles() }
    val user = userService.getUser(userToken)
    articles.await()
        .filter { canSeeOnList(user, it) }
        .map { toArticleJson(it) }
}
```