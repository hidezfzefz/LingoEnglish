package com.lingo.app.reminders

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.lingo.app.MainActivity
import com.lingo.app.R
import com.lingo.app.data.ProgressStore
import com.lingo.app.data.ReminderCopy
import com.lingo.app.data.ReminderSchedule
import java.time.ZonedDateTime

/**
 * Owns the local practice reminder: the notification channel, the single
 * next-day alarm, and permission checks. No server, account data, or exact
 * alarm permission is involved.
 */
object PracticeReminder {

    const val CHANNEL_ID = "practice_reminders"
    const val ACTION_REMINDER = "com.lingo.app.action.PRACTICE_REMINDER"

    private const val NOTIFICATION_ID = 1801
    private const val ALARM_REQUEST_CODE = 1802
    private const val CONTENT_REQUEST_CODE = 1803

    fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Practice reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "A gentle daily nudge to practise English."
            setShowBadge(false)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
        }
        manager.createNotificationChannel(channel)
    }

    fun scheduleNext(context: Context, store: ProgressStore, fromTomorrow: Boolean = false) {
        if (!store.remindersEnabled) {
            cancel(context)
            return
        }
        val trigger = ReminderSchedule.nextTrigger(
            now = ZonedDateTime.now(),
            hour = store.reminderHour,
            minute = store.reminderMinute,
            practicedToday = fromTomorrow || store.practicedToday,
        )
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        runCatching {
            alarm.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                trigger.toInstant().toEpochMilli(),
                alarmIntent(context),
            )
        }
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java)?.cancel(alarmIntent(context))
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    @SuppressLint("MissingPermission")
    fun show(context: Context, store: ProgressStore) {
        // notificationsAllowed() checks POST_NOTIFICATIONS on Android 13+ and
        // the app/channel switch on every version, so notify() is safe here.
        if (!notificationsAllowed(context)) return
        createChannel(context)
        val body = ReminderCopy.body(store.streak)
        val contentIntent = PendingIntent.getActivity(
            context,
            CONTENT_REQUEST_CODE,
            Intent(context, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_OPEN_PRACTICE, true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(ReminderCopy.TITLE)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(ContextCompat.getColor(context, R.color.forest))
            .build()
        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    fun notificationsAllowed(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = manager?.getNotificationChannel(CHANNEL_ID)
        if (channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE) {
            return false
        }
        return true
    }

    fun openNotificationSettings(context: Context) {
        val channelIntent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val appIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (runCatching { context.startActivity(channelIntent) }.isFailure) {
            runCatching { context.startActivity(appIntent) }
        }
    }

    private fun alarmIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            Intent(context, ReminderReceiver::class.java).setAction(ACTION_REMINDER),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
