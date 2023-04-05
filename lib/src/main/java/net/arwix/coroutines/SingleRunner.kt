package net.arwix.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Class which guarantees single execution of blocks passed to [runInIsolation] by cancelling the
 * previous call. [runInIsolation] is backed by a [Mutex], which is fair, so concurrent callers of
 * [runInIsolation] will trigger in order, with the last call winning (by cancelling previous calls)
 *
 * When priorities are used, if the currently running block has a higher priority, the new one is
 * cancelled. If the currently running block has lower priority, currently running block is
 * cancelled.
 * If they have equal priority:
 *  * if cancelPreviousInEqualPriority == true, existing block is cancelled
 *  * if cancelPreviousInEqualPriority == false, new block is cancelled
 *
 * Note: When a block is cancelled, the outer scope (which called runInIsolation) is NOT cancelled.
 */
class SingleRunner(
    cancelPreviousInEqualPriority: Boolean = true
) {
    private val holder = Holder(this, cancelPreviousInEqualPriority)

    suspend fun runInIsolation(
        priority: Int = DEFAULT_PRIORITY,
        block: suspend () -> Unit
    ) {
        try {
            coroutineScope {
                val thisJob = checkNotNull(coroutineContext[Job]) {
                    "Internal error. coroutineScope should've created a job."
                }
                val run = holder.tryEnqueue(
                    priority = priority,
                    job = thisJob
                )
                if (run) {
                    try {
                        block()
                    } finally {
                        holder.onFinish(thisJob)
                    }
                }

            }
        } catch (cancelIsolatedRunner: CancelIsolatedRunnerException) {
            if (cancelIsolatedRunner.runner !== this@SingleRunner) {
                throw cancelIsolatedRunner
            }
        }
    }

    /**
     * Exception which is used to cancel previous instance of an isolated runner.
     * We use this special class so that we can still support regular cancelation coming from the
     * `block` but don't cancel its coroutine just to cancel the block.
     */
    private class CancelIsolatedRunnerException(val runner: SingleRunner) : CancellationException()

    private class Holder(
        private val singleRunner: SingleRunner,
        private val cancelPreviousInEqualPriority: Boolean
    ) {
        private val mutex = Mutex()
        private var previous: Job? = null
        private var previousPriority: Int = 0

        suspend fun tryEnqueue(
            priority: Int,
            job: Job
        ): Boolean {
            mutex.withLock {
                val prev = previous
                return if (prev == null ||
                    !prev.isActive ||
                    previousPriority < priority ||
                    (previousPriority == priority && cancelPreviousInEqualPriority)
                ) {
                    prev?.cancel(CancelIsolatedRunnerException(singleRunner))
                    prev?.join()
                    previous = job
                    previousPriority = priority
                    true
                } else {
                    false
                }
            }
        }

        suspend fun onFinish(job: Job) {
            mutex.withLock {
                if (job === previous) previous = null
            }
        }
    }

    companion object {
        const val DEFAULT_PRIORITY = 0
    }
}