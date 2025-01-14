package com.ppeper.chapter03

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class User(
    val name: String
)
private val executor =
    Executors.newSingleThreadScheduledExecutor {
        Thread(it, "scheduler").apply { isDaemon = true }
    }
suspend fun delay(timeMillis: Long) =
    suspendCoroutine { cont ->
        executor.schedule({
            cont.resume(Unit)
        }, timeMillis, TimeUnit.MILLISECONDS)
    }

fun requestUser(callback: (User) -> Unit) {
    callback(User(name = "Text"))
}

suspend fun requestUser(): User {
    return suspendCoroutine { continuation ->
        requestUser { user ->
            continuation.resume(user)
        }
    }
}

class MyException: Throwable("Just an Exception")

suspend fun main() {
//    println("Before")
//
//    suspendCoroutine<Unit> {  }
//
//    println("After")

//    println("Before")
//
//    suspendCoroutine { continuation ->
//        continuation.resume(Unit)
//    }
//
//    println("After")

//    println("Before")
//
//    // delay 의 원형도 위에 해당하는 로직과 핵심코드는 거의 똑같다
//    delay(1000L)
//
//    println("After")

//    println("Before")
//    val user = suspendCoroutine { continuation ->
//        requestUser { user ->
//            continuation.resume(user)
//        }
//    }
//    println(user)
//    println("After")

//    println("Before")
//    val user = requestUser()
//    println(user)
//    println("After")
    try {
        suspendCoroutine<Unit> {  continuation ->
            continuation.resumeWithException(MyException())
        }
    } catch (e: MyException) {
        println("Caught!")
    }
}

