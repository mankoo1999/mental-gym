package com.mentalgym.app.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.mentalgym.app.data.preferences.AppPreferencesRepository
import java.time.ZoneId
import java.time.ZonedDateTime

object DailyWorkoutReminderScheduler {

    private const val REQUEST_CODE = 0x4d47

    private fun alarmPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, DailyWorkoutReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = alarmPendingIntent(context)
        am.cancel(pi)
    }

    fun sync(context: Context, enabled: Boolean, timeKey: String) {
        val appContext = context.applicationContext
        cancel(appContext)
        if (!enabled) return
        val (hour, minute) = parseTime(timeKey)
        val triggerAtMillis = nextReminderMillis(hour, minute, ZoneId.systemDefault())
        val pi = alarmPendingIntent(appContext)
        val am = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAtMillis, pi), pi)
    }

    internal fun parseTime(timeKey: String): Pair<Int, Int> =
        when (timeKey) {
            AppPreferencesRepository.REMINDER_TIME_700 -> 19 to 0
            else -> 18 to 30
        }

    internal fun nextReminderMillis(hour: Int, minute: Int, zone: ZoneId): Long {
        val now = ZonedDateTime.now(zone)
        var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) {
            next = next.plusDays(1)
        }
        return next.toInstant().toEpochMilli()
    }
}
