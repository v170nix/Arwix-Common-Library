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
import net.arwix.extension.safeCollect
import net.arwix.extension.safeLaunchIn

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
                .safeCollect(this@channelFlow::send)
        }
        launch(coroutineContext + SupervisorJob()) {
            mergeActionChannel
                .consumeAsFlow()
                .flatMapMerge { dispatchAction(it) }
                .safeCollect(this@channelFlow::send)
        }

        launch(coroutineContext + SupervisorJob()) {
            concatActionChannel
                .consumeAsFlow()
                .transform { this.emitAll(dispatchAction(it)) }
                .safeCollect(this@channelFlow::send)
        }
        launch(coroutineContext + SupervisorJob()) {
            resultChannel.consumeAsFlow().safeCollect(this@channelFlow::send)
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
            .safeLaunchIn(viewModelScope)
    }

    protected fun onResult(result: R) = resultChannel.trySend(result).isSuccess

    fun onSyncAction(action: A): Boolean {
        if (action.type != ActionType.Sync) throw IllegalStateException()
        return concatActionChannel.trySend(action).isSuccess
    }

    fun onLatestAction(action: A): Boolean {
        if (action.type != ActionType.Latest) throw IllegalStateException()
        return latestActionChannel.trySend(action).isSuccess
    }

    fun onMergeAction(action: A): Boolean {
        if (action.type != ActionType.Merge) throw IllegalStateException()
        return mergeActionChannel.trySend(action).isSuccess
    }

    enum class ActionType { Latest, Merge, Sync }

    interface Action {
        val type: ActionType
    }

    interface LatestAction : Action {
        override val type: ActionType
            get() = ActionType.Latest
    }

    interface MergeAction : Action {
        override val type: ActionType
            get() = ActionType.Merge
    }

    interface SyncAction : Action {
        override val type: ActionType
            get() = ActionType.Sync
    }

}