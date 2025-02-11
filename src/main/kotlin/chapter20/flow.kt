package chapter20

import kotlinx.coroutines.delay

//suspend fun main() {
//    val f: suspend () -> Unit = {
//        println("A")
//        delay(1000)
//        println("B")
//        delay(1000)
//        println("C")
//    }
//    f()
//    f()
//}

//suspend fun main() {
//    val f: suspend ((String) -> Unit) -> Unit = { emit ->
//        emit("A")
//        emit("B")
//        emit("C")
//    }
//    f { println(it) }
//    f { println(it) }
//}

//fun interface FlowCollector {
//    suspend fun emit(value: String)
//}
//
//suspend fun main() {
//    val f: suspend (FlowCollector) -> Unit = {
//        it.emit("A")
//        it.emit("B")
//        it.emit("C")
//    }
//    f { println(it) }
//    f { println(it) }
//}

//suspend fun main() {
//    val f: suspend FlowCollector.() -> Unit = {
//        emit("A")
//        emit("B")
//        emit("C")
//    }
//    f { println(it) }
//    f { println(it) }
//}

//interface Flow {
//    suspend fun collect(collector: FlowCollector)
//}
//
//suspend fun main() {
//    val builder: suspend FlowCollector.() -> Unit = {
//        emit("A")
//        emit("B")
//        emit("C")
//    }
//    val flow = object : chapter19.Flow {
//        override suspend fun collect(collector: FlowCollector) {
//            collector.builder()
//        }
//
//    }
//    flow.collect { println(it) }
//    flow.collect { println(it) }
//}

fun interface FlowCollector<T> {
    suspend fun emit(value: T)
}

interface Flow<T> {
    suspend fun collect(collector: FlowCollector<T>)
}

fun <T> flow(
    builder: suspend FlowCollector<T>.() -> Unit
) = object : Flow<T> {
    override suspend fun collect(collector: FlowCollector<T>) {
        collector.builder()
    }
}

suspend fun main() {
    val f: Flow<String> = flow {
        emit("A")
        emit("B")
        emit("C")
    }
    f.collect { println(it) }
    f.collect { println(it) }
}