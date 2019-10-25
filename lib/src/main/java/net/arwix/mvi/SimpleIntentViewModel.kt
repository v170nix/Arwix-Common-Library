package net.arwix.mvi

import androidx.annotation.UiThread
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class SimpleIntentViewModel<ACTION, RESULT, STATE> : ViewModel() {

    protected abstract var internalViewState: STATE
    protected abstract fun dispatchAction(action: ACTION): LiveData<RESULT>

    @UiThread
    protected abstract fun reduce(result: RESULT): STATE

    private val nextAction = MutableLiveData<ACTION>()
    private val nonCancelableResultState = MutableLiveData<STATE>()

    private val state: LiveData<STATE> = Transformations.map(
        Transformations.switchMap(nextAction, ::dispatchAction)
    ) { input: RESULT ->
        reduce(input).apply { internalViewState = this }
    }

    private val mediatorState = MediatorLiveData<STATE>()

    val liveState: LiveData<STATE> = mediatorState

    init {
        mediatorState.addSource(state, mediatorState::setValue)
        mediatorState.addSource(nonCancelableResultState, mediatorState::setValue)
    }

    @UiThread
    protected fun notificationFromObserver(result: RESULT) {
        nonCancelableResultState.value = reduce(result).apply { internalViewState = this }
    }

    @UiThread
    fun nonCancelableIntent(action: ACTION) {
        viewModelScope.launch(Dispatchers.Default) {
            dispatchAction(action)
                .asFlow()
                .collect {
                    withContext(Dispatchers.Main) {
                        nonCancelableResultState.value =
                            reduce(it).apply { internalViewState = this }
                    }
                }
        }
    }

    @UiThread
    fun intent(action: ACTION) {
        nextAction.value = action
    }

}