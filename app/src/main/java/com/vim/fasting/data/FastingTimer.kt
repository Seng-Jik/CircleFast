package com.vim.fasting.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.vim.fasting.receiver.FastingAlarmReceiver

/**
 * Manages the 16:8 fasting timer using AlarmManager.
 * No foreground service — the timer is purely alarm-driven.
 */
class FastingTimer(private val context: Context) {

    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    /**
     * Start a fasting phase. Schedules an alarm for when the 16h fast ends.
     */
    fun startFasting() {
        val endTime = System.currentTimeMillis() + FastingState.FAST_DURATION_MS
        scheduleAlarm(ACTION_FASTING_ENDED, endTime)
    }

    /**
     * Start the eating phase. Schedules an alarm for when the 8h eating window ends.
     */
    fun startEating() {
        val endTime = System.currentTimeMillis() + FastingState.EAT_DURATION_MS
        scheduleAlarm(ACTION_EATING_ENDED, endTime)
    }

    /**
     * Cancel all pending alarms.
     */
    fun cancelAll() {
        val pendingIntent = getPendingIntent(ACTION_FASTING_ENDED, PendingIntent.FLAG_NO_CREATE)
        pendingIntent?.let { pi ->
            alarmManager?.cancel(pi)
            pi.cancel()
        }
        val eatingIntent = getPendingIntent(ACTION_EATING_ENDED, PendingIntent.FLAG_NO_CREATE)
        eatingIntent?.let { pi ->
            alarmManager?.cancel(pi)
            pi.cancel()
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
            ACTION_FASTING_ENDED -> RC_FASTING_ENDED
            ACTION_EATING_ENDED -> RC_EATING_ENDED
            else -> 0
        }
    }

    companion object {
        const val ACTION_FASTING_ENDED = "com.vim.fasting.action.FASTING_ENDED"
        const val ACTION_EATING_ENDED = "com.vim.fasting.action.EATING_ENDED"
        private const val RC_FASTING_ENDED = 1001
        private const val RC_EATING_ENDED = 1002
    }
}
