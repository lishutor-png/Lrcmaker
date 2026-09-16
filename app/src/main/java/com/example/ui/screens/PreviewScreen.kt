package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LrcUtils
import com.example.model.LyricLine
import com.example.model.SongInfo
import com.example.ui.components.WaveformView
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioIndigo
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextSecondary

@Composable
fun PreviewScreen(
    songInfo: SongInfo,
    lyrics: List<LyricLine>,
    waveformBars: List<Float>,
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    activeLineIndex: Int,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipBackward: () -> Unit,
    onSkipForward: () -> Unit,
    onJumpToLine: (Int) -> Unit,
    onNavigateToExport: () -> Unit
) {
    val listState = rememberLazyListState()

    // Smoothly scroll list to keep currently sung line in center
    LaunchedEffect(activeLineIndex) {
        if (activeLineIndex in lyrics.indices) {
            val targetScroll = (activeLineIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(targetScroll)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("preview_screen")
    ) {
        // Karaoke Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StudioCyan.copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = StudioCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Pratinjau Karaoke",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${songInfo.displayTitle} • ${songInfo.displayArtist}",
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = LrcUtils.formatDisplayTime(currentPositionMs),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = StudioCyan,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Waveform Visualizer
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                WaveformView(
                    waveformBars = waveformBars,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    lyricLines = lyrics,
                    onSeek = onSeek
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Transport Mini Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onSeek(0L) }) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Putar dari awal",
                    tint = StudioTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onSkipBackward) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = "Mundur 2 detik",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                color = StudioCyan,
                modifier = Modifier
                    .size(46.dp)
                    .clickable { onPlayPause() }
                    .testTag("preview_play_pause")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onSkipForward) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Maju 2 detik",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Karaoke Lyrics Stage
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (lyrics.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada lirik yang dimuat",
                        color = StudioTextSecondary
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .testTag("preview_lyrics_list"),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    itemsIndexed(lyrics, key = { index, item -> item.id }) { index, line ->
                        val isActive = index == activeLineIndex
                        val isPassed = line.timestampMs != null && line.timestampMs < currentPositionMs && !isActive

                        val scale by animateFloatAsState(
                            targetValue = if (isActive) 1.08f else 0.95f,
                            label = "lineScale"
                        )
                        val textColor by animateColorAsState(
                            targetValue = when {
                                isActive -> StudioCyan
                                isPassed -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                else -> StudioTextMuted
                            },
                            label = "textColor"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(scale)
                                .clickable {
                                    if (line.timestampMs != null) {
                                        onJumpToLine(index)
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            if (line.timestampMs != null) {
                                Text(
                                    text = LrcUtils.formatDisplayTime(line.timestampMs),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = if (isActive) StudioCyan.copy(alpha = 0.8f) else StudioTextMuted.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }

                            Text(
                                text = line.text,
                                fontSize = if (isActive) 21.sp else 16.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                color = textColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(0.9f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CTA Button: Proceed to Export
        Button(
            onClick = onNavigateToExport,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("go_to_export_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StudioCyan)
        ) {
            Text(
                text = "Lanjut ke Ekspor File .LRC",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
