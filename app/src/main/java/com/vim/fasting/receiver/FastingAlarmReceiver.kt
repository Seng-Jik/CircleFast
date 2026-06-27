package com.vim.fasting.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vim.fasting.data.FastingPhase
import com.vim.fasting.data.FastingPreferences
import com.vim.fasting.data.FastingState
import com.vim.fasting.data.FastingTimer
import com.vim.fasting.data.FactPhase
import com.vim.fasting.data.FastingFact
import com.vim.fasting.notification.NotificationHelper

/**
 * Receives alarm intents from [FastingTimer].
 * Transitions the state and shows the appropriate notification.
 *
 * The receiver self-destroys after handling — no persistent service.
 */
class FastingAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val prefs = FastingPreferences(context)
        val notifier = NotificationHelper(context)
        val timer = FastingTimer(context)

        when (action) {
            FastingTimer.ACTION_FASTING_ENDED -> {
                // 16h fast finished → switch to eating window
                val now = System.currentTimeMillis()
                val newState = FastingState(FastingPhase.EATING, now)
                prefs.saveState(newState)

                // Schedule alarm for eating window end
                timer.startEating()

                // Show alert notification with science fact
                val fact = FastingFact.forElapsedMinutes(FastingState.FAST_DURATION_MS / 60000, FactPhase.FASTING)
                notifier.showPhaseChangeNotification(
                    "${fact.emoji} ${context.getString(com.vim.fasting.R.string.fasting_ended)}",
                    fact.body
                )
            }

            FastingTimer.ACTION_EATING_ENDED -> {
                // 8h eating window finished → back to idle or auto-start new fast
                prefs.clearState()

                val eatingFact = FastingFact.forElapsedMinutes(FastingState.EAT_DURATION_MS / 60000, FactPhase.EATING)
                notifier.showPhaseChangeNotification(
                    "${eatingFact.emoji} ${context.getString(com.vim.fasting.R.string.eating_ended)}",
                    eatingFact.body
                )
            }
        }
    }
}
