package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayerManager
import com.example.audio.DemoAudioGenerator
import com.example.audio.LocalMusicRepository
import com.example.model.LocalAudioTrack
import com.example.model.LrcUtils
import com.example.model.LyricLine
import com.example.model.SongInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StudioTab(val title: String) {
    SETUP("Lagu & Lirik"),
    SYNC("Sinkronisasi"),
    PREVIEW("Pratinjau"),
    EXPORT("Ekspor LRC")
}

class LrcStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val playerManager = AudioPlayerManager(application)
    private val musicRepo = LocalMusicRepository(application)

    val isPlaying = playerManager.isPlaying
    val currentPositionMs = playerManager.currentPositionMs
    val durationMs = playerManager.durationMs
    val playbackSpeed = playerManager.playbackSpeed
    val isAudioLoaded = playerManager.isLoaded
    val waveformBars = playerManager.waveformBars

    private val _currentTab = MutableStateFlow(StudioTab.SETUP)
    val currentTab: StateFlow<StudioTab> = _currentTab.asStateFlow()

    private val _songInfo = MutableStateFlow(SongInfo())
    val songInfo: StateFlow<SongInfo> = _songInfo.asStateFlow()

    private val _lyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val lyrics: StateFlow<List<LyricLine>> = _lyrics.asStateFlow()

    private val _currentSyncIndex = MutableStateFlow(0)
    val currentSyncIndex: StateFlow<Int> = _currentSyncIndex.asStateFlow()

    private val _localTracks = MutableStateFlow<List<LocalAudioTrack>>(emptyList())
    val localTracks: StateFlow<List<LocalAudioTrack>> = _localTracks.asStateFlow()

    private val _searchMusicQuery = MutableStateFlow("")
    val searchMusicQuery: StateFlow<String> = _searchMusicQuery.asStateFlow()

    private val _showMusicLibraryDialog = MutableStateFlow(false)
    val showMusicLibraryDialog: StateFlow<Boolean> = _showMusicLibraryDialog.asStateFlow()

    private val _isLoadingMusic = MutableStateFlow(false)
    val isLoadingMusic: StateFlow<Boolean> = _isLoadingMusic.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Filtered music library list
    val filteredLocalTracks: StateFlow<List<LocalAudioTrack>> = combine(
        _localTracks,
        _searchMusicQuery
    ) { tracks, query ->
        if (query.isBlank()) {
            tracks
        } else {
            val q = query.trim().lowercase()
            tracks.filter {
                it.title.lowercase().contains(q) || it.artist.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active line in Preview mode based on playback timestamp
    val activePreviewIndex: StateFlow<Int> = combine(
        currentPositionMs,
        _lyrics
    ) { pos, lines ->
        var activeIdx = -1
        for (i in lines.indices) {
            val time = lines[i].timestampMs
            if (time != null && time <= pos) {
                activeIdx = i
            } else if (time != null && time > pos) {
                break
            }
        }
        activeIdx
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    init {
        // Automatically prepare the demo melody so the app is instantly usable
        loadDemoSong(showSnackbar = false)
    }

    fun setTab(tab: StudioTab) {
        _currentTab.value = tab
    }

    fun loadDemoSong(showSnackbar: Boolean = true) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val (uri, demoWaveform) = DemoAudioGenerator.getOrCreateDemoSong(context)
            _songInfo.value = SongInfo(
                title = "Melodi Senja (Demo)",
                artist = "Lishrik Studio",
                album = "Acoustic Beats",
                durationMs = 36000L,
                uri = uri,
                isBuiltInDemo = true
            )
            playerManager.loadAudio(uri, demoWaveform)

            // If lyrics are empty, load demo lyrics
            if (_lyrics.value.isEmpty()) {
                val demoText = """
                    Selamat datang di Lishrik Studio
                    Tempat membuat lirik berirama
                    Dengarkan melodi yang mengalun
                    Tekan tombol tepat pada nada
                    Sinkronisasi presisi setiap kata
                    Lihat gelombang audio berdetak
                    Pratinjau lirik bagai karaoke
                    Ekspor file lrc dengan sempurna
                """.trimIndent()
                setRawLyrics(demoText)
            }

            if (showSnackbar) {
                _snackbarMessage.value = "Lagu contoh demo berhasil dimuat!"
            }
        }
    }

    fun resetProject() {
        playerManager.clearAudio()
        _songInfo.value = SongInfo()
        _lyrics.value = emptyList()
        _currentSyncIndex.value = 0
        _snackbarMessage.value = "Projek lagu berhasil dihapus. Silakan pilih atau masukkan lagu baru."
    }

    fun loadCustomAudio(uri: Uri, title: String?, artist: String?, album: String? = "") {
        val cleanTitle = title?.ifBlank { "Lagu Pilihan" } ?: "Lagu Pilihan"
        val cleanArtist = artist?.ifBlank { "Artis Lokal" } ?: "Artis Lokal"
        _songInfo.value = SongInfo(
            title = cleanTitle,
            artist = cleanArtist,
            album = album ?: "",
            durationMs = 0L,
            uri = uri,
            isBuiltInDemo = false
        )
        playerManager.loadAudio(uri)
        _snackbarMessage.value = "Audio berhasil dimuat: $cleanTitle"
    }

    fun updateMetadata(title: String, artist: String, album: String) {
        _songInfo.value = _songInfo.value.copy(
            title = title,
            artist = artist,
            album = album
        )
    }

    fun scanLocalMusicLibrary() {
        viewModelScope.launch {
            _isLoadingMusic.value = true
            val tracks = musicRepo.getLocalAudioTracks()
            _localTracks.value = tracks
            _isLoadingMusic.value = false
            if (tracks.isEmpty()) {
                _snackbarMessage.value = "Tidak ada file musik ditemukan di MediaStore. Anda bisa menggunakan pemilih file audio atau lagu demo."
            }
        }
    }

    fun selectLocalTrack(track: LocalAudioTrack) {
        loadCustomAudio(
            uri = track.contentUri,
            title = track.title,
            artist = track.artist,
            album = track.album
        )
        _showMusicLibraryDialog.value = false
    }

    fun setShowMusicLibraryDialog(show: Boolean) {
        _showMusicLibraryDialog.value = show
        if (show && _localTracks.value.isEmpty()) {
            scanLocalMusicLibrary()
        }
    }

    fun setSearchMusicQuery(query: String) {
        _searchMusicQuery.value = query
    }

    fun setRawLyrics(rawText: String) {
        val parsed = LrcUtils.parseLyricsInput(rawText)
        val meta = LrcUtils.extractMetadata(rawText)
        if (meta.isNotEmpty()) {
            val cur = _songInfo.value
            _songInfo.value = cur.copy(
                title = meta["ti"] ?: cur.title,
                artist = meta["ar"] ?: cur.artist,
                album = meta["al"] ?: cur.album
            )
        }
        _lyrics.value = parsed
        _currentSyncIndex.value = 0
    }

    fun cleanEmptyLines() {
        val cleaned = _lyrics.value.filter { it.text.isNotBlank() }
        _lyrics.value = cleaned
        _currentSyncIndex.value = _currentSyncIndex.value.coerceIn(0, (cleaned.size - 1).coerceAtLeast(0))
        _snackbarMessage.value = "Baris kosong berhasil dibersihkan."
    }

    fun stampCurrentLine() {
        val lines = _lyrics.value.toMutableList()
        val index = _currentSyncIndex.value
        if (index in lines.indices) {
            val currentMs = playerManager.currentPositionMs.value
            lines[index] = lines[index].copy(timestampMs = currentMs)
            _lyrics.value = lines

            triggerHapticFeedback()

            // Advance to the next unsynced or next line
            if (index < lines.size - 1) {
                _currentSyncIndex.value = index + 1
            }
        }
    }

    fun setCurrentSyncIndex(index: Int) {
        if (index in _lyrics.value.indices) {
            _currentSyncIndex.value = index
        }
    }

    fun nudgeTimestamp(index: Int, deltaMs: Long) {
        val lines = _lyrics.value.toMutableList()
        if (index in lines.indices) {
            val current = lines[index].timestampMs ?: playerManager.currentPositionMs.value
            val newTime = (current + deltaMs).coerceAtLeast(0L)
            lines[index] = lines[index].copy(timestampMs = newTime)
            _lyrics.value = lines
        }
    }

    fun setLineTimestamp(index: Int, timeMs: Long?) {
        val lines = _lyrics.value.toMutableList()
        if (index in lines.indices) {
            lines[index] = lines[index].copy(timestampMs = timeMs)
            _lyrics.value = lines
        }
    }

    fun clearTimestamp(index: Int) {
        setLineTimestamp(index, null)
    }

    fun resetAllTimestamps() {
        val cleared = _lyrics.value.map { it.copy(timestampMs = null) }
        _lyrics.value = cleared
        _currentSyncIndex.value = 0
        _snackbarMessage.value = "Semua tanda waktu telah direset."
    }

    fun jumpToLine(index: Int) {
        val lines = _lyrics.value
        if (index in lines.indices) {
            val targetTime = lines[index].timestampMs ?: return
            playerManager.seekTo(targetTime)
        }
    }

    fun playPause() {
        playerManager.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun skipBackward() {
        playerManager.skipBy(-2000L)
    }

    fun skipForward() {
        playerManager.skipBy(2000L)
    }

    fun setPlaybackSpeed(speed: Float) {
        playerManager.setPlaybackSpeed(speed)
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun generateLrcContent(): String {
        return LrcUtils.generateLrc(
            songInfo = _songInfo.value,
            lyrics = _lyrics.value
        )
    }

    private fun triggerHapticFeedback() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(35)
                }
            }
        } catch (e: Exception) {
            // Haptics optional
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
