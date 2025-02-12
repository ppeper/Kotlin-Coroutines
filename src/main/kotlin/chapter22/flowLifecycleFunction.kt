package chapter22

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.coroutineContext

//suspend fun main() {
//    flowOf(1, 2, 3, 4)
//        .onEach { print(it) }
//        .collect() // 1234
//}

//suspend fun main() {
//    flowOf(1, 2, 3, 4)
//        .onEach { delay(1000) }
//        .collect { println(it) } // 1234
//}

//suspend fun main() {
//    flowOf(1, 2)
//        .onEach { delay(1000) }
//        .onStart { println("Before") }
//        .collect { println(it) }
//}

//suspend fun main() {
//    flowOf(1, 2)
//        .onEach { delay(1000) }
//        .onCompletion { println("Completed") }
//        .collect { println(it) }
//}

//suspend fun main() = coroutineScope {
//    flow<List<Int>> { delay(1000) }
//        .onEmpty { emit(emptyList()) }
//        .collect { println(it) }
//}

class MyError: Throwable("My error")

//val flow = flow {
//    emit(1)
//    emit(2)
//    throw MyError()
//}
//
//suspend fun main(): Unit {
//    flow.onEach { println("Got $it") }
//        .catch { println("Caught $it") }
//        .collect { println("Collected $it")}
//}

//val flow = flow {
//    emit("Message")
//    throw MyError()
//}
//
//suspend fun main(): Unit {
//    try {
//        flow.collect { println("Collected $it") }
//    } catch (e: MyError) {
//        println("Caught!")
//    }
//}

//val flow = flow {
//    emit("Message1")
//    emit("Message2")
//}
//
//suspend fun main(): Unit {
//    flow.onStart { println("Before") }
//        .onEach { throw MyError() }
//        .catch { println("Caught $it") }
//        .collect()
//}

//fun usersFlow(): Flow<String> = flow {
//    repeat(2) {
//        val ctx = currentCoroutineContext()
//        val name = ctx[CoroutineName]?.name
//        emit("User$it in $name")
//    }
//}

//suspend fun main(): Unit {
//    val users = usersFlow()
//    withContext(CoroutineName("Name1")) {
//        users.collect { println(it) }
//    }
//    withContext(CoroutineName("Name2")) {
//        users.collect { println(it) }
//    }
//}

//suspend fun present(place: String, message: String) {
//    val ctx = coroutineContext
//    val name = ctx[CoroutineName]?.name
//    println("[$name] $message on $place")
//}
//
//fun messagesFlow(): Flow<String> = flow {
//    present("flow builder", "Message")
//    emit("Message")
//}
//
//suspend fun main(): Unit {
//    val users = messagesFlow()
//    withContext(CoroutineName("Name1")) {
//        users
//            .flowOn(CoroutineName("Name3"))
//            .onEach { present("onEach", it) }
//            .flowOn(CoroutineName("Name2"))
//            .collect { present("collect", it) }
//    }
//}

suspend fun main(): Unit = coroutineScope {
    flowOf("User1", "User2")
        .onStart { println("Users:") }
        .onEach { println(it) }
        .launchIn(this)
}