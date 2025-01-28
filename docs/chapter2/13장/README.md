# 13장 코루틴 스코프 만들기

### 📌 CoroutineScope Factory 함수

- CoroutineScope는 coroutineContext를 유일한 프로퍼티로 가지고 있는 인터페이스 이다.

```kotlin
public interface CoroutineScope {
    /**
     * The context of this scope.
     * Context is encapsulated by the scope and used for implementation of coroutine builders that are extensions on the scope.
     * Accessing this property in general code is not recommended for any purposes except accessing the [Job] instance for advanced usages.
     *
     * By convention, should contain an instance of a [job][Job] to enforce structured concurrency.
     */
    public val coroutineContext: CoroutineContext
}
```

- 해당 인터페이스를 구현한 클래스를 만들고 내부에서 코루틴 빌더를 직접 호출할 수 있다. 하지만 이런 방법은 자주 사용되지 않는다.

```kotlin
class SomeClass: CoroutineScope {
    override val coroutineContext: CoroutineContext = Job()

    fun onStart() {
        launch { 
            // ...
        }
    }
}
```

- 이는 편리해보이지만 CoroutineScope를 구현한 클래스에서 cancel, ensureActive 같은 다른 CoroutineScope의 메서드를 직접 호출하면 문제가 발생할 수 있다.
- 갑자기 전체 스코프를 취소하면 코루틴이 더 이상 시작될 수 없다. 대신 코루틴 스코프 인스턴스를 프로퍼티로 가지고 있다가 코루틴 빌더를 호출할 때 사용하는 방법이 선호된다.

```kotlin
class SomeClass {
    val scope: CoroutineScope = ...
            
    fun onStart() {
        scope.launch {
            // ...
        }
    }
}
```

- 코루틴 스코프 객체를 만드는 가장 쉬운 방법은 CoroutineScope 팩토리 함수를 사용하는 것이다. 이 함수는 Context를 넘겨 받아 스코프를 만든다.

```kotlin
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
```

### 📌 안드로이드에서 스코프 만들기

- 안드로이드에서는 MVP, MVVM 아키첵처를 많이 사용하고 있다. 이러한 아키텍처에서는 Presenters나 ViewModels 에서 사용자에서 보여주는 부분을 추출한다.
- BaseViewModel에서 스코프를 만들면, 모든 뷰 모델에서 쓰일 스코프를 단 한번으로 정의한다. 따라서 MainViewModel에서는 BaseViewModel의 scope 프로퍼티를 사용하기만 하면 된다.

```kotlin
abstract class BaseViewModel: ViewModel() {
    protected val scope = CoroutineScope(TODO())
}
```

- 기본 Base 스코프에서 Context를 정의해 보자. 안드로이드에서는 메인 스레드가 많은 수의 함수를 호출해야 하므로 기본 디스패처를 Dispatchers.Main으로 정하는 것이 가장 좋다.
- 다음으로 스코프를 취소 간으하기 만들어야 한다. ViewModel class의 onClear() 에서 스코프를 취소할 수 있다.
- 해당 스코프에서 시작된 각각의 코루틴이 독립적으로 작동해야하는 경우가 있다. 이럴때는 SupervisorJob을 사용해야 한다.

```kotlin
abstract class BaseViewModel: ViewModel() {
    protected val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    override fun onCleard() {
        scope.coroutineContext.cancelChildren()
    }
}
```

- 잡히지 않는 예외가 있는경우 CoroutineExceptionHandler를 사용해 해당 함수를 호출할 수 있다.

```kotlin
		
abstract class BaseViewModel: ViewModel() {
    private val _failure: MutableLiveData<Throwable> = MutableLiveData()
    val failure: LiveData<Throwable> = _failure
    
    private val exceptionHandler = 
        CoroutineExceptionHandler { _, throwable ->
                _failure.value = throwable
        }
				
    private val context = Dispatchers.Main + SupervisorJob() + exceptionHandler
    
    protexted val scope = CoroutineScope(context)
    
    override fun onCleard() {
        context.cancelChildren()
    }
}
```

### 📌 viewModelScope, lifecycleScope

- 최근 안드로이드에서는 애플리케이션 스코프를 따로 정의하는 대신에 `viewModelScope` 또는 `lifecycleScope` 를 사용할 수 있다. Dispatchers.Main과 SupervisorJob를 사용하고 ViewModel이나 Lifecycle이 종료되었을 때 Job을 취소시킨다는 점에서 우리가 만든 스코프와 거의 동일하다고 볼 수 있다.

```kotlin
// lifecycle-viewmodel-ktx 2.4.0 버전에서 구현된 방식
public val ViewModel.viewModelScope: CoroutineScope
    get() {
        val scope: CoroutineScope? = this.getTag(JOB_KEY)
        if (scope != null) {
            return scope
        }
        return setTagIfAbsent(
            JOB_KEY,
            CloseableCoroutineScope(
                SupervisorJob() +
                    Dispatchers.Main.immediate
            )
        )
    }
internal class CloseableCoroutineScope(
		context: CoroutineContext
): Closable, CoroutineScope {
    override val coroutineContext: CoroutineContext = context
    
    override fun close() {
        coroutineContext.cancel()
    }
}
```

- 스코프에서 CoroutineExceptionHandler와 같은 특정 Context가 필요없다면 viewModelScope와 lifecycleScope를 사용하는 것이 편리하고 더 좋다. 편리성 때문에 수많은 안드로이드 애플리케이션이 이 스코프를 사용하고 있다.

### 📌 추가적인 호출을 위한 스코프 만들기

- 추가적인 연산을 시작하기 위한 스코프를 종종 만들곤 한다. 이런 스코프는 함수나 생성자의 인자를 통해 주로 주입된다.  스코프를 위해 원하는 Scope를 설정하면 된다.

```kotlin
val analyticsScope = CoroutineScope(SupervisorJob())

private val exceptionHandler =
    CoroutineExceptionHandler { _, throwable ->
        FirebaseCrashlytics.getInstance()
            .recordException(throwable)
    }
			
	val analyticsScope = CoroutineScope(
        SupervisorJob() + exceptionHandler
	)
```