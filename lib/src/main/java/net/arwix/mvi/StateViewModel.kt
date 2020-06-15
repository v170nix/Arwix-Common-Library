package net.arwix.mvi

import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.concurrent.Executors

abstract class StateViewModel<A : StateViewModel.Action, R, S> : ViewModel() {

    protected abstract var internalViewState: S
    private val latestActionChannel = Channel<A>(Channel.CONFLATED)
    private val mergeActionChannel = Channel<A>(Channel.UNLIMITED)
    private val concatActionChannel = Channel<A>(Channel.UNLIMITED)
    private val resultChannel = Channel<R>(Channel.UNLIMITED)

    private val fetchDispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

    protected open suspend fun dispatchAction(action: A): Flow<R> = emptyFlow()
    protected abstract suspend fun reduce(state: S, result: R): S

    private val concatResultFlow: Flow<R> = concatActionChannel
        .consumeAsFlow()
        .transform { this.emitAll(dispatchAction(it)) }

    private val resultFlow: Flow<R> = resultChannel.consumeAsFlow()

    private val actionToResultFlow: Flow<R> = channelFlow {
        launch(coroutineContext + SupervisorJob()) {
            latestActionChannel
                .consumeAsFlow()
                .flatMapLatest { dispatchAction(it) }
                .collect(::send)
        }
        launch(coroutineContext + SupervisorJob()) {
            mergeActionChannel
                .consumeAsFlow()
                .flatMapMerge { dispatchAction(it) }
                .collect(::send)
        }

        launch(coroutineContext + SupervisorJob()) {
            concatResultFlow.collect(::send)
        }
        launch(coroutineContext + SupervisorJob()) {
            resultChannel.consumeAsFlow().collect(::send)
        }
        awaitClose {
            concatActionChannel.close()
            latestActionChannel.close()
            mergeActionChannel.close()
            resultChannel.close()
        }
    }

    private val _state = MutableLiveData<S>()
    val state: LiveData<S> = _state

    init {
        actionToResultFlow
            .onEach {
                reduce(internalViewState, it).also { state ->
                    if (state != internalViewState) {
                        internalViewState = state
                        withContext(Dispatchers.Main) { _state.value = state }
                    }
                }
            }
            .onStart { internalViewState }
            .distinctUntilChanged()
            .flowOn(fetchDispatcher)
            .launchIn(viewModelScope)
    }

    protected fun nextAction(action: A) {
        when (action.type) {
            ActionType.Latest -> latestActionChannel.offer(action)
            ActionType.Merge -> mergeActionChannel.offer(action)
            ActionType.Sync -> concatActionChannel.offer(action)
        }
    }

    fun nextSyncAction(action: A) {
        if (action.type != ActionType.Sync) throw IllegalStateException()
        concatActionChannel.offer(action)
    }

    fun nextLatestAction(action: A) {
        if (action.type != ActionType.Latest) throw IllegalStateException()
        latestActionChannel.offer(action)
    }

    fun nextMergeAction(action: A) {
        if (action.type != ActionType.Merge) throw IllegalStateException()
        mergeActionChannel.offer(action)
    }

    protected fun nextResult(result: R) = resultChannel.offer(result)

    enum class ActionType { Latest, Merge, Sync }

    abstract class Action {
        abstract val type: ActionType
    }

    override fun onCleared() {
        fetchDispatcher.cancel()
        super.onCleared()
    }
}

class OneEvent<T>(private var data: T?, private var count: Int = 1) {
    @UiThread
    fun getDataOrNull(): T? {
        count--
        if (count < 0) data = null
        return data
    }
}