package com.vim.fasting.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vim.fasting.data.FastingPhase
import com.vim.fasting.data.FastingPreferences
import com.vim.fasting.data.FastingState
import com.vim.fasting.data.FastingTimer
import com.vim.fasting.notification.NotificationHelper

/**
 * On device reboot, re-schedule any pending alarms so the user doesn't lose their timer.
 * No service is started — just re-arms AlarmManager.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = FastingPreferences(context)
        val state = prefs.loadState()

        if (state.phase == FastingPhase.IDLE) return

        val timer = FastingTimer(context)
        val elapsed = System.currentTimeMillis() - state.startTimeMs

        when (state.phase) {
            FastingPhase.FASTING -> {
                val remaining = FastingState.FAST_DURATION_MS - elapsed
                if (remaining > 0L) {
                    // Re-arm the alarm for the remaining time
                    val newStartTime = System.currentTimeMillis() - elapsed
                    prefs.saveState(state.copy(startTimeMs = newStartTime))
                    timer.startFasting()
                } else {
                    // Fast already ended while device was off — transition to eating
                    val notifier = NotificationHelper(context)
                    val now = System.currentTimeMillis()
                    prefs.saveState(FastingState(FastingPhase.EATING, now))
                    timer.startEating()
                    notifier.showPhaseChangeNotification(
                        "🍽️ 断食自动完成",
                        "设备重启 — 已自动进入进食窗口"
                    )
                }
            }

            FastingPhase.EATING -> {
                val remaining = FastingState.EAT_DURATION_MS - elapsed
                if (remaining > 0L) {
                    val newStartTime = System.currentTimeMillis() - elapsed
                    prefs.saveState(state.copy(startTimeMs = newStartTime))
                    timer.startEating()
                } else {
                    prefs.clearState()
                    val notifier = NotificationHelper(context)
                    notifier.showPhaseChangeNotification(
                        "⏰ 进食窗口已关闭",
                        "设备重启 — 进食窗口已过期，可开始新断食"
                    )
                }
            }

            FastingPhase.IDLE -> { /* nothing to do */ }
        }
    }
}
