package com.example.model

import android.net.Uri
import java.util.Locale
import java.util.UUID

data class LyricLine(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val timestampMs: Long? = null
)

data class SongInfo(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val durationMs: Long = 0L,
    val uri: Uri? = null,
    val isBuiltInDemo: Boolean = false
) {
    val isLoaded: Boolean get() = uri != null
    val displayTitle: String get() = title.ifBlank { if (isLoaded) "Lagu Pilihan" else "Belum Ada Lagu" }
    val displayArtist: String get() = artist.ifBlank { if (isLoaded) "Artis Musik" else "-" }
}

data class LocalAudioTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: Uri
)

object LrcUtils {

    /**
     * Formats milliseconds into standard LRC timestamp format: [mm:ss.xx]
     * e.g. 75320 ms -> "[01:15.32]"
     */
    fun formatLrcTimestamp(ms: Long): String {
        val totalHundredths = (ms / 10).coerceAtLeast(0)
        val hundredths = totalHundredths % 100
        val totalSeconds = totalHundredths / 100
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60
        return String.format(Locale.US, "[%02d:%02d.%02d]", minutes, seconds, hundredths)
    }

    /**
     * Formats milliseconds into readable display time: mm:ss.x or mm:ss
     */
    fun formatDisplayTime(ms: Long, includeMillis: Boolean = true): String {
        val totalSeconds = (ms / 1000).coerceAtLeast(0)
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60
        return if (includeMillis) {
            val hundredths = ((ms % 1000) / 10).coerceAtLeast(0)
            String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, hundredths)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    /**
     * Parses raw pasted lyrics text. Supports both:
     * 1. Plain text with one line per lyric
     * 2. Existing LRC text with [mm:ss.xx] or [mm:ss:xx] timestamps!
     */
    fun parseLyricsInput(input: String): List<LyricLine> {
        val lines = input.lines()
        val result = mutableListOf<LyricLine>()
        val lrcRegex = Regex("""\[(\d{1,2}):(\d{2})[.:](\d{2,3})\](.*)""")

        for (rawLine in lines) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) continue

            // Skip metadata tags like [ti:], [ar:], [al:], etc.
            if (trimmed.startsWith("[ti:") || trimmed.startsWith("[ar:") ||
                trimmed.startsWith("[al:") || trimmed.startsWith("[by:") ||
                trimmed.startsWith("[offset:") || trimmed.startsWith("[re:") ||
                trimmed.startsWith("[ve:")
            ) {
                continue
            }

            val match = lrcRegex.find(trimmed)
            if (match != null) {
                val min = match.groupValues[1].toLongOrNull() ?: 0L
                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                val fracStr = match.groupValues[3]
                val frac = fracStr.toLongOrNull() ?: 0L
                val fracMs = if (fracStr.length == 2) frac * 10 else frac
                val totalMs = (min * 60 + sec) * 1000 + fracMs
                val lyricText = match.groupValues[4].trim()
                result.add(LyricLine(text = lyricText, timestampMs = totalMs))
            } else {
                result.add(LyricLine(text = trimmed, timestampMs = null))
            }
        }
        return result
    }

    /**
     * Extracts metadata if input is an existing LRC file
     */
    fun extractMetadata(input: String): Map<String, String> {
        val meta = mutableMapOf<String, String>()
        val tagRegex = Regex("""\[(ti|ar|al|by|offset):([^\]]+)\]""", RegexOption.IGNORE_CASE)
        for (line in input.lines()) {
            val match = tagRegex.find(line.trim())
            if (match != null) {
                val key = match.groupValues[1].lowercase(Locale.US)
                val value = match.groupValues[2].trim()
                meta[key] = value
            }
        }
        return meta
    }

    /**
     * Generates standard, clean LRC string with metadata headers and ordered timestamps
     */
    fun generateLrc(
        songInfo: SongInfo,
        lyrics: List<LyricLine>,
        author: String = "Lishrik Studio",
        offsetMs: Long = 0L
    ): String {
        val sb = StringBuilder()
        sb.appendLine("[ti:${songInfo.title.ifBlank { "Untitled" }}]")
        sb.appendLine("[ar:${songInfo.artist.ifBlank { "Unknown Artist" }}]")
        if (songInfo.album.isNotBlank()) {
            sb.appendLine("[al:${songInfo.album}]")
        }
        sb.appendLine("[by:$author]")
        if (offsetMs != 0L) {
            sb.appendLine("[offset:$offsetMs]")
        }
        sb.appendLine("[re:Lishrik Studio Android]")
        sb.appendLine()

        // Filter and sort lines with timestamps, keeping lines without timestamps at the end or with fallback
        val sortedLines = lyrics.mapIndexed { index, line ->
            index to line
        }.sortedWith(compareBy(
            { it.second.timestampMs ?: Long.MAX_VALUE },
            { it.first }
        ))

        for ((_, line) in sortedLines) {
            val tag = if (line.timestampMs != null) {
                formatLrcTimestamp(line.timestampMs)
            } else {
                "[00:00.00]"
            }
            sb.appendLine("$tag${line.text}")
        }

        return sb.toString().trimEnd()
    }
}
