package com.example.audio

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object DemoAudioGenerator {

    private const val SAMPLE_RATE = 44100
    private const val DURATION_SECONDS = 36
    private const val NUM_SAMPLES = SAMPLE_RATE * DURATION_SECONDS

    /**
     * Generates a pleasant synthesized demo song in app cache so users can immediately
     * test and experience the LRC timing synchronization without needing to transfer music files.
     */
    suspend fun getOrCreateDemoSong(context: Context): Pair<Uri, List<Float>> = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, "demo_melody.wav")
        val amplitudes = ArrayList<Float>(120)

        if (!file.exists() || file.length() < 1000) {
            val pcmData = generateMelodyPcm()
            writeWavFile(file, pcmData, SAMPLE_RATE)
        }

        // Calculate 120 amplitude peaks for the waveform display
        val step = NUM_SAMPLES / 120
        // Deterministic synthetic waveform representing beats and dynamics of the track
        for (i in 0 until 120) {
            val progress = i / 120f
            val beat = sin(progress * PI * 16.0).toFloat().let { kotlin.math.abs(it) }
            val dynamicRise = 0.35f + 0.45f * sin(progress * PI).toFloat()
            val amp = (0.25f + 0.65f * beat * dynamicRise).coerceIn(0.12f, 0.98f)
            amplitudes.add(amp)
        }

        Uri.fromFile(file) to amplitudes
    }

    private fun generateMelodyPcm(): ShortArray {
        val pcm = ShortArray(NUM_SAMPLES)
        val chordFrequencies = arrayOf(
            floatArrayOf(261.63f, 329.63f, 392.00f), // C Major
            floatArrayOf(220.00f, 261.63f, 329.63f), // A Minor
            floatArrayOf(174.61f, 220.00f, 261.63f), // F Major
            floatArrayOf(196.00f, 246.94f, 293.66f)  // G Major
        )
        val chordDuration = 4.0 // seconds per chord progression step

        for (i in 0 until NUM_SAMPLES) {
            val t = i.toDouble() / SAMPLE_RATE
            val chordIndex = ((t / chordDuration).toInt()) % chordFrequencies.size
            val notes = chordFrequencies[chordIndex]

            // Soft arpeggiator
            val arpegStep = ((t * 4).toInt()) % notes.size
            val mainFreq = notes[arpegStep].toDouble()

            // Bass note
            val bassFreq = (notes[0] / 2).toDouble()

            // Envelope for pulse
            val noteT = (t * 4) % 1.0
            val envelope = exp(-noteT * 3.5)

            // Sine waves with warm harmonics
            val melodySample = sin(2.0 * PI * mainFreq * t) * envelope * 0.45
            val bassSample = sin(2.0 * PI * bassFreq * t) * 0.35
            val padSample = (sin(2.0 * PI * notes[0] * t) + sin(2.0 * PI * notes[1] * t)) * 0.15

            // Kick pulse every second
            val kickT = t % 1.0
            val kick = if (kickT < 0.15) sin(2.0 * PI * 55.0 * kickT) * exp(-kickT * 18.0) * 0.5 else 0.0

            val combined = (melodySample + bassSample + padSample + kick).coerceIn(-1.0, 1.0)
            pcm[i] = (combined * 32000).toInt().toShort()
        }

        return pcm
    }

    private fun writeWavFile(file: File, pcmData: ShortArray, sampleRate: Int) {
        val byteData = ByteBuffer.allocate(pcmData.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (s in pcmData) {
            byteData.putShort(s)
        }
        val pcmBytes = byteData.array()

        FileOutputStream(file).use { out ->
            // WAV Header (44 bytes)
            out.write("RIFF".toByteArray())
            out.write(intToByteArray(36 + pcmBytes.size))
            out.write("WAVE".toByteArray())
            out.write("fmt ".toByteArray())
            out.write(intToByteArray(16)) // Subchunk1Size (16 for PCM)
            out.write(shortToByteArray(1)) // AudioFormat (1 for PCM)
            out.write(shortToByteArray(1)) // NumChannels (1 = Mono)
            out.write(intToByteArray(sampleRate))
            out.write(intToByteArray(sampleRate * 2)) // ByteRate
            out.write(shortToByteArray(2)) // BlockAlign
            out.write(shortToByteArray(16)) // BitsPerSample
            out.write("data".toByteArray())
            out.write(intToByteArray(pcmBytes.size))
            out.write(pcmBytes)
        }
    }

    private fun intToByteArray(value: Int): ByteArray =
        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()

    private fun shortToByteArray(value: Short): ByteArray =
        ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array()
}
