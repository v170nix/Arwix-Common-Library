package net.arwix.extension

import java.lang.ref.WeakReference

fun <T> T.weak() = WeakReference<T>(this)

inline fun <T, R> WeakReference<T>.runWeak(block: T.() -> R): R? {
    return this.get()?.run(block)
}
