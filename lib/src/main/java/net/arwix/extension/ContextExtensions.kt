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