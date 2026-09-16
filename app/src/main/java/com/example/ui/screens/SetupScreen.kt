package com.example.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LrcUtils
import com.example.model.LyricLine
import com.example.model.SongInfo
import com.example.ui.theme.StudioCoral
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioIndigo
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextSecondary

@Composable
fun SetupScreen(
    songInfo: SongInfo,
    lyrics: List<LyricLine>,
    onLoadDemo: () -> Unit,
    onLoadAudioUri: (Uri, String?, String?) -> Unit,
    onOpenLocalMusicDialog: () -> Unit,
    onUpdateMetadata: (String, String, String) -> Unit,
    onUpdateLyrics: (String) -> Unit,
    onCleanEmptyLines: () -> Unit,
    onNavigateToSync: () -> Unit,
    onResetProject: () -> Unit = {}
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    var lyricsInput by remember(lyrics) {
        val initialText = if (lyrics.isNotEmpty()) {
            lyrics.joinToString("\n") { it.text }
        } else {
            ""
        }
        mutableStateOf(initialText)
    }

    var titleInput by remember(songInfo.title) { mutableStateOf(songInfo.title) }
    var artistInput by remember(songInfo.artist) { mutableStateOf(songInfo.artist) }
    var albumInput by remember(songInfo.album) { mutableStateOf(songInfo.album) }

    // Dialog Konfirmasi Hapus Projek
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = StudioCoral
                )
            },
            title = {
                Text(
                    text = "Hapus Projek Lagu Saat Ini?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Lagu ini beserta metadata dan progres lirik akan dibersihkan sehingga Anda bisa memulai projek lagu baru dengan segar."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onResetProject()
                        lyricsInput = ""
                        titleInput = ""
                        artistInput = ""
                        albumInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioCoral)
                ) {
                    Text("Hapus Projek", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // System SAF audio file picker
    val audioPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var fileName = "Lagu Pilihan"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIndex) ?: "Lagu Pilihan"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val cleanTitle = fileName.substringBeforeLast(".")
            onLoadAudioUri(uri, cleanTitle, "Artis Musik")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("setup_screen"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Banner Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            StudioCyan.copy(alpha = 0.15f),
                            StudioIndigo.copy(alpha = 0.12f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 1.dp,
                    color = StudioCyan.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StudioCyan.copy(alpha = 0.2f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = StudioCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lishrik Studio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Sinkronisasi waktu lirik lagu dengan presisi tinggi (1 Projek Aktif)",
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioTextSecondary
                    )
                }
            }
        }

        // Section 1: Audio Source Card (1 Projek Lagu Aktif)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = null,
                            tint = StudioCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1. Sumber Audio Lagu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (songInfo.isLoaded) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StudioCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "1 Lagu Aktif",
                                color = StudioCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (songInfo.isLoaded) {
                    // Current Loaded Song Pill with Delete/Reset Option
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (songInfo.isBuiltInDemo) StudioIndigo.copy(alpha = 0.25f) else StudioCyan.copy(alpha = 0.25f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (songInfo.isBuiltInDemo) Icons.Default.AutoAwesome else Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = if (songInfo.isBuiltInDemo) StudioIndigo else StudioCyan,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = songInfo.displayTitle,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${songInfo.displayArtist}${if (songInfo.album.isNotBlank()) " • ${songInfo.album}" else ""} • ${if (songInfo.isBuiltInDemo) "Lagu Demo" else "Berkas Audio Terhubung"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StudioTextSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Delete Project Button
                            OutlinedButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("delete_project_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioCoral),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCoral.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = StudioCoral,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Hapus Projek Ini (Ganti Lagu Baru)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                } else {
                    // Empty State: No song loaded
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Belum Ada Lagu di Projek",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Aplikasi ini memproses 1 lagu aktif. Pilih lagu atau berkas audio untuk memulai membuat file LRC.",
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Audio Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenLocalMusicDialog,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_music_library_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LibraryMusic,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pustaka Musik", fontSize = 12.sp, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pick_audio_file_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pilih Berkas", fontSize = 12.sp, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onLoadDemo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("load_demo_song_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = StudioIndigo,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gunakan Lagu Contoh (Demo Track)", fontSize = 13.sp)
                }
            }
        }

        // Section 2: Song Info / Metadata (Dibuat ke Bawah agar Semua Tulisan Terlihat Jelas)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "2. Metadata Lagu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tag LRC yang akan dituliskan ke dalam berkas .lrc",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioTextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Judul Lagu (ti) - Full Width
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = {
                        titleInput = it
                        onUpdateMetadata(titleInput, artistInput, albumInput)
                    },
                    label = { Text("Judul Lagu [ti]") },
                    placeholder = { Text("Contoh: Melodi Senja") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("song_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioCyan
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Nama Artis (ar) - Dibuat ke bawah full width
                OutlinedTextField(
                    value = artistInput,
                    onValueChange = {
                        artistInput = it
                        onUpdateMetadata(titleInput, artistInput, albumInput)
                    },
                    label = { Text("Nama Artis / Penyanyi [ar]") },
                    placeholder = { Text("Contoh: Lishrik Studio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("song_artist_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioCyan
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Nama Album (al) - Dibuat ke bawah full width
                OutlinedTextField(
                    value = albumInput,
                    onValueChange = {
                        albumInput = it
                        onUpdateMetadata(titleInput, artistInput, albumInput)
                    },
                    label = { Text("Nama Album [al]") },
                    placeholder = { Text("Contoh: Acoustic Beats (opsional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("song_album_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioCyan
                    )
                )
            }
        }

        // Section 3: Lyrics Input Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "3. Masukkan Lirik Lagu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${lyrics.size} baris terdeteksi",
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Ketik atau tempel lirik (teks biasa atau format .lrc yang sudah ada). Setiap baris akan menjadi satu poin sinkronisasi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = lyricsInput,
                    onValueChange = {
                        lyricsInput = it
                        onUpdateLyrics(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .testTag("lyrics_raw_input"),
                    placeholder = {
                        Text(
                            "Tempel lirik di sini...\nContoh:\nSelamat datang di Lishrik Studio\nTempat sinkronisasi lirik presisi",
                            color = StudioTextMuted
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Action Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCleanEmptyLines,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bersihkan Kosong", fontSize = 11.sp, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = {
                            lyricsInput = ""
                            onUpdateLyrics("")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus Teks", fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }

        // Primary Navigation Button to Sync Studio
        Button(
            onClick = onNavigateToSync,
            enabled = lyrics.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("start_sync_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = StudioCyan,
                disabledContainerColor = StudioCyan.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = "Mulai Sinkronisasi Waktu",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
