package com.vim.fasting.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vim.fasting.data.FactPhase
import com.vim.fasting.data.FastingFact
import com.vim.fasting.data.FastingPhase
import com.vim.fasting.data.FastingState
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

/**
 * Main screen composable for CircleFast.
 *
 * Everything (phase label, timer, science fact) is drawn inside the ring
 * canvas so it fits any screen size including round watches.
 */
@Composable
fun CircleFastScreen(
    state: FastingState,
    modifier: Modifier = Modifier
) {
    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Update every second
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTimeMs = System.currentTimeMillis()
        }
    }

    // Compute elapsed (always positive count-up)
    val elapsedMs = currentTimeMs - state.startTimeMs

    val targetDuration: Long
    val phaseColor: Long  // argb color passed to canvas

    when (state.phase) {
        FastingPhase.FASTING -> {
            targetDuration = FastingState.FAST_DURATION_MS
            phaseColor = 0xFFFF5722
        }
        FastingPhase.EATING -> {
            targetDuration = FastingState.EAT_DURATION_MS
            phaseColor = 0xFF4CAF50
        }
    }

    // Progress: capped at 1.0f once target duration is reached
    val rawProgress = if (targetDuration > 0L) {
        (elapsedMs.toFloat() / targetDuration.toFloat()).coerceAtMost(1f)
    } else {
        0f
    }

    // Animate the progress arc smoothly
    val progress by animateFloatAsState(
        targetValue = rawProgress.coerceIn(0f, 1f),
        label = "arcProgress"
    )

    // Science fact for current elapsed time
    val elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMs)
    val currentFact = remember(elapsedMinutes, state.phase) {
        FastingFact.forElapsedMinutes(
            minutes = elapsedMinutes,
            phase = if (state.phase == FastingPhase.EATING) FactPhase.EATING else FactPhase.FASTING
        )
    }

    // Format elapsed time (HH:MM:SS, positive count-up)
    val timeText = if (elapsedMs < 0) {
        "00:00:00"
    } else {
        val hours = TimeUnit.MILLISECONDS.toHours(elapsedMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMs) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMs) % 60
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    // Phase label
    val phaseLabel = when (state.phase) {
        FastingPhase.FASTING -> "断食中"
        FastingPhase.EATING -> "进食窗口"
    }

    // Check if overtime
    val isOvertime = elapsedMs >= targetDuration

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularCountdown(
            phase = state.phase,
            progress = progress,
            phaseLabel = phaseLabel,
            timeText = timeText,
            isOvertime = isOvertime,
            factTitle = "${currentFact.emoji} ${currentFact.title}",
            factBody = currentFact.body
        )
    }
}
