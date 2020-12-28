package net.arwix.extension

import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat

fun Drawable.animatedVectorStart(): Boolean {
    return when (this) {
        is AnimatedVectorDrawableCompat -> {
            start()
            true
        }
        is AnimatedVectorDrawable -> {
            start()
            true
        }
        else -> false
    }
}