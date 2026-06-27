package com.vim.fasting.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.vim.fasting.data.FastingPhase
import kotlin.math.cos
import kotlin.math.sin

/**
 * Colors matching the app's AMOLED dark theme.
 */
private val ArcTrack = Color(0x331A1A1A)      // Very dark grey track
private val FastingArc = Color(0xFF4CAF50)      // Green for fasting
private val EatingArc = Color(0xFFFF9800)        // Orange for eating
private val TickMark = Color(0x44FFFFFF)         // Subtle white tick marks
private val CenterDot = Color(0x55FFFFFF)        // Dim center indicator

/**
 * Draws a circular countdown arc on a Canvas, manually sized to fit the screen.
 * Designed for round (circular) watch screens — the canvas draws into a centered square.
 *
 * @param phase     Current fasting phase (determines arc color)
 * @param progress  0.0–1.0 progress of the current phase
 * @param modifier  Standard Compose Modifier
 */
@Composable
fun CircularCountdown(
    phase: FastingPhase,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val arcColor = when (phase) {
        FastingPhase.FASTING -> FastingArc
        FastingPhase.EATING -> EatingArc
        FastingPhase.IDLE -> ArcTrack
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Use the smaller dimension for a square drawing area
        val drawSize = minOf(canvasWidth, canvasHeight)
        val offsetX = (canvasWidth - drawSize) / 2f
        val offsetY = (canvasHeight - drawSize) / 2f

        // Stroke thickness: 8% of draw size, cap at 24px
        val strokeWidth = (drawSize * 0.08f).coerceAtMost(24f)
        val radius = (drawSize - strokeWidth) / 2f
        val center = Offset(offsetX + drawSize / 2f, offsetY + drawSize / 2f)
        val topLeft = Offset(
            center.x - radius,
            center.y - radius
        )
        val arcSize = Size(radius * 2f, radius * 2f)

        // ---------- Track ring (full circle) ----------
        drawArc(
            color = ArcTrack,
            startAngle = 270f,       // Start from top (12 o'clock)
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // ---------- Tick marks (every 2 hours) ----------
        val tickCount = 12  // 24h ÷ 2h
        val tickLength = strokeWidth * 0.6f
        val tickWidth = strokeWidth * 0.15f
        for (i in 0 until tickCount) {
            val angle = Math.toRadians((270.0 + 360.0 * i / tickCount))
            val innerR = radius - strokeWidth / 2f
            val outerR = innerR + tickLength
            val startX = center.x + (innerR * cos(angle)).toFloat()
            val startY = center.y + (innerR * sin(angle)).toFloat()
            val endX = center.x + (outerR * cos(angle)).toFloat()
            val endY = center.y + (outerR * sin(angle)).toFloat()
            drawLine(
                color = TickMark,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = tickWidth
            )
        }

        // ---------- Progress arc ----------
        if (phase != FastingPhase.IDLE) {
            val sweep = 360f * progress.coerceIn(0f, 1f)
            drawArc(
                color = arcColor,
                startAngle = 270f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // ---------- Center dot ----------
        drawCircle(
            color = CenterDot,
            radius = strokeWidth * 0.3f,
            center = center
        )
    }
}
