@file:Suppress("unused", "NOTHING_TO_INLINE")

package net.arwix.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import net.arwix.extension.asMap

/**
 * Check that notifications for the app and in channel with [channelId] are available.
 * For Pre-O (8.0) devices check only [NotificationManagerCompat.areNotificationsEnabled]
 *
 * @param channelId Id of the channel
 *
 * return If notifications for the app and the channel are available.
 */
fun NotificationManagerCompat.areNotificationsEnabled(channelId: String): Boolean {
    // Check that notifications isn't disabled for the app
    if (!areNotificationsEnabled()) return false

    // Check notification channels
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Check that notification channel isn't disabled
        val channel = getNotificationChannel(channelId)
        if (channel == null || channel.importance == NotificationManager.IMPORTANCE_NONE) {
            return false
        }

        // Check that notification channel group isn't blocked
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val channelGroup = channel.group?.let(::getNotificationChannelGroup)
            if (channelGroup != null && channelGroup.isBlocked) {
                return false
            }
        }
    }

    return true
}

inline fun notificationsGroup(
    context: Context,
    groupKey: String,
    channelId: String,
    skipDisabledNotification: Boolean = true,
    @NotificationCompat.GroupAlertBehavior groupAlertBehavior: Int = NotificationCompat.GROUP_ALERT_ALL,
    body: NotificationsGroupBuilder.() -> Unit
): NotificationsGroupDsl {
    val group = NotificationsGroupBuilder(context, groupKey, channelId, skipDisabledNotification, groupAlertBehavior)
        .apply(body)
    val summaryNotification = requireNotNull(group.summary) { "Summary notification isn't set" }
    return NotificationsGroupDsl(group.notifications.asMap(), group.summaryId to summaryNotification)
}

/**
 * Create notification channels and group. Will be called only on [Build.VERSION_CODES.O] and greater
 */
inline fun createNotificationChannels(context: Context, build: NotificationChannelsDsl.() -> Unit) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        // Notification Channels aren't supported before Android 8.0 Oreo
        return
    }

    val channels = NotificationChannelsDsl().apply(build)

    NotificationManagerCompat.from(context).apply {
        createNotificationChannels(channels.channels)
        channels.groups.forEach { group -> createNotificationChannels(group.channels) }
        createNotificationChannelGroups(channels.groups)
    }
}

inline fun notification(
    context: Context,
    channelId: String,
    @DrawableRes smallIcon: Int,
    body: NotificationDsl.() -> Unit
): Notification {
    val builder = NotificationCompat.Builder(context, channelId).apply {
        setSmallIcon(smallIcon)
    }

    NotificationDsl(builder, context).apply(body)
    return builder.build()
}

inline fun notification(
    context: Context,
    channelId: String,
    @DrawableRes smallIcon: Int
): Notification {
    NotificationCompat.Builder(context, channelId).apply {
        setSmallIcon(smallIcon)
        return@notification build()
    }
}