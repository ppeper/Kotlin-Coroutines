package chapter12

import kotlinx.coroutines.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.ContinuationInterceptor
import kotlin.random.Random
import kotlin.system.measureTimeMillis

//private val dispatcher = Dispatchers.Default
//    .limitedParallelism(5)

//suspend fun main() = coroutineScope {
//    repeat(1000) {
//        launch {
//            List(1000) { Random.nextLong() }.maxOrNull()
//            val threadName = Thread.currentThread().name
//            println("Running on thread: $threadName")
//        }
//    }
//}

//suspend fun main() {
//    val time = measureTimeMillis {
//        coroutineScope {
//            repeat(50) {
//                launch(Dispatchers.IO) {
//                    Thread.sleep(1000)
//                    val threadName = Thread.currentThread().name
//                    println("Running on thread: $threadName")
//                }
//            }
//        }
//    }
//    println(time)
//}

suspend fun printCoroutinesTime(
    dispatcher: CoroutineDispatcher
) {
    val test = measureTimeMillis {
        coroutineScope {
            repeat(100) {
                launch(dispatcher) {
                    Thread.sleep(1000)
                }
            }
        }
    }
    println("$dispatcher took: $test")
}
//}

//suspend fun main(): Unit = coroutineScope {
//    launch {
//        printCoroutinesTime(Dispatchers.IO)
//        // Dispatchers.IO took: 2014
//    }
//    launch {
//        val dispatcher = Dispatchers.IO
//            .limitedParallelism(100)
//        printCoroutinesTime(dispatcher)
//        // Dispatchers.IO.limitedParallelism(100) took: 1028
//    }

//var i = 0
//val dispatcher = Executors.newSingleThreadExecutor()
//    .asCoroutineDispatcher()
//val dispatcher = Dispatchers.Default
//    .limitedParallelism(1)
//suspend fun main(): Unit = coroutineScope {
//    repeat(10000) {
////        launch(Dispatchers.IO) {
////            i++
////        }
//        launch(dispatcher) {
//            i++
//        }
//    }
//    delay(1000)
//    println(i)
//}

suspend fun main(): Unit = coroutineScope {
    ContinuationInterceptor
    val dispatcher = Dispatchers.Default
        .limitedParallelism(1)
    val job = Job()
    repeat(5) {
        launch(dispatcher + job) {
            Thread.sleep(1000)
        }
    }
    job.complete()
    val time = measureTimeMillis { job.join() }
    println("$time")
}
