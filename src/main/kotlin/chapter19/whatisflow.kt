package chapter19

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun getList(): List<String> = List(3) {
    Thread.sleep(1000)
    "User$it"
}

fun getSequence(): Sequence<String> = sequence {
    repeat(3) {
        Thread.sleep(1000)
        yield("User$it")
    }
}

//fun main() {
//    val list = getList()
//    println("Function started")
//    list.forEach(::println)
//}

//fun main() {
//    val list = getSequence()
//    println("Function started")
//    list.forEach(::println)
//}

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