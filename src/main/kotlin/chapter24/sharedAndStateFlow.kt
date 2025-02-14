package chapter24

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

//suspend fun main(): Unit = coroutineScope {
//    val mutableSharedFlow = MutableSharedFlow<String>()
//
//    launch {
//        mutableSharedFlow.collect {
//            println("#1 received $it")
//        }
//    }
//    launch {
//        mutableSharedFlow.collect {
//            println("#2 received $it")
//        }
//    }
//    delay(1000)
//    mutableSharedFlow.emit("Message1")
//    mutableSharedFlow.emit("Message2")
//}

//@OptIn(ExperimentalCoroutinesApi::class)
//suspend fun main(): Unit = coroutineScope {
//    val mutableSharedFlow = MutableSharedFlow<String>(replay = 2)
//    repeat(3) {
//        mutableSharedFlow.emit("Message${it + 1}")
//    }
//    println(mutableSharedFlow.replayCache)
//    // [Message2, Message3]
//
//    launch {
//        mutableSharedFlow.collect {
//            println("#1 received $it")
//        }
//    }
//    // #1 received Message2
//    // #1 received Message3
//
//    delay(100)
//    mutableSharedFlow.resetReplayCache()
//    println(mutableSharedFlow.replayCache)
//    // []
//}

//suspend fun main(): Unit = coroutineScope {
//    val mutableSharedFlow = MutableSharedFlow<String>()
//    val sharedFlow: SharedFlow<String> = mutableSharedFlow
//    val collector: FlowCollector<String> = mutableSharedFlow
//
//    launch {
//        mutableSharedFlow.collect {
//            println("#1 received $it")
//        }
//    }
//    launch {
//        sharedFlow.collect {
//            println("#2 received $it")
//        }
//    }
//
//    delay(1000)
//    mutableSharedFlow.emit("Message1")
//    collector.emit("Message2")
//}

//suspend fun main(): Unit = coroutineScope {
//    val flow = flowOf("A", "B", "C")
//        .onEach { delay(1000) }
//
//    val sharedFlow = flow.shareIn(
//        scope = this,
//        started = SharingStarted.Eagerly
//    )
//    delay(500)
//    launch { sharedFlow.collect { println("#1 $it") } }
//    delay(1000)
//    launch { sharedFlow.collect { println("#2 $it") } }
//    delay(1000)
//    launch { sharedFlow.collect { println("#3 $it") } }
//}

//suspend fun main(): Unit = coroutineScope {
//    val flow = flowOf("A", "B", "C")
//
//    val sharedFlow = flow.shareIn(
//        scope = this,
//        started = SharingStarted.Eagerly
//    )
//    delay(100)
//    launch {
//        sharedFlow.collect {
//            println("#1 $it")
//        }
//    }
//    println("Done!")
//}

//suspend fun main(): Unit = coroutineScope {
//    val flow1 = flowOf("A", "B", "C")
//    val flow2 = flowOf("D")
//        .onEach { delay(1000) }
//
//    val sharedFlow = merge(flow1, flow2).shareIn(
//        scope = this,
//        started = SharingStarted.Lazily,
//    )
//    delay(100)
//    launch {
//        sharedFlow.collect {
//            println("#1 $it")
//        }
//    }
//    delay(1000)
//    launch {
//        sharedFlow.collect {
//            println("#2 $it")
//        }
//    }
//}

//suspend fun main(): Unit = coroutineScope {
//    val flow = flowOf("A", "B", "C", "D")
//        .onStart { println("Started") }
//        .onCompletion { println("Finished") }
//        .onEach { delay(1000) }
//
//    val sharedFlow = flow.shareIn(
//        scope = this,
//        started = SharingStarted.WhileSubscribed(),
//    )
//    delay(3000)
//    launch {
//        println("#1 ${sharedFlow.first()}")
//    }
//    launch {
//        println("#2 ${sharedFlow.take(2).toList()}")
//    }
//    delay(3000)
//    launch {
//        println("#3 ${sharedFlow.first()}")
//    }
//}

//suspend fun main(): Unit = coroutineScope {
//    val state = MutableStateFlow("A")
//    println(state.value)
//    launch {
//        state.collect {
//            println("Value Changed to $it")
//        }
//    }
//    delay(1000)
//    state.value = "B"
//    delay(1000)
//    launch {
//        state.collect {
//            println("And now it is $it")
//        }
//    }
//    delay(1000)
//    state.value = "C"
//}

//suspend fun main(): Unit = coroutineScope {
//    val state = MutableStateFlow('C')
//    launch {
//        for (c in 'A'..'E') {
//            delay(300)
//            state.value = c
//        }
//    }
//    state.collect {
//        delay(1000)
//        println(it)
//    }
//}

suspend fun main(): Unit = coroutineScope {
    val flow = flowOf("A", "B", "C")
        .onEach { delay(1000) }
        .onEach { println("Produced $it") }

    val stateFlow = flow.stateIn(this)
    println("Listening")
    println(stateFlow.value)
    stateFlow.collect {
        println("Received $it")
    }
}
