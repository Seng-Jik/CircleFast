package com.vim.fasting.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Lightweight SharedPreferences wrapper for fasting state persistence.
 * MVP — sufficient for single-user watch usage.
 */
class FastingPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadState(): FastingState {
        val phaseOrdinal = prefs.getInt(KEY_PHASE, FastingPhase.EATING.ordinal)
        val phase = FastingPhase.entries.getOrElse(phaseOrdinal) { FastingPhase.EATING }
        val startTime = prefs.getLong(KEY_START_TIME, System.currentTimeMillis())
        return FastingState(phase, startTime)
    }

    fun saveState(state: FastingState) {
        prefs.edit()
            .putInt(KEY_PHASE, state.phase.ordinal)
            .putLong(KEY_START_TIME, state.startTimeMs)
            .apply()
    }

    /**
     * Reset to default state: EATING with current time.
     */
    fun resetToDefault() {
        val now = System.currentTimeMillis()
        prefs.edit()
            .putInt(KEY_PHASE, FastingPhase.EATING.ordinal)
            .putLong(KEY_START_TIME, now)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "circlefast_prefs"
        private const val KEY_PHASE = "phase"
        private const val KEY_START_TIME = "start_time_ms"
    }
}
