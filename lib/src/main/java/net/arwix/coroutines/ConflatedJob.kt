@file:Suppress("unused")

package net.arwix.coroutines

import kotlinx.coroutines.Job
import kotlin.coroutines.coroutineContext

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