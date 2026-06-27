package com.vim.fasting.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vim.fasting.data.FastingPhase
import com.vim.fasting.data.FastingState
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

/**
 * Main screen composable for CircleFast.
 *
 * Layout (top to bottom on a round display):
 *   1. Phase label ("断食中" / "进食中" / "准备中")
 *   2. Circular countdown ring
 *   3. Remaining time text (H:MM:SS)
 *   4. No button — we use taps/swipes or separate controls
 *
 * The whole screen is one content block that fits on a round watch face.
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

    val ctx = LocalContext.current

    // Compute elapsed and progress
    val elapsedMs = currentTimeMs - state.startTimeMs
    val (totalDuration, remainingMs) = when (state.phase) {
        FastingPhase.FASTING -> {
            val remaining = (FastingState.FAST_DURATION_MS - elapsedMs).coerceAtLeast(0L)
            FastingState.FAST_DURATION_MS to remaining
        }
        FastingPhase.EATING -> {
            val remaining = (FastingState.EAT_DURATION_MS - elapsedMs).coerceAtLeast(0L)
            FastingState.EAT_DURATION_MS to remaining
        }
        FastingPhase.IDLE -> {
            0L to 0L
        }
    }

    val rawProgress = if (totalDuration > 0L) {
        1f - (remainingMs.toFloat() / totalDuration.toFloat())
    } else {
        0f
    }

    // Animate the progress arc smoothly
    val progress by animateFloatAsState(
        targetValue = rawProgress.coerceIn(0f, 1f),
        label = "arcProgress"
    )

    // Phase colors
    val phaseColor = when (state.phase) {
        FastingPhase.FASTING -> Color(0xFF4CAF50)   // Green
        FastingPhase.EATING -> Color(0xFFFF9800)     // Orange
        FastingPhase.IDLE -> Color(0xFF888888)       // Grey
    }

    val phaseLabel = ctx.getString(
        when (state.phase) {
            FastingPhase.FASTING -> com.vim.fasting.R.string.fasting
            FastingPhase.EATING -> com.vim.fasting.R.string.eating
            FastingPhase.IDLE -> com.vim.fasting.R.string.idle
        }
    )

    // Format remaining time
    val timeText = if (state.phase == FastingPhase.IDLE) {
        "--:--:--"
    } else {
        val hours = TimeUnit.MILLISECONDS.toHours(remainingMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMs) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMs) % 60
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    }

    // Format countdown label
    val countdownLabel = when (state.phase) {
        FastingPhase.FASTING -> "剩余断食"
        FastingPhase.EATING -> "剩余进食"
        FastingPhase.IDLE -> "16:8 断食"
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // All content stacked in center using Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Phase label at top
            Text(
                text = timeText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = phaseColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = countdownLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFAAAAAA),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Circular countdown container — centered, takes ~60% of screen
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularCountdown(
                    phase = state.phase,
                    progress = progress
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Phase label
            Text(
                text = phaseLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = phaseColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
