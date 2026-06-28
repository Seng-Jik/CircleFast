package com.vim.fasting.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vim.fasting.data.FactPhase
import com.vim.fasting.data.FastingFact
import com.vim.fasting.data.FastingPhase
import com.vim.fasting.data.FastingState
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

private val FastColor = Color(0xFFFF5722)   // Orange-Red
private val EatColor = Color(0xFF4CAF50)     // Green

/**
 * Main screen composable for CircleFast.
 *
 * Optimized: phase strings resolved once per phase change, minimal allocations.
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

    // Resolve phase-dependent values once per phase change
    val phaseLabel: String
    val countUpLabel: String
    val targetDuration: Long
    val phaseColor: Color

    when (state.phase) {
        FastingPhase.FASTING -> {
            phaseLabel = "断食中"
            countUpLabel = "断食中"
            targetDuration = FastingState.FAST_DURATION_MS
            phaseColor = FastColor
        }
        FastingPhase.EATING -> {
            phaseLabel = "进食中"
            countUpLabel = "进食窗口"
            targetDuration = FastingState.EAT_DURATION_MS
            phaseColor = EatColor
        }
    }

    // Compute elapsed (always positive count-up)
    val elapsedMs = currentTimeMs - state.startTimeMs

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

    // Check if overtime
    val isOvertime = elapsedMs >= targetDuration

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Phase label at top
            Text(
                text = phaseLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = phaseColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Circular ring container
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

            Spacer(modifier = Modifier.height(4.dp))

            // Elapsed time (prominent)
            Text(
                text = timeText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = phaseColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = countUpLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFAAAAAA),
                textAlign = TextAlign.Center
            )

            // Overtime badge
            if (isOvertime) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠️ 已超时",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFFD700),
                    textAlign = TextAlign.Center
                )
            }

            // Science fact
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${currentFact.emoji} ${currentFact.title}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = phaseColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = currentFact.body,
                fontSize = 9.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFAAAAAA),
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            // Tap hint
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "点击切换",
                fontSize = 9.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0x55FFFFFF),
                textAlign = TextAlign.Center
            )
        }
    }
}
