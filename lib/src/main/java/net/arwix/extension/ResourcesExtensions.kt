package net.arwix.extension

import android.content.res.Resources
import android.util.TypedValue

fun Resources.toSp(spValue: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, displayMetrics)

fun Resources.toDp(dpValue: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, displayMetrics)

fun Resources.toSp(spValue: Int) = toSp(spValue.toFloat())

fun Resources.toDp(dpValue: Int) = toDp(dpValue.toFloat())