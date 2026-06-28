package com.vim.fasting.data

/**
 * Represents the current state of the 16:8 fasting timer.
 *
 * No "IDLE" state — the app always defaults to EATING.
 * When a phase exceeds its duration, a notification is sent
 * but timing continues (positive count-up).
 *
 * @param phase         Current phase: EATING or FASTING
 * @param startTimeMs   Wall-clock timestamp (System.currentTimeMillis()) when the current phase started
 */
data class FastingState(
    val phase: FastingPhase = FastingPhase.EATING,
    val startTimeMs: Long = System.currentTimeMillis()
) {
    companion object {
        /** 16 hours in milliseconds */
        const val FAST_DURATION_MS = 16L * 60 * 60 * 1000

        /** 8 hours in milliseconds */
        const val EAT_DURATION_MS = 8L * 60 * 60 * 1000

        /** Total cycle: 24 hours */
        const val CYCLE_DURATION_MS = FAST_DURATION_MS + EAT_DURATION_MS
    }
}

enum class FastingPhase {
    /** 16-hour fasting window active */
    FASTING,

    /** 8-hour eating window active */
    EATING
}
