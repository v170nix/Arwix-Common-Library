@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package net.arwix.notification

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.NotificationChannel
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat

@NotificationChannelGroupMarker
@TargetApi(Build.VERSION_CODES.O)
@Suppress("UndocumentedPublicClass")
class NotificationChannelGroupDsl @PublishedApi internal constructor(
    internal val channels: MutableList<NotificationChannel>
) {

    /**
     * Create a channel and add into the group
     */
    @SuppressLint("WrongConstant")
    fun channel(
        id: String,
        name: CharSequence,
        @NotificationImportance importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT,
        build: @NotificationChannelGroupMarker NotificationChannelDsl.() -> Unit
    ) {
        channels += NotificationChannel(id, name, importance).also { NotificationChannelDsl(it).build() }
    }

    /**
     * Create a channel and add into the group
     */
    @SuppressLint("WrongConstant")
    fun channel(
        id: String,
        name: CharSequence,
        @NotificationImportance importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT
    ) {
        channels += NotificationChannel(id, name, importance)
    }

    /**
     * Add the channel into the group
     */
    fun channel(channel: NotificationChannel) {
        @SuppressLint("WrongConstant")
        channels += channel
    }

    /**
     * Add the channel into the group
     */
    operator fun plus(channel: NotificationChannel) {
        this.channels += channel
    }

    @RequiresApi(Build.VERSION_CODES.O)
    operator fun NotificationChannelGroupDsl.plus(channels: Iterable<NotificationChannel>) {
        this.channels += channels
    }
}