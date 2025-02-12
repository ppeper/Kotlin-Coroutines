# 21장 플로우 만들기

### 📌 원시값을 가지는 플로우

- 플로우를 만드는 가장 간단한 방법은 플로우가 어떤 값을 가져야 하는지 정의하는 flowOf 함수를 사용하는 것이다.

```kotlin
suspend fun main() {
    flowOf(1, 2, 3, 4, 5)
        .collect(::print) // 12345
    
    emptyFlow<Int>()
        .collect(::print) // 아무 것도 출력  X
}
```

### 📌 Converter

- asFlow 함수를 사용해서 Iterable, Iterator, Sequence를 Flow로 바꿀 수도 있다.

```kotlin
suspend fun main() {
    listOf(1, 2, 3, 4, 5)
        .asFlow()
        .collect(::print) // 12345
}
```

### 📌 함수를 플로우로 바꾸기

- 플로우는 시간상 지연되는 하나의 값을 나타낼 때 자주 사용된다.
- 따라서 중단 함수를 플로우로 변환하는 것 또한 가능하다.

```kotlin
suspend fun getUserName(): String {
    delay(1000)
    return "ppeper"
}

suspend fun main() {
    val f = suspend {
        delay(1000)
        "ppeper"
    }
    f.asFlow()
        .collect(::println)
    
    ::getUserName
        .asFlow()
        .collect(::println)
}
```

### 📌 플로우와 리액티브 스트림

- 애플리케이션에서 리액티브 스트림을 활요하고 있다면 코드를 변로 바꾸지 않고 플로우를 적용할 수 있다. Flux, Flowable, Obserable은 `kotlin-coroutines-reactive` 라이브러리의 asFlow 함수를 사용해 Flow로 변환 가능한 Publisher 인터페이스를 구현하고 있다.
- 역으로 변환하려면 `kotlinx-coroutines-reactor` 라이브러리를 사용하면 Flow를 Flux로 변환할 수 있다. `kotlinx-coroutines-rx2` 라이브러리를 사용하면 Flow를 Flowable이나 Obserable로 변환 가능하다.

### 📌 플로우 빌더

- 플로우를 만들 때 가장 많이 사용되는 방법은 flow 빌더이다. 빌더는 flow 함수를 먼저 호출하고, 람다식 내부에서 emit 함수를 사용해 다음 값을 방출한다.
- Channel이나 Flow에서 모든 값을 방출하려면 emitAll을 사용할 수 있다.

```kotlin
fun makeFlow(): Flow<Int> = flow {
    repeat(3) { num ->
        delay(1000)
        emit(num)
    }
}

suspend fun main() {
    makeFlow()
        .collect(::println)
}
// (1초후)
// 0
// (1초후)
// 1
// (1초후)
// 2
```

### 📌 ChannelFlow

- Flow는 콜드 데이터 스트림이므로 필요할 때만 값을 생성한다. 앞에서 봤던 allUserFlow를 떠올려 보면 사용자 목록의 다음 페이지는 리시버가 필요로 할때 요청한다.
- 예를 들어 특정 사용자를 찾는 상황에서, 사용자가 첫 번째 페이지에 없다면 더 많은 페이지를 요청하지 않아도 된다. 아래 에제에서 flow 빌더를 사용해 다음 원소를 생성한다. 다음 페이지는 필요할 때만 지연 요청한다는 것만 명심하면 된다.

```kotlin
data class User(val name: String)

interface UserApi {
    suspend fun takePage(pageNumber: Int): List<User>
}

class FakeUserApi: UserApi {
    private val users = List(20) { User("User$it") }
    private val pageSize = 3

    override suspend fun takePage(
        pageNumber: Int
    ): List<User> {
        delay(1000)
        return users
            .drop(pageSize * pageNumber)
            .take(pageSize)
    }
}

fun allUsersFlow(api: UserApi): Flow<User> = flow {
    var page = 0
    do {
        println("Fetching page $page")
        val users = api.takePage(page++)
        emitAll(users.asFlow())
    } while (users.isNotEmpty())
}

suspend fun main() {
    val api = FakeUserApi()
    val users = allUsersFlow(api)
    val user = users
        .first {
            println("Checking $it")
            delay(1000)
            it.name == "User3"
        }
    println(user)
}

// Fetching page 0
// (1초후)
// Checking User(name=User0)
// (1초후)
// Checking User(name=User1)
// (1초후)
// Checking User(name=User2)
// (1초후)
// Fetching page 1
// (1초후)
// Checking User(name=User3)
// (1초후)
// User(name=User3)
```

- 반면 원소를 처리하고 있을 때 미리 페이지를 받아오고 싶은 경우도 있다. 네트워크 호출을 더 빈번하게 하는 단점이 있지만 결과를 더 빠르게 얻어올 수 있다.
- 이렇게 하려면 데이터를 생성하고 소비하는 과정이 별개로 진행되어여 한다. 이는 채널과 같은 핫 데이터 스트림의 전형적인 특징이다.
- 따라서 채널과 플로우를 합친 형태가 필요하다. channelFlow 함수는 플로우처럼 Flow 인터페이스를 구현하기 때문에 플로우가 가지는 특징을 제공한다.
- 채널플로우 빌더는 일반 함수이며 최종 연산으로 시작된다. 한 번 시작하기만 하면 리시버를 기다릴 필요 없이 분리된 코루틴에서 값을 생성한다는 점이 채널과 비슷하다고 할 수 있다.

```kotlin
fun allUsersFlow(api: UserApi): Flow<User> = channelFlow {
    var page = 0
    do {
        println("Fetching page $page")
        val users = api.takePage(page++)
        users.forEach { send(it) }
    } while (users.isNotEmpty())
}
// Fetching page 0
// (1초후)
// Checking User(name=User0)
// Fetching page 1
// (1초후)
// Checking User(name=User1)
// Fetching page 2
// (1초후)
// Checking User(name=User2)
// Fetching page 3
// (1초후)
// Checking User(name=User3)
// Fetching page 4
// (1초후)
// User(name=User3)
```

### 📌 callbackFlow

- 사용자의 클릭이나 활동 변화를 감지해야 하는 이벤트 플로가 필요하다 하자. 간지하는 프로세스는 이벤트를 처리하는 프로세스와 독립적이어야 하므로 channelFlow를 사용해도 좋다. 하지만 이 경우에는 callbackFlow를 사용하는 것이 낫다.
- 버전 1.3.4에서는 콜백을 사용할 때 에러에 덜 민감하도록 몇 가지 작은 변화가 있었다. 하지만 가장 큰 차이점은 callbackFlow가 콜백 함수를 래핑하는 방식으로 변경된 것이다.
- callbackFlow는 ProduceScope<T>에서 작동한다. 다음은 콜백을 래핑하는 데 유용한 몇가지 함수다.
    - `awaitClose { ... }` : 채널이 닫힐 때까지 중단되는 함수다. 채널이 닫힌 다음에 인자로 들어온 함수가 실행된다.
    - `trySendBlocking(value)` : send와 비슷하지만 중단되는 대신 블로킹하여 중단 함수가 아닌 함수에서도 사용할 수 있다.
    - `close()` : 채널을 닫는다.
    - `cancel(throwable)` : 채널을 종료하고 플로우에 예외를 던진다.

```kotlin
fun flowFrom(api: CallbackBaseApi): Flow<T> = callbackFlow {
    val callback = object : Callback {
        override fun onNextValue(value: T) {
            trySendBlocking(value)
        }
        override fun onApiError(cause: Throwable) {
            cancel(CancellationException("API ERROR", cause))
        }
    }
    api.register(callback)
    awaitClose { api.unregister(callback) }
}
```