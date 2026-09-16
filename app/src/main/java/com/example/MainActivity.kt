package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.MusicLibraryBottomSheet
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.PreviewScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.screens.SyncStudioScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioCyan
import com.example.viewmodel.LrcStudioViewModel
import com.example.viewmodel.StudioTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LrcStudioApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LrcStudioApp(viewModel: LrcStudioViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    val songInfo by viewModel.songInfo.collectAsState()
    val lyrics by viewModel.lyrics.collectAsState()
    val waveformBars by viewModel.waveformBars.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val currentSyncIndex by viewModel.currentSyncIndex.collectAsState()
    val activePreviewIndex by viewModel.activePreviewIndex.collectAsState()

    val showMusicDialog by viewModel.showMusicLibraryDialog.collectAsState()
    val filteredTracks by viewModel.filteredLocalTracks.collectAsState()
    val searchQuery by viewModel.searchMusicQuery.collectAsState()
    val isLoadingMusic by viewModel.isLoadingMusic.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lishrik Studio",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("studio_bottom_nav")
            ) {
                val tabs = listOf(
                    StudioTab.SETUP to Icons.Default.Audiotrack,
                    StudioTab.SYNC to Icons.Default.GraphicEq,
                    StudioTab.PREVIEW to Icons.Default.Mic,
                    StudioTab.EXPORT to Icons.Default.FileDownload
                )

                tabs.forEach { (tab, icon) ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setTab(tab) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title, fontSize = 11.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = StudioCyan
                        ),
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                StudioTab.SETUP -> {
                    SetupScreen(
                        songInfo = songInfo,
                        lyrics = lyrics,
                        onLoadDemo = { viewModel.loadDemoSong() },
                        onLoadAudioUri = { uri, title, artist ->
                            viewModel.loadCustomAudio(uri, title, artist)
                        },
                        onOpenLocalMusicDialog = { viewModel.setShowMusicLibraryDialog(true) },
                        onUpdateMetadata = { title, artist, album ->
                            viewModel.updateMetadata(title, artist, album)
                        },
                        onUpdateLyrics = { text -> viewModel.setRawLyrics(text) },
                        onCleanEmptyLines = { viewModel.cleanEmptyLines() },
                        onNavigateToSync = { viewModel.setTab(StudioTab.SYNC) },
                        onResetProject = { viewModel.resetProject() }
                    )
                }

                StudioTab.SYNC -> {
                    SyncStudioScreen(
                        songInfo = songInfo,
                        lyrics = lyrics,
                        waveformBars = waveformBars,
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        isPlaying = isPlaying,
                        playbackSpeed = playbackSpeed,
                        currentSyncIndex = currentSyncIndex,
                        onPlayPause = { viewModel.playPause() },
                        onSeek = { viewModel.seekTo(it) },
                        onSkipBackward = { viewModel.skipBackward() },
                        onSkipForward = { viewModel.skipForward() },
                        onSetPlaybackSpeed = { viewModel.setPlaybackSpeed(it) },
                        onStampCurrentLine = { viewModel.stampCurrentLine() },
                        onSelectSyncIndex = { viewModel.setCurrentSyncIndex(it) },
                        onNudgeTimestamp = { idx, delta -> viewModel.nudgeTimestamp(idx, delta) },
                        onClearTimestamp = { viewModel.clearTimestamp(it) },
                        onResetAllTimestamps = { viewModel.resetAllTimestamps() },
                        onJumpToLine = { viewModel.jumpToLine(it) }
                    )
                }

                StudioTab.PREVIEW -> {
                    PreviewScreen(
                        songInfo = songInfo,
                        lyrics = lyrics,
                        waveformBars = waveformBars,
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        isPlaying = isPlaying,
                        activeLineIndex = activePreviewIndex,
                        onPlayPause = { viewModel.playPause() },
                        onSeek = { viewModel.seekTo(it) },
                        onSkipBackward = { viewModel.skipBackward() },
                        onSkipForward = { viewModel.skipForward() },
                        onJumpToLine = { viewModel.jumpToLine(it) },
                        onNavigateToExport = { viewModel.setTab(StudioTab.EXPORT) }
                    )
                }

                StudioTab.EXPORT -> {
                    ExportScreen(
                        songInfo = songInfo,
                        lyrics = lyrics,
                        onShowSnackbar = { viewModel.showSnackbar(it) }
                    )
                }
            }
        }
    }

    // Music Library Modal Bottom Sheet
    MusicLibraryBottomSheet(
        visible = showMusicDialog,
        tracks = filteredTracks,
        searchQuery = searchQuery,
        isLoading = isLoadingMusic,
        onSearchChange = { viewModel.setSearchMusicQuery(it) },
        onTrackSelected = { viewModel.selectLocalTrack(it) },
        onScanRequested = { viewModel.scanLocalMusicLibrary() },
        onDismiss = { viewModel.setShowMusicLibraryDialog(false) }
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
