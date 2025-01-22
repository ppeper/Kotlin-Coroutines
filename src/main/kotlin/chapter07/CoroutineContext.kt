package chapter07

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

fun CoroutineScope.log(msg: String) {
    val name = coroutineContext[CoroutineName]?.name
    println("[$name] $msg")
}

//fun main() = runBlocking(CoroutineName("main")) {
//    log("Started")
//    val v1 = async {
//        delay(500)
//        log("Running async")
//        42
//    }
//    launch {
//        delay(1000)
//        log("Running launch")
//    }
//    // 부모의 Context 대체
//    launch(CoroutineName("Child")) {
//        delay(1000)
//        log("Running Child launch")
//    }
//    log("The answer is ${v1.await()}")
//}

//suspend fun printName() {
//    println(coroutineContext[CoroutineName]?.name)
//}
//
//suspend fun main() = withContext(CoroutineName("Outer")) {
//    printName()     // Outer
//    launch(CoroutineName("Inner")) {
//        printName()     // Inner
//    }
//    delay(100)
//    printName()     // Outer
//}

class CounterContext(
    private val name: String
): CoroutineContext.Element {

    override val key: CoroutineContext.Key<*> = Key
    private var nextNumber = 0

    fun printNext() {
        println("$name: ${nextNumber++}")
    }

    companion object Key: CoroutineContext.Key<CounterContext>
}

suspend fun printNext() {
    coroutineContext[CounterContext]?.printNext()
}

suspend fun main() = withContext(CounterContext("Outer")) {
    printNext()             // Outer: 0
    launch {
        printNext()         // Outer: 1
        launch {
            printNext()     // Outer: 2
        }
        launch(CounterContext("Inner")) {
            printNext()     // Inner: 0
            printNext()     // Inner: 1
            launch {
                printNext() // Inner: 2
            }
        }
    }
    printNext()             // Outer: 3
}

//fun main() {
//    val name: CoroutineName = CoroutineName("A name")
//    val element: CoroutineContext.Element = name
//    val context: CoroutineContext = element
//
//    val job: Job = Job()
//    val jobElement: CoroutineContext.Element = job
//    val jobContext: CoroutineContext = jobElement
//    val ctx: CoroutineContext = CoroutineName("A name")
//
//    val coroutineName: CoroutineName? = ctx[CoroutineName]
//    println(coroutineName?.name)
//    val job: Job? = ctx[Job]
//    println(job)
    // A name
    // null

//    val ctx1: CoroutineContext = CoroutineName("Name1")
//    println(ctx1[CoroutineName]?.name)  // Name1
//    println(ctx1[Job]?.isActive)        // null
//
//    val ctx2: CoroutineContext = Job()
//    println(ctx2[CoroutineName]?.name)  // null
//    println(ctx2[Job]?.isActive)        // true
//
//    val ctx3 = ctx1 + ctx2
//    println(ctx3[CoroutineName]?.name)  // Name1
//    println(ctx3[Job]?.isActive)        // true

//    val ctx1: CoroutineContext = CoroutineName("Name1")
//    println(ctx1[CoroutineName]?.name)  // Name1
//
//    val ctx2: CoroutineContext = CoroutineName("Name2")
//    println(ctx2[CoroutineName]?.name)  // Name2
//
//    val ctx3 = ctx1 + ctx2
//    println(ctx3[CoroutineName]?.name)  // Name2

//    val ctx = CoroutineName("Name1") + Job()
//    println(ctx[CoroutineName]?.name)  // Name1
//    println(ctx[Job]?.isActive)        // true
//
//    val ctx2 = ctx.minusKey(CoroutineName)
//    println(ctx2[CoroutineName]?.name)  // null
//    println(ctx2[Job]?.isActive)        // true

//    val ctx = CoroutineName("Name1") + Job()
//
//    ctx.fold("") { acc, element -> "$acc$element " }
//        .also(::println)     // CoroutineName(Name1) JobImpl{Active}@5fa7e7ff
//
//    val empty = emptyList<CoroutineContext>()
//    ctx.fold(empty) { acc, element -> acc + element }
//        .joinToString()
//        .also(::println)		 // CoroutineName(Name1), JobImpl{Active}@5fa7e7ff

//}