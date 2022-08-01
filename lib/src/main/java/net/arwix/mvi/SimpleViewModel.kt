@file:Suppress("unused")

package net.arwix.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface UIState

interface UIEvent

interface UISideEffect

interface EventHandler<E> {
    fun doEvent(event: E)
}

abstract class SimpleViewModel<E : UIEvent, S : UIState, F : UISideEffect>(initState: S) :
    ViewModel(), EventHandler<E> {

    private val _viewState = MutableStateFlow(initState)
    val state: StateFlow<S> = _viewState

    private val _event = MutableSharedFlow<E>()

    private val _effect: Channel<F> = Channel(Channel.UNLIMITED)

    /**
     *  use
     *  LaunchedEffect(SimpleViewModel.SIDE_EFFECT_LAUNCH_ID) {
     *      effect.onEach {
     *      ...
     *      }
     *     .launchIn(this + Dispatchers.Main.immediate)
     *  }
     */
    val effect = _effect.receiveAsFlow()

    init {
        subscribeToEvents()
    }

    override fun doEvent(event: E) {
        viewModelScope.launch { _event.emit(event) }
    }

    protected fun reduceState(reducer: S.() -> S) {
        _viewState.update(reducer)
    }

    private fun subscribeToEvents() {
        viewModelScope.launch {
            _event.collect {
                handleEvents(it)
            }
        }
    }

    protected abstract fun handleEvents(event: E)

    protected fun applyEffect(builder: () -> F) {
        val effectValue = builder()
        // https://github.com/Kotlin/kotlinx.coroutines/issues/2886
        viewModelScope.launch(Dispatchers.Main.immediate) { _effect.trySend(effectValue) }
    }

    companion object {
        const val SIDE_EFFECT_LAUNCH_ID = "side-effect-launch-id"
    }
}