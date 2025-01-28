package chapter13

import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

//class SomeClass: CoroutineScope {
//    override val coroutineContext: CoroutineContext = Job()
//
//    fun onStart() {
//        launch {
//            // ...
//        }
//    }
//}

internal class ContextScope(
    context: CoroutineContext
): CoroutineScope {
    override val coroutineContext: CoroutineContext = context

    override fun toString(): String {
        return "CoroutineScope(coroutineContext=$coroutineContext"
    }
}

public fun CoroutineScope(
    context: CoroutineContext
): CoroutineScope =
    ContextScope(
        if (context[Job] != null) context
        else context + Job()
    )