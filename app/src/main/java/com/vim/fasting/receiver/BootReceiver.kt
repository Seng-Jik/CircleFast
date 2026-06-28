package com.vim.fasting.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vim.fasting.data.FastingPhase
import com.vim.fasting.data.FastingPreferences
import com.vim.fasting.data.FastingState
import com.vim.fasting.data.FastingTimer
import com.vim.fasting.data.FastingFact
import com.vim.fasting.data.FactPhase
import com.vim.fasting.notification.NotificationHelper

/**
 * On device reboot, re-schedule any pending overdue alarms.
 * No automatic phase transitions — matches the manual-switch philosophy.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = FastingPreferences(context)
        val state = prefs.loadState()
        val timer = FastingTimer(context)

        when (state.phase) {
            FastingPhase.FASTING -> {
                val elapsed = System.currentTimeMillis() - state.startTimeMs
                if (elapsed < FastingState.FAST_DURATION_MS) {
                    // Not yet over target — re-am the overdue alarm
                    timer.scheduleFastingOverdue()
                } else {
                    // Already past target — notify on boot
                    val notifier = NotificationHelper(context)
                    val fact = FastingFact.forElapsedMinutes(
                        FastingState.FAST_DURATION_MS / 60000,
                        FactPhase.FASTING
                    )
                    notifier.showPhaseChangeNotification(
                        "⏰ 断食已超时",
                        "设备重启 — 断食已超过16小时，点击屏幕切换至进食窗口。"
                    )
                }
            }

            FastingPhase.EATING -> {
                val elapsed = System.currentTimeMillis() - state.startTimeMs
                if (elapsed < FastingState.EAT_DURATION_MS) {
                    timer.scheduleEatingOverdue()
                } else {
                    val notifier = NotificationHelper(context)
                    notifier.showPhaseChangeNotification(
                        "⏰ 进食已超时",
                        "设备重启 — 进食已超过8小时，点击屏幕切换至断食模式。"
                    )
                }
            }
        }
    }
}
