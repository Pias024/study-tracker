package com.pias.studytracker.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pias.studytracker.R

const val REMINDER_CHANNEL_ID = "study_reminder_channel"
private const val REMINDER_NOTIFICATION_ID = 2001

fun ensureReminderChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            "Study reminder",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily nudge to log today's study hours"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}

fun showReminderNotification(context: Context) {
    ensureReminderChannel(context)
    val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("Logged today's hours yet?")
        .setContentText("Open Study Tracker and log today's study session.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    // Guarded by the caller checking POST_NOTIFICATIONS permission on API 33+;
    // NotificationManagerCompat.notify() is a no-op if the app lacks permission.
    NotificationManagerCompat.from(context).notify(REMINDER_NOTIFICATION_ID, notification)
}
