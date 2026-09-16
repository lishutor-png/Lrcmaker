package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin

class AudioPlayerManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private val _waveformBars = MutableStateFlow<List<Float>>(emptyList())
    val waveformBars: StateFlow<List<Float>> = _waveformBars.asStateFlow()

    fun loadAudio(uri: Uri, customWaveform: List<Float>? = null) {
        stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _isLoaded.value = false
        _currentPositionMs.value = 0L

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, uri)
                setOnPreparedListener { mp ->
                    _durationMs.value = mp.duration.toLong()
                    _isLoaded.value = true
                    applyPlaybackSpeed(_playbackSpeed.value)

                    if (customWaveform != null && customWaveform.isNotEmpty()) {
                        _waveformBars.value = customWaveform
                    } else {
                        _waveformBars.value = generateAudioWaveformBars(mp.duration.toLong())
                    }
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPositionMs.value = _durationMs.value
                    stopProgressTicker()
                }
                setOnErrorListener { _, _, _ ->
                    _isPlaying.value = false
                    stopProgressTicker()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play() {
        val player = mediaPlayer ?: return
        if (!_isLoaded.value) return
        try {
            player.start()
            _isPlaying.value = true
            startProgressTicker()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pause() {
        val player = mediaPlayer ?: return
        try {
            if (player.isPlaying) {
                player.pause()
            }
            _isPlaying.value = false
            stopProgressTicker()
            _currentPositionMs.value = player.currentPosition.toLong()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun seekTo(positionMs: Long) {
        val player = mediaPlayer ?: return
        val validPos = positionMs.coerceIn(0L, _durationMs.value)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                player.seekTo(validPos, MediaPlayer.SEEK_CLOSEST)
            } else {
                player.seekTo(validPos.toInt())
            }
            _currentPositionMs.value = validPos
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun skipBy(deltaMs: Long) {
        val current = _currentPositionMs.value
        seekTo(current + deltaMs)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        applyPlaybackSpeed(speed)
    }

    private fun applyPlaybackSpeed(speed: Float) {
        try {
            mediaPlayer?.let { mp ->
                val params = mp.playbackParams ?: PlaybackParams()
                params.speed = speed
                mp.playbackParams = params
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startProgressTicker() {
        stopProgressTicker()
        progressJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _currentPositionMs.value = mp.currentPosition.toLong()
                    }
                }
                delay(25) // ~40 fps polling for buttery smooth needle and karaoke line transitions
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun stop() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _isPlaying.value = false
        stopProgressTicker()
    }

    fun release() {
        stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun clearAudio() {
        stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _isLoaded.value = false
        _durationMs.value = 0L
        _currentPositionMs.value = 0L
        _waveformBars.value = emptyList()
    }

    private fun generateAudioWaveformBars(durationMs: Long): List<Float> {
        val count = 120
        val bars = ArrayList<Float>(count)
        val seedFactor = if (durationMs > 0) (durationMs % 1000).toFloat() / 1000f else 0.5f

        for (i in 0 until count) {
            val progress = i.toFloat() / count.toFloat()
            val w1 = sin(progress * 28.0 + seedFactor * 10.0).toFloat()
            val w2 = sin(progress * 14.0 + 1.2).toFloat()
            val envelope = sin(progress * Math.PI).toFloat().coerceAtLeast(0.2f)
            val raw = (abs(w1 * 0.6f + w2 * 0.4f) * envelope).coerceIn(0.12f, 0.96f)
            bars.add(raw)
        }
        return bars
    }
}
