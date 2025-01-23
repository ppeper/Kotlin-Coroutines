package chapter09

import kotlinx.coroutines.coroutineScope

suspend fun main() = coroutineScope {
//    val job = launch {
//        repeat(1000) {
//            delay(200)
//            println("Printing $it")
//        }
//    }
//    delay(1100)
//    job.run {
//        cancel()
//        join()
//    }
//    // job.cancelAndJoin()
//    println("Cancelled successfully")

//    val job = launch {
//        repeat(1000) {
//            delay(100)
//            Thread.sleep(100)
//            println("Printing $it")
//        }
//    }
//    delay(1000)
//    job.cancel()
//    println("Cancelled successfully")

//    val job = Job()
//    launch(job) {
//        try {
//            repeat(1000) {
//                delay(200)
//                println("Printing $it")
//            }
//        } catch (e: CancellationException) {
//            println(e)
//            throw e
//        } finally {
//            println("Will always printed")
//        }
//    }
//    delay(1100)
//    job.cancelAndJoin()
//    println("Cancelled success")
//    delay(1000)

//    val job = Job()
//    launch(job) {
//        try {
//            delay(2000)
//            println("Job is done")
//        } finally {
//            println("Finally")
//            launch { // 무시
//                println("Will not be printed")
//            }
//        }
//    }
//    delay(1000) // 여기서 에러가 발생
//    job.cancelAndJoin()
//    println("Cancelled done")

//    val job = launch {
//        delay(1000)
//    }
//    job.invokeOnCompletion { cause: Throwable? ->
//        println("Finished")
//    }
//    delay(400)
//    job.cancelAndJoin()
}