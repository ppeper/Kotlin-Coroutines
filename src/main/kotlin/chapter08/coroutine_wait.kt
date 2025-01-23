package chapter08

import kotlinx.coroutines.*

suspend fun main() = coroutineScope {
//    // 빌더로 생성된 Job은
//    val job = Job()
//    println(job) // JobImpl{Active}@4f8e5cde
//    // 메서드로 완료시킬 때까지 Active 상태이다.
//    job.complete()
//    println(job) // JobImpl{Completed}@4f8e5cde
//
//    // launch는 기본적으로 Active되어 있다.
//    val activeJob = launch {
//        delay(1000)
//    }
//    println(activeJob) // StandaloneCoroutine{Active}@2a098129
//    // 여기서 잡이 완료될 때까지 기다린다.
//    activeJob.join()
//    println(activeJob) // StandaloneCoroutine{Completed}@2a098129
//
//    // launch는 New 상태로 지연 시작한다.
//    val lazyJob = launch(start = CoroutineStart.LAZY) {
//        delay(1000)
//    }
//    println(lazyJob) // LazyStandaloneCoroutine{New}@20cb9435
//    // Active 상태가 되려면 시작하는 함수를 호출해야한다.
//    lazyJob.start()
//    println(lazyJob) // LazyStandaloneCoroutine{Active}@20cb9435
//    lazyJob.join() // 1초 delay
//    println(lazyJob) // LazyStandaloneCoroutine{Completed}@20cb9435

//    launch {
//        delay(1000)
//        println("Test1")
//    }
//    launch {
//        delay(2000)
//        println("Test2")
//    }
////    val children = coroutineContext[Job]?.children
//    // 확장 프로퍼티를 사용해서 할 수 있다.
//    // 이는 coroutineContext[Job]!! 과 동일하기 때문에 지금 예시는 명시적으로 children이 있지만 없다면 error()를 반환해 주의 해야한다.
//    val children = coroutineContext.job.children
//
//    val childrenNum = children.count()
//    println("Number of children: $childrenNum")
//    children.forEach {
//        it.join()
//    }
//    println("All tests are done.")

//    val job = Job()
//    launch(job) {
//        delay(1000)
//        println("Test 1")
//    }
//    launch(job) {
//        delay(2000)
//        println("Test 2")
//    }
//    job.join()
//    println("Will not be printed")
//
//    val job = Job()
//
//    launch(job) {
//        repeat(5) {
//            delay(200)
//            println("Rep$it")
//        }
//    }
//    launch {
//        delay(500)
//        job.complete()
//    }
//    job.join()
//    launch(job) {
//        println("Will not be printed")
//    }
//    println("Done")
}