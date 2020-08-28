@file:Suppress("unused")

package net.arwix.notification

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.os.Build
import androidx.core.app.NotificationManagerCompat

@TargetApi(Build.VERSION_CODES.O)
@NotificationChannelsMarker
@Suppress("UndocumentedPublicClass")
class NotificationChannelsDsl @PublishedApi internal constructor(
    @PublishedApi internal val groups: MutableList<NotificationChannelGroup> = mutableListOf(),
    @PublishedApi internal val channels: MutableList<NotificationChannel> = mutableListOf()
) {
    /**
     * Add notification channel
     *
     * It doesn't do anything on older SDKs which doesn't support Notification Channels.
     *
     * @param id The id of the channel. Must be unique per package. The value may be truncated if it is too long.
     * @param name The user visible name of the channel. You can rename this channel when the system
     *             locale changes by listening for the
     *             [Intent.ACTION_LOCALE_CHANGED][android.content.Intent.ACTION_LOCALE_CHANGED] broadcast.
     *             The recommended maximum length is 40 characters; the value may be truncated if it is too long.
     * @param importance The importance of the channel. This controls how interruptive notifications
     *                   posted to this channel are.
     */
    fun channel(
        id: String,
        name: CharSequence,
        @NotificationImportance importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT
    ) {
        @SuppressLint("WrongConstant")
        channels += NotificationChannel(id, name, importance)
    }

    /**
     * Add notification channel
     *
     * It doesn't do anything on older SDKs which doesn't support Notification Channels.
     *
     * @param id The id of the channel. Must be unique per package. The value may be truncated if it is too long.
     * @param name The user visible name of the channel. You can rename this channel when the system locale changes
     *             by listening for the [Intent.ACTION_LOCALE_CHANGED][android.content.Intent.ACTION_LOCALE_CHANGED]
     *             broadcast.
     *             The recommended maximum length is 40 characters; the value may be truncated if it is too long.
     * @param importance The importance of the channel.
     *                       This controls how interruptive notifications posted to this channel are.
     */
    inline fun channel(
        id: String,
        name: CharSequence,
        @NotificationImportance importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT,
        build: @NotificationChannelsMarker NotificationChannelDsl.() -> Unit
    ) {
        @SuppressLint("WrongConstant")
        val channel = NotificationChannel(id, name, importance)
        NotificationChannelDsl(channel).build()
        channels += channel
    }

    /**
     * Add notification channel group.
     *
     * @param id The id of the group. Must be unique per package. The value may be truncated if it is too long.
     * @param name The user visible name of the group. You can rename this group when the system locale changes
     *             by listening for the [Intent.ACTION_LOCALE_CHANGED][android.content.Intent.ACTION_LOCALE_CHANGED]
     *             broadcast.
     *             The recommended maximum length is 40 characters; the value may be truncated if it is too long.
     */
    fun group(id: String, name: CharSequence) {
        groups += NotificationChannelGroup(id, name)
    }

    /**
     * Add notification channel group.
     *
     * @param id The id of the group. Must be unique per package. The value may be truncated if it is too long.
     * @param name The user visible name of the group. You can rename this group when the system locale changes
     *             by listening for the [Intent.ACTION_LOCALE_CHANGED][android.content.Intent.ACTION_LOCALE_CHANGED]
     *             broadcast.
     *             The recommended maximum length is 40 characters; the value may be truncated if it is too long.
     */
    inline fun group(
        id: String,
        name: CharSequence,
        build: @NotificationChannelsMarker NotificationChannelGroupDsl.() -> Unit
    ) {
        val group = NotificationChannelGroup(id, name)
        NotificationChannelGroupDsl(group.channels).build()
        groups += group
    }
}
