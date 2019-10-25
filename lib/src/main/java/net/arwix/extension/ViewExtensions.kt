package net.arwix.extension

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun <T : View> T.visible(): T {
    visibility = View.VISIBLE
    return this
}

fun <T : View> T.gone(): T {
    visibility = View.GONE
    return this
}

fun <T : View> T.invisible(): T {
    visibility = View.INVISIBLE
    return this
}

fun View.hideSoftInputFromWindow() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE)?.let {
        it as InputMethodManager
    } ?: return
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}