package chapter06

import kotlinx.coroutines.*

//fun main() = runBlocking {
//    val deferred: Deferred<Int> = GlobalScope.async {
//        delay(1000L)
//        29
//    }
//    // 다른 작업 진행..
//    val result = deferred.await() // (1초 후)
//    println(result) // 29
//    println(deferred.await()) // 함께 작성이 가능
//}

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