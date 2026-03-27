package com.mentalgym.app.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mentalgym.app.MainActivity
import com.mentalgym.app.R

object WorkoutReminderNotifications {

    const val CHANNEL_ID = "daily_workout_reminder"
    private const val NOTIFICATION_ID = 1701

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily workout reminder",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminder when today's scheduled workout is still open"
        }
        nm.createNotificationChannel(channel)
    }

    fun showPendingWorkoutReminder(context: Context) {
        ensureChannel(context)
        val appContext = context.applicationContext
        val openApp = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPi = PendingIntent.getActivity(
            appContext,
            0,
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Mental Gym")
            .setContentText("You still have a scheduled workout today.")
            .setContentIntent(contentPi)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(appContext).notify(NOTIFICATION_ID, notification)
    }
}
