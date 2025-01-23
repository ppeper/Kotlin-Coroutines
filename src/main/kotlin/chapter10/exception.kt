package chapter10

import kotlinx.coroutines.*

fun main(): Unit = runBlocking {
//    supervisorScope {
//        launch {
//            delay(1000)
//            throw Error("Some error")
//        }
//
//        launch {
//            delay(2000)
//            println("Will be printed")
//        }
//    }
//    delay(1000)
//    println("Done")
//    val scope = CoroutineScope(SupervisorJob())
//
//    val deferred = scope.async {
//        delay(1000)
//        throw Error("Some error")
//    }
//
//    try {
//        deferred.await()
//    } catch (e: Throwable) {
//        println(e)
//    }
//    delay(2000)
//    println("Done")

    val handler = CoroutineExceptionHandler { ctx, throwable ->
        println("Caught $throwable")
    }
    val scope = CoroutineScope(SupervisorJob() + handler)
    scope.launch {
        delay(1000)
        throw Error("Some error")
    }
    scope.launch {
        delay(2000)
        println("Will be printed")
    }
    delay(3000)
}

//object MyNonPropagatingException: CancellationException()
//
//suspend fun main(): Unit = coroutineScope {
//    launch { // 1
//        launch { // 2
//            delay(2000)
//            println("Will not be printed")
//        }
//        throw MyNonPropagatingException // 3
//    }
//    launch { // 4
//        delay(2000)
//        println("Will be printed")
//    }
//}