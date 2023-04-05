@file:Suppress("unused")

package net.arwix.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@Deprecated("use standard collect", ReplaceWith("collect"))
suspend inline fun <T> Flow<T>.safeCollect(crossinline action: suspend (value: T) -> Unit): Unit =
    collect {
        try {
            action(it)
        } catch (e: CancellationException) {

        }
    }

@Deprecated("use standard launch", ReplaceWith("launch"))
fun <T> Flow<T>.safeLaunchIn(scope: CoroutineScope) = scope.launch {
    this@safeLaunchIn.safeCollect {  }
}

fun <T, V> Flow<T>.mapDistinct(mapper: suspend (T) -> V): Flow<V> =
    map(mapper).distinctUntilChanged()

