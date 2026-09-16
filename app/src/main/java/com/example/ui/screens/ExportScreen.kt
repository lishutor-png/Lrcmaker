package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LrcUtils
import com.example.model.LyricLine
import com.example.model.SongInfo
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioIndigo
import com.example.ui.theme.StudioTextSecondary

@Composable
fun ExportScreen(
    songInfo: SongInfo,
    lyrics: List<LyricLine>,
    onShowSnackbar: (String) -> Unit
) {
    val context = LocalContext.current
    var authorTag by remember { mutableStateOf("Lishrik Studio") }
    var offsetMs by remember { mutableLongStateOf(0L) }

    val lrcContent = remember(songInfo, lyrics, authorTag, offsetMs) {
        LrcUtils.generateLrc(
            songInfo = songInfo,
            lyrics = lyrics,
            author = authorTag,
            offsetMs = offsetMs
        )
    }

    val syncedCount = lyrics.count { it.timestampMs != null }
    val totalCount = lyrics.size
    val cleanFileName = (songInfo.title.ifBlank { "lyrics" } + ".lrc").replace("[^a-zA-Z0-9._-]".toRegex(), "_")

    // Launcher to save file to user's storage
    val saveFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outStream ->
                    outStream.write(lrcContent.toByteArray(Charsets.UTF_8))
                }
                onShowSnackbar("Berkas .lrc berhasil disimpan ke penyimpanan perangkat!")
            } catch (e: Exception) {
                e.printStackTrace()
                onShowSnackbar("Gagal menyimpan berkas: ${e.localizedMessage}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("export_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Summary Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StudioGreen.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StudioGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "File LRC Siap Diekspor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$syncedCount dari $totalCount baris telah disinkronkan (${if (totalCount > 0) (syncedCount * 100 / totalCount) else 0}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioTextSecondary
                    )
                }
            }
        }

        // Action Buttons Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Save to file
            Button(
                onClick = { saveFileLauncher.launch(cleanFileName) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("save_lrc_file_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StudioCyan)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Simpan File .LRC",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Share button
            OutlinedButton(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, lrcContent)
                        putExtra(Intent.EXTRA_TITLE, cleanFileName)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Bagikan Lirik LRC")
                    context.startActivity(shareIntent)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("share_lrc_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Bagikan", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }

        // Copy to clipboard button
        OutlinedButton(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("LRC Lyrics", lrcContent)
                clipboard.setPrimaryClip(clip)
                onShowSnackbar("Teks format .lrc berhasil disalin ke clipboard!")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("copy_lrc_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Salin Teks ke Clipboard", fontSize = 13.sp)
        }

        // LRC Format Preview Viewer Box
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Format Berkas .LRC Standar",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = StudioIndigo.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = cleanFileName,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = StudioCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(Color(0xFF070B12), shape = RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF1E293B), shape = RoundedCornerShape(10.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = lrcContent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }
        }

        // Optional Advanced LRC Tags Settings Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pengaturan Tag Tambahan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = authorTag,
                    onValueChange = { authorTag = it },
                    label = { Text("Pembuat LRC [by:]") },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StudioCyan)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Kompensasi Waktu [offset:]",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Offset: ${if (offsetMs > 0) "+$offsetMs" else "$offsetMs"} ms",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = StudioCyan
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { offsetMs -= 50L },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("-50ms", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { offsetMs = 0L },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("0", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { offsetMs += 50L },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+50ms", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
