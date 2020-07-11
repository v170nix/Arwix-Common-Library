package net.arwix.extension

import android.graphics.Paint
import android.graphics.Rect

fun Paint.getTextRect(text: String): Rect {
    val bounds = Rect()
    getTextBounds(text, 0, text.length, bounds)
    return bounds
}