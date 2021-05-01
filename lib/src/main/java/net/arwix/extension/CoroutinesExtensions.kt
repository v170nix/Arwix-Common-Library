@file:Suppress("unused")

package net.arwix.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

fun <E> SendChannel<E>.tryOffer(element: E): Boolean {
    return runCatching {
        offer(element)
    }.getOrDefault(false)
}

fun <E> SendChannel<E>.trySendBlocking(element: E) {
    if (tryOffer(element)) return
    runBlocking {
        send(element)
    }
}

suspend inline fun <T> Flow<T>.safeCollect(crossinline action: suspend (value: T) -> Unit): Unit =
    collect {
        try {
            action(it)
        } catch (e: CancellationException) { }
    }

fun <T> Flow<T>.safeLaunchIn(scope: CoroutineScope) = scope.launch {
    this@safeLaunchIn.safeCollect {  }
}

fun <T, V> Flow<T>.mapDistinct(mapper: suspend (T) -> V): Flow<V> =
    map(mapper).distinctUntilChanged()

/**
 * Job container that will cancel the previous job if a new one is set.
 *
 * Assign the new job with the += operator.
 */
@Suppress("MemberVisibilityCanBePrivate")
class ConflatedJob {

    private var job: Job? = null
    private var prevJob: Job? = null

    val isActive get() = job?.isActive ?: false

    @Synchronized
    operator fun plusAssign(newJob: Job) {
        cancel()
        job = newJob
    }

    fun cancel() {
        if (job?.isActive == true) job?.cancel()
        prevJob = job
    }

    fun start() {
        job?.start()
    }

    /**
     * This can be used inside newly started job to await completion of previous job.
     */
    suspend fun joinPreviousJob() {
        val thisJob = coroutineContext[Job]
        val jobToJoin = synchronized(this) { if (job == thisJob) prevJob else job }
        jobToJoin?.join()
    }
}