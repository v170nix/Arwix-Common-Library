@file:Suppress("unused")

package net.arwix.extension

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import kotlin.reflect.KClass

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

@Deprecated("use createNotificationChannels")
fun Context.createNotificationChannel(id: String, createChannel: (id: String) -> NotificationChannel) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
    val channel = manager.getNotificationChannel(id)
    if (channel != null) return
    manager.createNotificationChannel(createChannel(id))
}

@SuppressLint("InlinedApi")
@IntDef(
    flag = true,
    value = [
        PendingIntent.FLAG_ONE_SHOT,
        PendingIntent.FLAG_NO_CREATE,
        PendingIntent.FLAG_CANCEL_CURRENT,
        PendingIntent.FLAG_UPDATE_CURRENT,
        PendingIntent.FLAG_IMMUTABLE,

        Intent.FILL_IN_ACTION,
        Intent.FILL_IN_DATA,
        Intent.FILL_IN_CATEGORIES,
        Intent.FILL_IN_COMPONENT,
        Intent.FILL_IN_PACKAGE,
        Intent.FILL_IN_SOURCE_BOUNDS,
        Intent.FILL_IN_SELECTOR,
        Intent.FILL_IN_CLIP_DATA
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class PengingIntentFlags

fun Context.activityPendingIntent(
    requestCode: Int,
    intent: Intent,
    @PengingIntentFlags flags: Int = PendingIntent.FLAG_CANCEL_CURRENT,
    options: Bundle? = null
): PendingIntent {
    return PendingIntent.getActivity(this, requestCode, intent, flags, options)
}

fun Context.activityPendingIntent(
    requestCode: Int,
    activityClass: Class<out Activity>,
    @PengingIntentFlags flags: Int = PendingIntent.FLAG_CANCEL_CURRENT,
    options: Bundle? = null
): PendingIntent {
    return activityPendingIntent(requestCode, Intent(this, activityClass), flags, options)
}

fun Context.activityPendingIntent(
    requestCode: Int,
    activityClass: KClass<out Activity>,
    @PengingIntentFlags flags: Int = PendingIntent.FLAG_CANCEL_CURRENT,
    options: Bundle? = null
): PendingIntent {
    return activityPendingIntent(requestCode, activityClass.java, flags, options)
}

inline fun <reified T : Activity> Context.activityPendingIntent(
    requestCode: Int,
    @PengingIntentFlags flags: Int = PendingIntent.FLAG_CANCEL_CURRENT,
    options: Bundle? = null
): PendingIntent {
    return activityPendingIntent(requestCode, T::class, flags, options)
}

fun Context.activitiesPendingIntent(
    requestCode: Int,
    intents: Array<Intent>,
    @PengingIntentFlags flags: Int = PendingIntent.FLAG_CANCEL_CURRENT,
    options: Bundle? = null
): PendingIntent {
    return PendingIntent.getActivities(this, requestCode, intents, flags, options)
}

fun Context.broadcastPendingIntent(
    requestCode: Int,
    intent: Intent,
    flags: Int = PendingIntent.FLAG_CANCEL_CURRENT
): PendingIntent {
    return PendingIntent.getBroadcast(this, requestCode, intent, flags)
}

fun Context.broadcastPendingIntent(
    requestCode: Int,
    broadcastReceiverClass: Class<out BroadcastReceiver>,
    flags: Int = PendingIntent.FLAG_CANCEL_CURRENT
): PendingIntent {
    return broadcastPendingIntent(requestCode, Intent(this, broadcastReceiverClass), flags)
}

fun Context.broadcastPendingIntent(
    requestCode: Int,
    broadcastReceiverClass: KClass<out BroadcastReceiver>,
    flags: Int = PendingIntent.FLAG_CANCEL_CURRENT
): PendingIntent {
    return broadcastPendingIntent(requestCode, broadcastReceiverClass.java, flags)
}

inline fun <reified T : BroadcastReceiver> Context.broadcastPendingIntent(
    requestCode: Int,
    flags: Int = PendingIntent.FLAG_CANCEL_CURRENT
): PendingIntent {
    return broadcastPendingIntent(requestCode, T::class, flags)
}

fun Context.servicePendingIntent(
    requestCode: Int,
    intent: Intent,
    flags: Int = PendingIntent.FLAG_CANCEL_CURRENT
): PendingIntent {
    return PendingIntent.getService(this, requestCode, intent, flags)
}

fun Context.servicePendingIntent(
    requestCode: Int,
    serviceClass: Class<out Service>,
    flags: Int = PendingIntent.FLAG_CANCEL_CURRENT
): PendingIntent {
    return servicePendingIntent(requestCode, Intent(this, serviceClass), flags)
}

fun Context.servicePendingIntent(
    requestCode: Int,
    serviceClass: KClass<out Service>,
    flags: Int = PendingIntent.FLAG_CANCEL_CURRENT
): PendingIntent {
    return servicePendingIntent(requestCode, serviceClass.java, flags)
}

inline fun <reified T : Service> Context.servicePendingIntent(
    requestCode: Int,
    flags: Int = PendingIntent.FLAG_CANCEL_CURRENT
): PendingIntent {
    return servicePendingIntent(requestCode, T::class, flags)
}

@RequiresApi(Build.VERSION_CODES.O)
fun Context.foregroundServicePendingIntent(
    requestCode: Int,
    intent: Intent,
    flags: Int = PendingIntent.FLAG_CANCEL_CURRENT
): PendingIntent {
    return PendingIntent.getForegroundService(this, requestCode, intent, flags)
}

@RequiresApi(Build.VERSION_CODES.O)
fun Context.foregroundServicePendingIntent(
    requestCode: Int,
    serviceClass: Class<out Service>,
    flags: Int = PendingIntent.FLAG_CANCEL_CURRENT
): PendingIntent {
    return foregroundServicePendingIntent(requestCode, Intent(this, serviceClass), flags)
}

@RequiresApi(Build.VERSION_CODES.O)
fun Context.foregroundServicePendingIntent(
    requestCode: Int,
    serviceClass: KClass<out Service>,
    flags: Int = PendingIntent.FLAG_CANCEL_CURRENT
): PendingIntent {
    return foregroundServicePendingIntent(requestCode, serviceClass.java, flags)
}

@RequiresApi(Build.VERSION_CODES.O)
inline fun <reified T : Service> Context.foregroundServicePendingIntent(
    requestCode: Int,
    flags: Int = PendingIntent.FLAG_CANCEL_CURRENT
): PendingIntent {
    return foregroundServicePendingIntent(requestCode, T::class, flags)
}