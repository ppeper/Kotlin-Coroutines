package chapter23

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

//suspend fun main() {
//    flowOf(1, 2, 3)
//        .map { it * it }
//        .collect { println(it) }
//}

fun isEven(num: Int): Boolean = num % 2 == 0

//suspend fun main() {
//    (1..10).asFlow()
//        .filter { it <= 5 } // 1, 2, 3, 4, 5
//        .filter { isEven(it) } // 2, 4
//        .collect { println(it) }
//}

//suspend fun main() {
//    ('A'..'Z').asFlow()
//        .take(5)
//        .collect { print(it) }
//}

//suspend fun main() {
//    val ints = flowOf(1, 2, 3)
//    val doubles = flowOf(0.1, 0.2, 0.3)
//
//    val together = merge(ints, doubles)
//    print(together.toList())
//}

//suspend fun main() {
//    val ints = flowOf(1, 2, 3)
//        .onEach { delay(1000) }
//    val doubles = flowOf(0.1, 0.2, 0.3)
//
//    val together = merge(ints, doubles)
//    together.collect {
//        println(it)
//    }
//}

//suspend fun main() {
//    val flow1 = flowOf("A", "B", "C")
//        .onEach { delay(400) }
//
//    val flow2 = flowOf(1, 2, 3, 4)
//        .onEach { delay(1000) }
//
//    flow1.zip(flow2) { f1, f2 ->
//        "${f1}_${f2}"
//    }.collect {
//        println(it)
//    }
//}

//suspend fun main() {
//    val flow1 = flowOf("A", "B", "C")
//        .onEach { delay(400) }
//
//    val flow2 = flowOf(1, 2, 3, 4)
//        .onEach { delay(1000) }
//
//    flow1.zip(flow2) { f1, f2 ->
//        "${f1}_${f2}"
//    }.collect {
//        println(it)
//    }
//}

//suspend fun main() {
//    val list = flowOf(1, 2, 3, 4)
//        .onEach { delay(1000) }
//    val res = list.fold(0) { accumulator, value -> accumulator + value  }
//    println(res)
//}

//suspend fun main() {
//    flowOf(1, 2, 3, 4)
//        .onEach { delay(1000) }
//        .scan(0) { acc, v -> acc + v }
//        .collect { println(it) }
//}

fun otherFlow(str: String) = flowOf(1, 2, 3)
    .onEach { delay(1000) }
    .map { "${it}_${str}" }

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun main() {
    flowOf("A", "B", "C")
//        .flatMapConcat { otherFlow(it) }
//        .flatMapMerge(concurrency = 2) { otherFlow(it) }
        .flatMapLatest { otherFlow(it) }
        .collect { println(it) }
}
