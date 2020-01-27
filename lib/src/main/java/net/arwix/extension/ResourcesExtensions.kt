package net.arwix.extension

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue

fun Resources.getCompatColor(id: Int, theme: Resources.Theme) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.getColor(id, theme)
    } else {
        this.getColor(id)
    }

fun Resources.getCompatDrawable(id: Int, theme: Resources.Theme): Drawable =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        this.getDrawable(id, theme)
    } else {
        this.getDrawable(id)
    }

fun Resources.getColorStateListCompat(id: Int, theme: Resources.Theme): ColorStateList =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.getColorStateList(id, theme)
    } else {
        this.getColorStateList(id)
    }

fun Resources.toSp(spValue: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, displayMetrics)

fun Resources.toDp(dpValue: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, displayMetrics)

fun Resources.toSp(spValue: Int) = toSp(spValue.toFloat())

fun Resources.toDp(dpValue: Int) = toDp(dpValue.toFloat())