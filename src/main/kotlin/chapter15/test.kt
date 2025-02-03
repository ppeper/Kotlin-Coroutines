package chapter15

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler

@OptIn(ExperimentalCoroutinesApi::class)
//fun main() {
//    val dispatcher = StandardTestDispatcher()

//    println(scheduler.currentTime) // 0
//    scheduler.advanceTimeBy(1000)
//    println(scheduler.currentTime) // 1000
//    scheduler.advanceTimeBy(1000)
//    println(scheduler.currentTime) // 2000

//    CoroutineScope(dispatcher).launch {
//        println("Some work1")
//        delay(1000)
//        println("Some work2")
//        delay(1000)
//        println("Coroutine done")
//    }
//    println("[${dispatcher.scheduler.currentTime}] Before")
//    dispatcher.scheduler.advanceUntilIdle()
//    println("[${dispatcher.scheduler.currentTime}] After")
//}
fun main() {
    val testDispatcher = StandardTestDispatcher()

    CoroutineScope(testDispatcher).launch {
        delay(1)
        println("Done1")
    }
    CoroutineScope(testDispatcher).launch {
        delay(2)
        println("Done2")
    }
    testDispatcher.scheduler.advanceTimeBy(2)
    testDispatcher.scheduler.runCurrent()
}