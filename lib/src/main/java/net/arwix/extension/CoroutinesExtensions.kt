package net.arwix.extension

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking

fun <E> SendChannel<E>.tryOffer(element: E): Boolean {
    return runCatching { offer(element) }.getOrDefault(false)
}

fun <E> SendChannel<E>.trySendBlocking(element: E) {
    if (tryOffer(element)) return
    runBlocking {
        send(element)
    }
}