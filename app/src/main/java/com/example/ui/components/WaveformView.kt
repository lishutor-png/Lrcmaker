package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.LyricLine
import com.example.ui.theme.StudioCoral
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioIndigo

@Composable
fun WaveformView(
    waveformBars: List<Float>,
    currentPositionMs: Long,
    durationMs: Long,
    lyricLines: List<LyricLine> = emptyList(),
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val activeProgress = if (isDragging) {
        dragProgress
    } else if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val activeBarColor = StudioCyan
    val inactiveBarColor = Color(0xFF1E293B)
    val needleColor = StudioCoral
    val lyricMarkerColor = StudioIndigo

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .testTag("waveform_view")
            .pointerInput(durationMs) {
                detectTapGestures { offset ->
                    if (durationMs > 0) {
                        val progress = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek((progress * durationMs).toLong())
                    }
                }
            }
            .pointerInput(durationMs) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        isDragging = false
                        if (durationMs > 0) {
                            onSeek((dragProgress * durationMs).toLong())
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerY = canvasHeight / 2f

            // Baseline subtle guide
            drawLine(
                color = Color(0x3364748B),
                start = Offset(0f, centerY),
                end = Offset(canvasWidth, centerY),
                strokeWidth = 1.dp.toPx()
            )

            // Draw amplitude bars
            val bars = if (waveformBars.isNotEmpty()) waveformBars else List(90) { 0.35f }
            val barCount = bars.size
            val barSpacing = 2.5f.dp.toPx()
            val totalSpacing = barSpacing * (barCount - 1)
            val barWidth = ((canvasWidth - totalSpacing) / barCount).coerceAtLeast(1.5f.dp.toPx())

            for (i in 0 until barCount) {
                val barProgress = i.toFloat() / barCount.toFloat()
                val isPlayed = barProgress <= activeProgress

                val amp = bars[i]
                val barHeight = ((canvasHeight - 16.dp.toPx()) * amp).coerceAtLeast(4.dp.toPx())
                val x = i * (barWidth + barSpacing)
                val top = centerY - barHeight / 2f

                val color = if (isPlayed) {
                    activeBarColor
                } else {
                    inactiveBarColor
                }

                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }

            // Draw lyric markers (small dots/pins at the bottom of the waveform)
            if (durationMs > 0) {
                for (line in lyricLines) {
                    val time = line.timestampMs ?: continue
                    val pinProgress = (time.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                    val pinX = pinProgress * canvasWidth

                    // Vertical tick
                    drawLine(
                        color = lyricMarkerColor.copy(alpha = 0.85f),
                        start = Offset(pinX, canvasHeight - 8.dp.toPx()),
                        end = Offset(pinX, canvasHeight),
                        strokeWidth = 2.dp.toPx()
                    )
                    // Marker dot
                    drawCircle(
                        color = lyricMarkerColor,
                        radius = 2.5f.dp.toPx(),
                        center = Offset(pinX, canvasHeight - 9.dp.toPx())
                    )
                }
            }

            // Draw current scrubber needle
            val needleX = activeProgress * canvasWidth
            // Subtle glow behind needle
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        needleColor.copy(alpha = 0.0f),
                        needleColor.copy(alpha = 0.35f),
                        needleColor.copy(alpha = 0.0f)
                    ),
                    startX = needleX - 6.dp.toPx(),
                    endX = needleX + 6.dp.toPx()
                ),
                topLeft = Offset(needleX - 6.dp.toPx(), 0f),
                size = Size(12.dp.toPx(), canvasHeight)
            )

            // Needle line
            drawLine(
                color = needleColor,
                start = Offset(needleX, 0f),
                end = Offset(needleX, canvasHeight),
                strokeWidth = 2.dp.toPx()
            )

            // Scrubber top handle
            drawCircle(
                color = needleColor,
                radius = 5.dp.toPx(),
                center = Offset(needleX, 6.dp.toPx())
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = Offset(needleX, 6.dp.toPx())
            )
        }
    }
}
