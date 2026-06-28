package com.vim.fasting.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.vim.fasting.data.FastingPhase
import kotlin.math.cos
import kotlin.math.sin

/**
 * Colors matching the app's AMOLED dark theme.
 */
private val ArcTrack = Color(0x1A1A1A1A)      // Very dark grey track
private val FastingArc = Color(0xFFFF5722)      // Orange-Red for fasting
private val EatingArc = Color(0xFF4CAF50)        // Green for eating
private val TickMark = Color(0x30FFFFFF)         // Subtle white tick marks
private val OvertimeColor = Color(0xFFFFD700)    // Gold for overtime
private val BodyTextColor = Color(0x99AAAAAA)    // Muted grey for body text

/**
 * Draws a circular countdown filling the entire canvas.
 * All text (phase label, timer, facts) is drawn inside the ring.
 */
@Composable
fun CircularCountdown(
    phase: FastingPhase,
    progress: Float,
    phaseLabel: String,
    timeText: String,
    isOvertime: Boolean,
    factTitle: String = "",
    factBody: String = "",
    modifier: Modifier = Modifier
) {
    val arcColor = when (phase) {
        FastingPhase.FASTING -> FastingArc
        FastingPhase.EATING -> EatingArc
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Square drawing area centered in canvas
        val drawSize = minOf(w, h)
        val ox = (w - drawSize) / 2f
        val oy = (h - drawSize) / 2f

        // Ring dimensions — thicker on small screens, capped
        val strokeWidth = (drawSize * 0.09f).coerceIn(12f, 26f)
        val outerRadius = drawSize / 2f - strokeWidth / 2f
        val cx = ox + drawSize / 2f
        val cy = oy + drawSize / 2f
        val arcTopLeft = Offset(cx - outerRadius, cy - outerRadius)
        val arcSize = Size(outerRadius * 2f, outerRadius * 2f)

        // ── Track ring ──
        drawArc(
            color = ArcTrack,
            startAngle = 270f, sweepAngle = 360f,
            useCenter = false, topLeft = arcTopLeft, size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // ── Tick marks ──
        val tickCount = 12
        val tickLen = strokeWidth * 0.5f
        val tickWid = strokeWidth * 0.12f
        for (i in 0 until tickCount) {
            val angle = Math.toRadians(270.0 + 360.0 * i / tickCount)
            val innerR = outerRadius - strokeWidth / 2f
            val outerR = innerR + tickLen
            val sx = cx + (innerR * cos(angle)).toFloat()
            val sy = cy + (innerR * sin(angle)).toFloat()
            val ex = cx + (outerR * cos(angle)).toFloat()
            val ey = cy + (outerR * sin(angle)).toFloat()
            drawLine(TickMark, Offset(sx, sy), Offset(ex, ey), tickWid)
        }

        // ── Progress arc ──
        val sweep = 360f * progress.coerceIn(0f, 1f)
        drawArc(
            color = arcColor,
            startAngle = 270f, sweepAngle = sweep,
            useCenter = false, topLeft = arcTopLeft, size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // ── Text area ──
        val innerRadius = outerRadius - strokeWidth
        val maxTextWidth = innerRadius * 1.6f  // ~80% inner diameter — roomier

        val n = drawContext.canvas.nativeCanvas

        // Paint definitions
        val phaseP = Paint().apply {
            color = arcColor.toArgb()
            textSize = drawSize * 0.070f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val timeP = Paint().apply {
            color = arcColor.toArgb()
            textSize = drawSize * 0.12f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val otP = Paint().apply {
            color = OvertimeColor.toArgb()
            textSize = drawSize * 0.055f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val ftP = Paint().apply {
            color = arcColor.toArgb()
            textSize = drawSize * 0.060f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val fbP = Paint().apply {
            color = BodyTextColor.toArgb()
            textSize = drawSize * 0.048f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        // Gap constants
        val bigGap = drawSize * 0.035f    // between major sections
        val smallGap = drawSize * 0.018f  // between title & body, body lines

        // ── Word-wrap body with sentence-awareness ──
        // Strategy: prefer breaking after 。, ！, ？(sentence end);
        // fall back to space break; fall back to char break.
        fun wrapText(text: String, paint: Paint, maxW: Float, maxLines: Int): List<String> {
            if (text.isEmpty()) return emptyList()
            val result = mutableListOf<String>()
            var remaining = text

            while (remaining.isNotEmpty() && result.size < maxLines) {
                // If it all fits, take it
                if (paint.measureText(remaining) <= maxW) {
                    result.add(remaining)
                    break
                }

                // Find optimal break point
                // Strategy: prefer sentence break (after 。！？), then space, then char
                val nChars = remaining.length
                var lo = 1
                var hi = nChars
                while (lo < hi) {
                    val mid = (lo + hi + 1) / 2
                    if (paint.measureText(remaining.take(mid)) <= maxW) {
                        lo = mid
                    } else {
                        hi = mid - 1
                    }
                }

                // Try to find a sentence break before `lo`
                val sentenceBreakers = setOf('。', '！', '？', '.', '!', '?')
                var breakAt = lo
                val sentenceCandidates = (lo downTo 2)
                    .filter { remaining[it - 1] in sentenceBreakers }
                val bestSentenceBreak = sentenceCandidates.maxOrNull()

                if (bestSentenceBreak != null && bestSentenceBreak > lo / 2) {
                    // Found a sentence break in the second half of the line — prefer it
                    breakAt = bestSentenceBreak
                } else {
                    // Fall back to space break
                    val spaceIdx = remaining.take(lo).lastIndexOf(' ')
                    if (spaceIdx > 0 && spaceIdx > lo / 2) {
                        breakAt = spaceIdx
                    } else {
                        // Punctuation-aware char break
                        val punctIdx = (lo downTo 1).firstOrNull {
                            remaining[it - 1] in ",;，；、—…"
                        }
                        if (punctIdx != null) breakAt = punctIdx
                    }
                }

                result.add(remaining.take(breakAt).trimEnd())
                remaining = remaining.drop(breakAt).trimStart()

                if (remaining.isNotEmpty() && result.size >= maxLines) break
            }
            return result
        }

        // ── Compute text layout ──
        val phaseH = phaseP.textSize
        val timeH = timeP.textSize
        val otH = if (isOvertime) otP.textSize else 0f
        val ftH = if (factTitle.isNotEmpty()) ftP.textSize else 0f
        val bodyLines = if (factBody.isNotEmpty()) wrapText(factBody, fbP, maxTextWidth, 3) else emptyList()
        val bodyH = bodyLines.size * fbP.textSize + (bodyLines.size - 1) * smallGap

        val headerH = phaseH + bigGap + timeH
        val otBlockH = if (isOvertime) otH + bigGap else 0f
        val factBlockH = if (factTitle.isNotEmpty() || bodyLines.isNotEmpty()) {
            val ftBlock = if (factTitle.isNotEmpty()) ftH + smallGap else 0f
            bigGap + ftBlock + bodyH
        } else 0f

        val totalH = headerH + otBlockH + factBlockH

        // Start drawing Y — vertically centered
        var y = cy - totalH / 2f + phaseH

        // ── Phase label ──
        n.drawText(phaseLabel, cx, y, phaseP)
        y += phaseH + bigGap

        // ── Timer ──
        n.drawText(timeText, cx, y, timeP)
        y += timeH + (if (isOvertime) smallGap else bigGap)

        // ── Overtime ──
        if (isOvertime) {
            n.drawText("⚠️ 已超时", cx, y, otP)
            y += otH + bigGap
        }

        // ── Fact section ──
        if (factTitle.isNotEmpty() || bodyLines.isNotEmpty()) {
            y += bigGap

            if (factTitle.isNotEmpty()) {
                var display = factTitle
                if (ftP.measureText(display) > maxTextWidth) {
                    while (display.isNotEmpty() && ftP.measureText("$display…") > maxTextWidth) {
                        display = display.dropLast(1)
                    }
                    display = "$display…"
                }
                n.drawText(display, cx, y, ftP)
                y += ftH + smallGap
            }

            for (line in bodyLines) {
                n.drawText(line, cx, y, fbP)
                y += fbP.textSize + smallGap
            }
        }
    }
}
