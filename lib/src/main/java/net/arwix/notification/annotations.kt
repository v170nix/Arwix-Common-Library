package net.arwix.notification

import androidx.annotation.IntDef
import androidx.annotation.StringDef
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

@StringDef(
    NotificationCompat.CATEGORY_ALARM,
    NotificationCompat.CATEGORY_CALL,
    NotificationCompat.CATEGORY_EMAIL,
    NotificationCompat.CATEGORY_ERROR,
    NotificationCompat.CATEGORY_EVENT,
    NotificationCompat.CATEGORY_MESSAGE,
    NotificationCompat.CATEGORY_NAVIGATION,
    NotificationCompat.CATEGORY_PROGRESS,
    NotificationCompat.CATEGORY_PROMO,
    NotificationCompat.CATEGORY_RECOMMENDATION,
    NotificationCompat.CATEGORY_REMINDER,
    NotificationCompat.CATEGORY_SERVICE,
    NotificationCompat.CATEGORY_SOCIAL,
    NotificationCompat.CATEGORY_STATUS,
    NotificationCompat.CATEGORY_SYSTEM
)
@Retention(AnnotationRetention.SOURCE)
annotation class NotificationCategory

@IntDef(
    flag = true, value = [
        NotificationCompat.DEFAULT_ALL,
        NotificationCompat.DEFAULT_SOUND,
        NotificationCompat.DEFAULT_LIGHTS,
        NotificationCompat.DEFAULT_VIBRATE
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class NotificationDefaults

@IntDef(
    NotificationManagerCompat.IMPORTANCE_DEFAULT,
    NotificationManagerCompat.IMPORTANCE_HIGH,
    NotificationManagerCompat.IMPORTANCE_LOW,
    NotificationManagerCompat.IMPORTANCE_MAX,
    NotificationManagerCompat.IMPORTANCE_MIN,
    NotificationManagerCompat.IMPORTANCE_NONE,
    NotificationManagerCompat.IMPORTANCE_UNSPECIFIED
)
@Retention(AnnotationRetention.SOURCE)
annotation class NotificationImportance

@IntDef(
    NotificationCompat.PRIORITY_MIN,
    NotificationCompat.PRIORITY_LOW,
    NotificationCompat.PRIORITY_DEFAULT,
    NotificationCompat.PRIORITY_HIGH,
    NotificationCompat.PRIORITY_MAX
)
@Retention(AnnotationRetention.SOURCE)
annotation class NotificationPriority

@IntDef(
    NotificationCompat.VISIBILITY_PRIVATE,
    NotificationCompat.VISIBILITY_PUBLIC,
    NotificationCompat.VISIBILITY_SECRET
)
@Retention(AnnotationRetention.SOURCE)
annotation class NotificationVisibility