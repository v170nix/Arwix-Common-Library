package net.arwix.extension

import androidx.annotation.RestrictTo
import androidx.collection.ArrayMap
import androidx.collection.SparseArrayCompat
import androidx.collection.arrayMapOf
import androidx.collection.forEach


fun <K, V> Map<K, V>.toArrayMap() = ArrayMap<K, V>(this.size).let {
    it.putAll(this)
    it
}

inline fun <K, V, R> ArrayMap<out K, V>.arrayMapValues(transform: (Map.Entry<K, V>) -> R): ArrayMap<K, R> {
    return mapValuesTo(ArrayMap(size), transform)
}

inline fun <K, V, R> Map<out K, V>.arrayMapValues(transform: (Map.Entry<K, V>) -> R): ArrayMap<K, R> {
    return mapValuesTo(ArrayMap(size), transform)
}

inline fun <K, V, R> ArrayMap<out K, V>.arrayMapKeys(transform: (Map.Entry<K, V>) -> R): ArrayMap<R, V> {
    return mapKeysTo(ArrayMap(size), transform)
}

inline fun <K, V, R> Map<out K, V>.arrayMapKeys(transform: (Map.Entry<K, V>) -> R): ArrayMap<R, V> {
    return mapKeysTo(ArrayMap(size), transform)
}

inline fun <K, V> ArrayMap<out K, V>.filterKeys(predicate: (K) -> Boolean): ArrayMap<K, V> {
    val result = ArrayMap<K, V>()
    for (entry in this) {
        if (predicate(entry.key)) {
            result[entry.key] = entry.value
        }
    }
    return result
}

inline fun <K, V> ArrayMap<out K, V>.filterValues(predicate: (V) -> Boolean): ArrayMap<K, V> {
    val result = ArrayMap<K, V>()
    for (entry in this) {
        if (predicate(entry.value)) {
            result[entry.key] = entry.value
        }
    }
    return result
}

public inline fun <K, V> ArrayMap<out K, V>.filter(predicate: (Map.Entry<K, V>) -> Boolean): ArrayMap<K, V> {
    return filterTo(ArrayMap(), predicate)
}

public inline fun <K, V> ArrayMap<out K, V>.filterNot(predicate: (Map.Entry<K, V>) -> Boolean): ArrayMap<K, V> {
    return filterNotTo(ArrayMap(), predicate)
}

public fun <K, V> Array<out Pair<K, V>>.toArrayMap(): ArrayMap<K, V> = when (size) {
    0 -> arrayMapOf()
    1 -> arrayMapOf(this[0])
    else -> toMap(ArrayMap(size))
}

/**
 * Transform [SparseArrayCompat<E>][SparseArrayCompat] to [Map<Int,E>][Map]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <E> SparseArrayCompat<E>.asMap(): Map<Int, E> {
    val map = HashMap<Int, E>()
    forEach { key, value ->
        map[key] = value
    }
    return map
}
