package chapter14

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicInteger

//var counter = 0

suspend fun massiveRun(action: suspend () -> Unit) {
    withContext(Dispatchers.Default) {
        repeat(1000) {
            launch {
                repeat(1000) { action() }
            }
        }
    }
}
//fun main() = runBlocking {
//    val lock = Any()
//    massiveRun {
//        synchronized(lock) { // 스레드를 블로킹한다.
//            counter++
//        }
//    }
//    println(counter)
//}

private var counter = AtomicInteger()

fun main() = runBlocking {
    massiveRun {
//        counter.incrementAndGet()
        counter.set(counter.get() + 1)
    }
    println(counter.get())
}

//val mutex = Mutex()

//suspend fun delayAndPrint() {
//    mutex.lock()
//    delay(1000)
//    println("Done")
//    mutex.unlock()
//}

//suspend fun main() = coroutineScope {
//    repeat(5) {
//        launch {
//            delayAndPrint()
//        }
//    }
//}

//suspend fun main() = coroutineScope {
//    val semaphore = Semaphore(2)
//
//    repeat(5) {
//        launch {
//            semaphore.withPermit {
//                delay(1000)
//                print(it)
//            }
//        }
//    }
//}