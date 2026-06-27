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
        val phaseOrdinal = prefs.getInt(KEY_PHASE, FastingPhase.IDLE.ordinal)
        val phase = FastingPhase.entries.getOrElse(phaseOrdinal) { FastingPhase.IDLE }
        val startTime = prefs.getLong(KEY_START_TIME, 0L)
        return FastingState(phase, startTime)
    }

    fun saveState(state: FastingState) {
        prefs.edit()
            .putInt(KEY_PHASE, state.phase.ordinal)
            .putLong(KEY_START_TIME, state.startTimeMs)
            .apply()
    }

    fun clearState() {
        prefs.edit()
            .putInt(KEY_PHASE, FastingPhase.IDLE.ordinal)
            .putLong(KEY_START_TIME, 0L)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "circlefast_prefs"
        private const val KEY_PHASE = "phase"
        private const val KEY_START_TIME = "start_time_ms"
    }
}
