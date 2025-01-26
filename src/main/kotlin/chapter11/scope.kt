package chapter11

import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

data class Details(
    val name: String,
    val followers: Int
)
data class Tweet(
    val text: String
)

fun getFollowersNumber(): Int = throw Error("Service exception")

suspend fun getUserName(): String {
    delay(500)
    return "ppeper"
}

suspend fun getTweets(): List<Tweet> {
    return listOf(Tweet("Hello, Coroutines"))
}

suspend fun CoroutineScope.getUserDetails(): Details {
    val userName = async { getUserName() }
    val followersNumber = async { getFollowersNumber() }
    return Details(userName.await(), followersNumber.await())
}

suspend fun longTask() = coroutineScope {
    launch {
        delay(1000)
        val name = coroutineContext[CoroutineName]?.name
        println("[$name] Finished task 1")
    }
    launch {
        delay(2000)
        val name = coroutineContext[CoroutineName]?.name
        println("[$name] Finished task 2")
    }
}

fun CoroutineScope.log(text: String) {
    val name = coroutineContext[CoroutineName]?.name
    println("[$name] $text")
}

suspend fun test(): Int = withTimeout(1500) {
    delay(1000)
    println("Still thinking")
    delay(1000)
    println("Done!")
    42
}

fun main(): Unit = runBlocking(CoroutineName("Main")) {
//    val details = try {
//        getUserDetails()
//    } catch (e: Error) {
//        null
//    }
//    val tweets = async { getTweets() }
//    println("User: $details")
//    println("Tweets: ${tweets.await()}")

//    val a = coroutineScope {
//        delay(1000)
//        10
//    }
//    println("a is calculated")
//    val b = coroutineScope {
//        delay(1000)
//        20
//    }
//    println(a)
//    println(b)

//    println("Before")
//    longTask()
//    println("After")
//
//    val job = launch(CoroutineName("Parent")) {
//        longTask()
//    }
//    delay(1500)
//    job.cancel()

//    log("Before")
//
//    withContext(CoroutineName("Child 1")) {
//        delay(1000)
//        log("Hello 1")
//    }
//
//    withContext(CoroutineName("Child 2")) {
//        delay(1000)
//        log("Hello 2")
//    }
//
//    log("After")

    try {
        test()
    } catch (e: TimeoutCancellationException) {
        println("Cancelled")
    }
    delay(1000)

}