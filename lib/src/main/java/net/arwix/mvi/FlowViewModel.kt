@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package net.arwix.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class FlowViewModel<A : FlowViewModel.Action, R, S>(
    initState: S,
    useDistinctUntilChangedInAction: Boolean = true,
    workerDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    protected open suspend fun dispatchAction(action: A): Flow<R> = emptyFlow()
    protected abstract suspend fun reduce(state: S, result: R): S

    private val latestActionChannel = Channel<A>(Channel.CONFLATED)
    private val mergeActionChannel = Channel<A>(Channel.UNLIMITED)
    private val concatActionChannel = Channel<A>(Channel.UNLIMITED)
    private val resultChannel = Channel<R>(Channel.UNLIMITED)

    private val actionToResultFlow: Flow<R> = channelFlow {
        launch(coroutineContext + SupervisorJob()) {
            latestActionChannel
                .consumeAsFlow()
                .flatMapLatest { dispatchAction(it) }
                .collect(this@channelFlow::send)
        }
        launch(coroutineContext + SupervisorJob()) {
            mergeActionChannel
                .consumeAsFlow()
                .flatMapMerge { dispatchAction(it) }
                .collect(this@channelFlow::send)
        }

        launch(coroutineContext + SupervisorJob()) {
            concatActionChannel
                .consumeAsFlow()
                .transform { this.emitAll(dispatchAction(it)) }
                .collect(this@channelFlow::send)
        }
        launch(coroutineContext + SupervisorJob()) {
            resultChannel.consumeAsFlow().collect(this@channelFlow::send)
        }
        awaitClose {
            concatActionChannel.close()
            latestActionChannel.close()
            mergeActionChannel.close()
            resultChannel.close()
        }
    }

    protected val errorState: MutableStateFlow<Throwable?>  = MutableStateFlow(null)
    private val _state = MutableStateFlow(initState)
    val state = _state.asStateFlow()

    init {
        actionToResultFlow
            .run { if (useDistinctUntilChangedInAction) distinctUntilChanged() else this }
            .onEach {
                reduce(_state.value, it)
                    .also { state -> _state.value = state }
            }
            .catch { e -> errorState.value = e }
            .flowOn(workerDispatcher)
            .launchIn(viewModelScope)
    }

    protected fun nextResult(result: R) = resultChannel.offer(result)

    fun nextSyncAction(action: A): Boolean {
        if (action.type != ActionType.Sync) throw IllegalStateException()
        return concatActionChannel.offer(action)
    }

    fun nextLatestAction(action: A): Boolean {
        if (action.type != ActionType.Latest) throw IllegalStateException()
        return latestActionChannel.offer(action)
    }

    fun nextMergeAction(action: A): Boolean {
        if (action.type != ActionType.Merge) throw IllegalStateException()
        return mergeActionChannel.offer(action)
    }

    enum class ActionType { Latest, Merge, Sync }

    abstract class Action {
        abstract val type: ActionType
    }

    open class LatestAction : Action() {
        override val type: ActionType
            get() = ActionType.Latest
    }

    open class MergeAction : Action() {
        override val type: ActionType
            get() = ActionType.Merge
    }

    open class SyncAction : Action() {
        override val type: ActionType
            get() = ActionType.Sync
    }

}