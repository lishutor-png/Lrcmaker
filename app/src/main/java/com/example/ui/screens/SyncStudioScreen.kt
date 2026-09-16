package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCoral
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioIndigo
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextSecondary

@Composable
fun SyncStudioScreen(
    songInfo: SongInfo,
    lyrics: List<LyricLine>,
    waveformBars: List<Float>,
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    playbackSpeed: Float,
    currentSyncIndex: Int,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipBackward: () -> Unit,
    onSkipForward: () -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit,
    onStampCurrentLine: () -> Unit,
    onSelectSyncIndex: (Int) -> Unit,
    onNudgeTimestamp: (Int, Long) -> Unit,
    onClearTimestamp: (Int) -> Unit,
    onResetAllTimestamps: () -> Unit,
    onJumpToLine: (Int) -> Unit
) {
    val listState = rememberLazyListState()

    // Auto-scroll list to keep active sync line visible
    LaunchedEffect(currentSyncIndex) {
        if (currentSyncIndex in lyrics.indices) {
            listState.animateScrollToItem(currentSyncIndex)
        }
    }

    val currentLine = lyrics.getOrNull(currentSyncIndex)
    val nextLine = lyrics.getOrNull(currentSyncIndex + 1)
    val syncedCount = lyrics.count { it.timestampMs != null }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("sync_studio_screen")
    ) {
        // Top Player Info Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = songInfo.displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${songInfo.displayArtist} • $syncedCount/${lyrics.size} tersinkron",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // High Precision Time Display
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "${LrcUtils.formatDisplayTime(currentPositionMs)} / ${LrcUtils.formatDisplayTime(durationMs, false)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = StudioCyan,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Audio Waveform Visualization
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                WaveformView(
                    waveformBars = waveformBars,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    lyricLines = lyrics,
                    onSeek = onSeek
                )

                // Playback Speed Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Kecepatan:",
                            style = MaterialTheme.typography.labelSmall,
                            color = StudioTextSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0.5f, 0.75f, 1.0f, 1.25f).forEach { speed ->
                            val isSelected = playbackSpeed == speed
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSetPlaybackSpeed(speed) },
                                label = { Text("${speed}x", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StudioCyan,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Transport Controls Row (-2s, Play/Pause, +2s)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onSkipBackward,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("skip_backward_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = "Mundur 2 detik",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                shape = CircleShape,
                color = StudioCyan,
                modifier = Modifier
                    .size(54.dp)
                    .clickable { onPlayPause() }
                    .testTag("play_pause_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Jeda" else "Putar",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = onSkipForward,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("skip_forward_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Maju 2 detik",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Primary Synchronization Focus Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, StudioCyan.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Step Indicator Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StudioCyan.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Baris ${currentSyncIndex + 1} dari ${lyrics.size}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = StudioCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (currentLine?.timestampMs != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StudioGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Tersinkron: ${LrcUtils.formatDisplayTime(currentLine.timestampMs)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = StudioGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Menunggu tanda waktu...",
                            style = MaterialTheme.typography.labelSmall,
                            color = StudioAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Current Active Lyric Text (Prominent)
                Text(
                    text = currentLine?.text ?: "Semua baris selesai disinkronkan!",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )

                // Upcoming Line Preview
                if (nextLine != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Berikutnya: ${nextLine.text}",
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioTextMuted,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // MASSIVE "TANDAI WAKTU" BUTTON
                Button(
                    onClick = onStampCurrentLine,
                    enabled = currentLine != null,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StudioCoral,
                        disabledContainerColor = StudioCoral.copy(alpha = 0.3f)
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stamp_time_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "TEKAN UNTUK TANDAI WAKTU",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Saat lirik ini mulai dinyanyikan (${LrcUtils.formatDisplayTime(currentPositionMs)})",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Navigation Row (Replay Line, Skip)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentLine?.timestampMs != null) {
                                onSeek((currentLine.timestampMs - 2000L).coerceAtLeast(0L))
                            } else {
                                onSkipBackward()
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dengar Ulang", fontSize = 11.sp)
                    }

                    if (currentSyncIndex < lyrics.size - 1) {
                        OutlinedButton(
                            onClick = { onSelectSyncIndex(currentSyncIndex + 1) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Lewati Baris", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Precision Line-by-Line List Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daftar Lirik & Presisi Waktu",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            if (syncedCount > 0) {
                Text(
                    text = "Reset Semua",
                    style = MaterialTheme.typography.labelSmall,
                    color = StudioCoral,
                    modifier = Modifier
                        .clickable { onResetAllTimestamps() }
                        .padding(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Precision Line-by-Line Items
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("lyric_lines_sync_list"),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(lyrics, key = { index, item -> item.id }) { index, line ->
                val isCurrentTarget = index == currentSyncIndex
                val isSynced = line.timestampMs != null

                val backgroundColor by animateColorAsState(
                    targetValue = if (isCurrentTarget) {
                        StudioCyan.copy(alpha = 0.18f)
                    } else if (isSynced) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                    },
                    label = "bgColor"
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = backgroundColor,
                    border = if (isCurrentTarget) {
                        androidx.compose.foundation.BorderStroke(1.5.dp, StudioCyan)
                    } else {
                        null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSyncIndex(index) }
                        .testTag("sync_item_$index")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Index number
                        Text(
                            text = "${index + 1}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isCurrentTarget) StudioCyan else StudioTextMuted,
                            modifier = Modifier.width(22.dp)
                        )

                        // Lyric Text
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = line.text,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isCurrentTarget) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrentTarget) MaterialTheme.colorScheme.onSurface else StudioTextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Timestamp badge or actions
                        if (line.timestampMs != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StudioCyan.copy(alpha = 0.2f),
                                modifier = Modifier.clickable { onJumpToLine(index) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Putar lirik",
                                        tint = StudioCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = LrcUtils.formatDisplayTime(line.timestampMs),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StudioCyan
                                    )
                                }
                            }

                            // Micro-tuning buttons (-100ms, +100ms)
                            Row(
                                modifier = Modifier.padding(start = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { onNudgeTimestamp(index, -100L) }
                                ) {
                                    Text(
                                        text = "-0.1s",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = StudioTextSecondary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { onNudgeTimestamp(index, 100L) }
                                ) {
                                    Text(
                                        text = "+0.1s",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = StudioTextSecondary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onClearTimestamp(index) },
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus tanda waktu",
                                        tint = StudioTextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "--:--.--",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = StudioTextMuted,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
