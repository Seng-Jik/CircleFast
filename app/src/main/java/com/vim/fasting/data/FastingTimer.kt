package com.vim.fasting.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.vim.fasting.receiver.FastingAlarmReceiver

/**
 * Manages the 16:8 fasting timer using AlarmManager.
 * No foreground service — the timer is purely alarm-driven.
 *
 * Alarms fire when a phase exceeds its target duration,
 * but the phase does NOT auto-switch — the notification only warns.
 */
class FastingTimer(private val context: Context) {

    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    /**
     * Schedule an alarm that fires when 16h fasting target is exceeded.
     */
    fun scheduleFastingOverdue() {
        val endTime = System.currentTimeMillis() + FastingState.FAST_DURATION_MS
        scheduleAlarm(ACTION_FASTING_OVERDUE, endTime)
    }

    /**
     * Schedule an alarm that fires when 8h eating target is exceeded.
     */
    fun scheduleEatingOverdue() {
        val endTime = System.currentTimeMillis() + FastingState.EAT_DURATION_MS
        scheduleAlarm(ACTION_EATING_OVERDUE, endTime)
    }

    /**
     * Cancel all pending alarms.
     */
    fun cancelAll() {
        var pi = getPendingIntent(ACTION_FASTING_OVERDUE, PendingIntent.FLAG_NO_CREATE)
        pi?.let {
            alarmManager?.cancel(it)
            it.cancel()
        }
        pi = getPendingIntent(ACTION_EATING_OVERDUE, PendingIntent.FLAG_NO_CREATE)
        pi?.let {
            alarmManager?.cancel(it)
            it.cancel()
        }
    }

    private fun scheduleAlarm(action: String, triggerAtMillis: Long) {
        val pendingIntent = getPendingIntent(action, PendingIntent.FLAG_UPDATE_CURRENT) ?: return
        alarmManager?.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    private fun getPendingIntent(action: String, flags: Int): PendingIntent? {
        val intent = Intent(context, FastingAlarmReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            requestCodeForAction(action),
            intent,
            flags or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun requestCodeForAction(action: String): Int {
        return when (action) {
            ACTION_FASTING_OVERDUE -> RC_FASTING_OVERDUE
            ACTION_EATING_OVERDUE -> RC_EATING_OVERDUE
            else -> 0
        }
    }

    companion object {
        const val ACTION_FASTING_OVERDUE = "com.vim.fasting.action.FASTING_OVERDUE"
        const val ACTION_EATING_OVERDUE = "com.vim.fasting.action.EATING_OVERDUE"
        private const val RC_FASTING_OVERDUE = 1001
        private const val RC_EATING_OVERDUE = 1002
    }
}
