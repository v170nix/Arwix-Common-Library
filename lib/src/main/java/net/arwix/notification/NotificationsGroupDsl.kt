package net.arwix.notification

import android.app.Notification
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat

class NotificationsGroupDsl @PublishedApi internal constructor(
    private val notifications: Map<Int, Notification>,
    private val summary: Pair<Int, Notification>
) {

    /**
     * Post notifications to be shown in the status bar, stream, etc.
     */
    fun notify(notificationManager: NotificationManagerCompat) {
        notifications.forEach { (notificationId, notification) ->
            notificationManager.notify(notificationId, notification)
        }

        notificationManager.notify(summary.first, summary.second)
    }

    /**
     * Post notifications to be shown in the status bar, stream, etc.
     */
    fun notify(notificationManager: NotificationManager) {
        notifications.forEach { (notificationId, notification) ->
            notificationManager.notify(notificationId, notification)
        }

        notificationManager.notify(summary.first, summary.second)
    }
}