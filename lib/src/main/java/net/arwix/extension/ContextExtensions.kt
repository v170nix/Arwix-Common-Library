package net.arwix.extension

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt

@ColorInt
fun Context.getThemeColor(@AttrRes attr: Int) = TypedValue().also {
    theme.resolveAttribute(attr, it, true)
}.data

fun Context.getString(identifierKey: String): String =
    this.getString(resources.getIdentifier(identifierKey, "string", packageName))

fun Context.resolveAttribute(@AttrRes attributeResId: Int): Int {
    val typedValue = TypedValue()
    if (theme.resolveAttribute(attributeResId, typedValue, true))
        return typedValue.data
    throw IllegalArgumentException(resources.getResourceName(attributeResId))
}