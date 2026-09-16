package com.example

import com.example.model.LrcUtils
import com.example.model.LyricLine
import com.example.model.SongInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LrcUtilsTest {

    @Test
    fun `test formatLrcTimestamp correctly formats minutes, seconds, hundredths`() {
        assertEquals("[00:00.00]", LrcUtils.formatLrcTimestamp(0L))
        assertEquals("[00:01.50]", LrcUtils.formatLrcTimestamp(1500L))
        assertEquals("[01:15.32]", LrcUtils.formatLrcTimestamp(75320L))
        assertEquals("[02:05.08]", LrcUtils.formatLrcTimestamp(125080L))
    }

    @Test
    fun `test parse plain lyrics lines`() {
        val input = """
            Baris pertama
            Baris kedua
            Baris ketiga
        """.trimIndent()

        val parsed = LrcUtils.parseLyricsInput(input)
        assertEquals(3, parsed.size)
        assertEquals("Baris pertama", parsed[0].text)
        assertNull(parsed[0].timestampMs)
        assertEquals("Baris kedua", parsed[1].text)
        assertNull(parsed[1].timestampMs)
        assertEquals("Baris ketiga", parsed[2].text)
        assertNull(parsed[2].timestampMs)
    }

    @Test
    fun `test parse existing LRC content with timestamps and tags`() {
        val input = """
            [ti:Contoh Lagu]
            [ar:Artis Hebat]
            [00:05.20]Lirik baris pembuka
            [00:10.55]Lirik baris kedua
        """.trimIndent()

        val parsed = LrcUtils.parseLyricsInput(input)
        assertEquals(2, parsed.size)
        assertEquals("Lirik baris pembuka", parsed[0].text)
        assertEquals(5200L, parsed[0].timestampMs)
        assertEquals("Lirik baris kedua", parsed[1].text)
        assertEquals(10550L, parsed[1].timestampMs)

        val metadata = LrcUtils.extractMetadata(input)
        assertEquals("Contoh Lagu", metadata["ti"])
        assertEquals("Artis Hebat", metadata["ar"])
    }

    @Test
    fun `test generate clean standard LRC string`() {
        val info = SongInfo(
            title = "Melodi Senja",
            artist = "Studio Musisi",
            album = "Akustik 2026"
        )
        val lyrics = listOf(
            LyricLine(text = "Intro instrumen", timestampMs = 0L),
            LyricLine(text = "Saat senja tiba", timestampMs = 3450L)
        )

        val lrc = LrcUtils.generateLrc(info, lyrics, author = "Lishrik Studio")

        assert(lrc.contains("[ti:Melodi Senja]"))
        assert(lrc.contains("[ar:Studio Musisi]"))
        assert(lrc.contains("[al:Akustik 2026]"))
        assert(lrc.contains("[by:Lishrik Studio]"))
        assert(lrc.contains("[00:00.00]Intro instrumen"))
        assert(lrc.contains("[00:03.45]Saat senja tiba"))
    }
}
