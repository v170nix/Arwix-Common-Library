package net.arwix.extension

import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Like Delegates.observable except it only calls the callback when the value actually changes.
 */
public inline fun <T> uniqueObservable(
    initialValue: T,
    emitInitial: Boolean = false,
    crossinline onChange: (value: T) -> Unit
): ReadWriteProperty<Any?, T> {
    if (emitInitial) onChange(initialValue)
    return object : ObservableProperty<T>(initialValue) {
        override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
            if (oldValue != newValue) onChange(newValue)
        }
    }
}