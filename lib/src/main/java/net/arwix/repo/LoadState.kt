package net.arwix.repo

sealed class LoadState<R : Request>(open val request: R) {

    data class Error<R : Request> internal constructor(
        override val request: R,
        val error: Throwable,
    ) : LoadState<R>(request)

    data class StartLoading<R : Request> internal constructor(
        override val request: R
    ) : LoadState<R>(request)

    data class ProgressLoading<R : Request> internal constructor(
        override val request: R,
        val percent: Int
    ) : LoadState<R>(request)

    data class Incomplete<R : Request> internal constructor(
        override val request: R
    ) : LoadState<R>(request)

    data class Complete<R : Request, D>(
        override val request: R,
        val result: D
    ) : LoadState<R>(request)

    companion object {
        fun <R : Request> R.error(error: Throwable) = Error(this, error)
        fun <R : Request> R.start() = StartLoading(this)
        fun <R : Request> R.progress(percent: Int) = ProgressLoading(this, percent)
        fun <R : Request> R.incomplete() = Incomplete(this)
        fun <R : Request, T> R.complete(result: T) = Complete(this, result)
    }

}
