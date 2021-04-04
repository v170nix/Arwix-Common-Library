@file:Suppress("unused")

package net.arwix.extension

import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.coroutineContext

fun <E> SendChannel<E>.tryOffer(element: E): Boolean {
    return runCatching { offer(element) }.getOrDefault(false)
}

fun <E> SendChannel<E>.trySendBlocking(element: E) {
    if (tryOffer(element)) return
    runBlocking {
        send(element)
    }
}

fun <T, V> Flow<T>.mapDistinct(mapper: suspend (T) -> V): Flow<V> = map(mapper).distinctUntilChanged()

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
        job?.cancel()
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