package chapter12

import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import java.util.concurrent.Executors

@OptIn(ExperimentalCoroutinesApi::class)
class SomeTest {

    private val dispatcher = Executors
        .newSingleThreadExecutor()
        .asCoroutineDispatcher()

    @Before
    fun setup () {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        // 메인 디스패처를 원래의 Main 디스패처로 되돌린다.
        Dispatchers.resetMain()
        dispatcher.close()
    }

    fun testSomeUI() = runBlocking {
        launch(Dispatchers.Main) {
            // ...
        }
    }
}