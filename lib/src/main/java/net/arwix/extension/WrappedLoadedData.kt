@file:Suppress("unused")

package net.arwix.extension

data class WrappedLoadedData<T>(
    val value: T? = null,
    val updatingState: UpdatingState = UpdatingState.None
)

sealed class UpdatingState {
    object None : UpdatingState()
    object Loading : UpdatingState()
    data class ErrorLoading(val throwable: Throwable) : UpdatingState()
    object Complete : UpdatingState()
}