@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package net.arwix.notification

import android.app.Notification
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.collection.SparseArrayCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


@NotificationsGroupMarker
@Suppress("UndocumentedPublicClass")
class NotificationsGroupBuilder @PublishedApi internal constructor(
    private val context: Context,
    private val groupKey: String,
    private val channelId: String,
    private val skipDisabledNotification: Boolean,
    @NotificationCompat.GroupAlertBehavior private val groupAlertBehavior: Int
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    @Suppress("MemberNameEqualsClassName")
    @PublishedApi
    internal val notifications = SparseArrayCompat<Notification>()

    @PublishedApi
    internal var summary: Notification? = null

    @PublishedApi
    internal var summaryId = 0

    /**
     * Create a notification and set it as notification groip summary.
     *
     * If notification channel is disabled than the notification will be added only
     * if [NotificationsGroupBuilder.skipDisabledNotification] is `false`.
     */
    fun summary(
        notificationId: Int,
        @DrawableRes smallIcon: Int,
        channelId: String = this.channelId,
        body: @NotificationsGroupMarker NotificationDsl.() -> Unit
    ) {
        if (needToBuildNotification(channelId)) {
            this.summaryId = notificationId
            summary = notification(context, channelId, smallIcon) {
                body()
                group(groupKey)
                groupAlertBehavior(groupAlertBehavior)
                groupSummary(true)
            }
        }
    }

    /**
     * Set [notification] as notifications group summary
     *
     * If notification channel is disabled than the notification will be added only
     * if [NotificationsGroupBuilder.skipDisabledNotification] is `false`.
     */
    fun summary(notificationId: Int, notification: Notification) {
        if (needToBuildNotification(channelId)) {
            this.summaryId = notificationId
            summary = notification
        }
    }

    /**
     * Create a notification and add it to the group. If notification channel is disabled than the notification
     * will be added only if [NotificationsGroupBuilder.skipDisabledNotification] is `false`.
     */
    fun notification(
        notificationId: Int,
        @DrawableRes smallIcon: Int,
        channelId: String = this@NotificationsGroupBuilder.channelId,
        body: @NotificationsGroupMarker NotificationDsl.() -> Unit
    ) {
        if (needToBuildNotification(channelId)) {
            notifications.put(notificationId,
                notification(context, channelId, smallIcon) {
                    body()
                    group(groupKey)
                }
            )
        }
    }

    /**
     * Create a notification and add it to the group. If notification channel is disabled than the notification
     * will be added only if [NotificationsGroupBuilder.skipDisabledNotification] is `false`.
     */
    fun notification(
        notificationId: Int,
        @DrawableRes smallIcon: Int,
        channelId: String = this@NotificationsGroupBuilder.channelId
    ) {
        if (needToBuildNotification(channelId)) {
            notifications.put(notificationId,
                notification(context, channelId, smallIcon) {
                    group(groupKey)
                }
            )
        }
    }

    private fun needToBuildNotification(channelId: String): Boolean {
        return !skipDisabledNotification || notificationManager.areNotificationsEnabled(channelId)
    }
}