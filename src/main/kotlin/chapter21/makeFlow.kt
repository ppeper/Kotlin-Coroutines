package chapter21

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

//suspend fun main() {
//    flowOf(1, 2, 3, 4, 5)
//        .collect(::print) // 12345
//
//    emptyFlow<Int>()
//        .collect(::print) // 아무 것도 출력  X
//}

//suspend fun main() {
//    listOf(1, 2, 3, 4, 5)
//        .asFlow()
//        .collect(::print) // 12345
//}

//suspend fun getUserName(): String {
//    delay(1000)
//    return "ppeper"
//}
//
//suspend fun main() {
//    val f = suspend {
//        delay(1000)
//        "ppeper"
//    }
//    f.asFlow()
//        .collect(::println)
//
//    ::getUserName
//        .asFlow()
//        .collect(::println)
//}

fun makeFlow(): Flow<Int> = flow {
    repeat(3) { num ->
        delay(1000)
        emit(num)
    }
}

//suspend fun main() {
//    makeFlow()
//        .collect(::println)
//}

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