package net.arwix.extension

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
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

fun Context.createNotificationChannel(id: String, createChannel: (id: String) -> NotificationChannel) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
    val channel = manager.getNotificationChannel(id)
    if (channel != null) return
    manager.createNotificationChannel(createChannel(id))
}