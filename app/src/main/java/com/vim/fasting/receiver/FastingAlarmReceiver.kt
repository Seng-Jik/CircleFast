package com.vim.fasting.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vim.fasting.data.FastingPhase
import com.vim.fasting.data.FastingState
import com.vim.fasting.data.FastingTimer
import com.vim.fasting.data.FactPhase
import com.vim.fasting.data.FastingFact
import com.vim.fasting.notification.NotificationHelper

/**
 * Receives alarm intents from [FastingTimer].
 *
 * Unlike the original design, alarms do NOT switch phases.
 * They ONLY notify the user that the target duration has been exceeded.
 * The user manually switches via tap.
 */
class FastingAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val notifier = NotificationHelper(context)

        when (action) {
            FastingTimer.ACTION_FASTING_OVERDUE -> {
                // 16h target exceeded — notify but keep fasting
                val fact = FastingFact.forElapsedMinutes(
                    FastingState.FAST_DURATION_MS / 60000,
                    FactPhase.FASTING
                )
                notifier.showPhaseChangeNotification(
                    "⏰ 断食16小时目标达成！",
                    "已断食满16小时，请考虑切换到进食窗口。\n${fact.emoji} ${fact.body}"
                )
            }

            FastingTimer.ACTION_EATING_OVERDUE -> {
                // 8h target exceeded — notify but keep eating
                val fact = FastingFact.forElapsedMinutes(
                    FastingState.EAT_DURATION_MS / 60000,
                    FactPhase.EATING
                )
                notifier.showPhaseChangeNotification(
                    "⏰ 进食8小时目标达成！",
                    "进食窗口已满8小时，请考虑切换到断食。\n${fact.emoji} ${fact.body}"
                )
            }
        }
    }
}
