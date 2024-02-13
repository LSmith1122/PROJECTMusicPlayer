@file:Suppress(
    "FunctionName",
    "UnusedMaterial3ScaffoldPaddingParameter"
)
package com.seebaldtart.projectmusicplayer.ui.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seebaldtart.projectmusicplayer.R
import com.seebaldtart.projectmusicplayer.models.AudioTrack
import com.seebaldtart.projectmusicplayer.models.data.enums.PlaybackError
import com.seebaldtart.projectmusicplayer.models.data.enums.TrackState
import com.seebaldtart.projectmusicplayer.ui.components.AudioTrackListItem
import com.seebaldtart.projectmusicplayer.ui.components.MediaPlayerController
import com.seebaldtart.projectmusicplayer.ui.components.MediaPlayerController_Preview
import com.seebaldtart.projectmusicplayer.ui.theme.PROJECTMusicPlayerTheme
import com.seebaldtart.projectmusicplayer.viewmodels.AudioPlayListViewModel
import com.seebaldtart.projectmusicplayer.viewmodels.MusicPlayerStateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.lang.RuntimeException
import kotlin.jvm.optionals.getOrNull

@AndroidEntryPoint
class MusicPlayerActivity : ComponentActivity() {

    private lateinit var playlistViewModel: AudioPlayListViewModel
    private lateinit var musicPlayerViewModel: MusicPlayerStateViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PROJECTMusicPlayerTheme {
                playlistViewModel = hiltViewModel<AudioPlayListViewModel>()
                musicPlayerViewModel = hiltViewModel<MusicPlayerStateViewModel>()
                musicPlayerViewModel.setDelegate(object : MusicPlayerStateViewModel.Delegate {
                    override fun onTrackAutomaticallyUpdated(track: AudioTrack?) {
                        playlistViewModel.onAudioTrackSelected(track)
                    }

                    override fun onError(error: PlaybackError) {
                        // TODO: Display a Toast or SnackBar
                        val message = "Playback Error: $error"
                        Log.e(this@MusicPlayerActivity::class.simpleName, message, RuntimeException(message))
                    }
                })
                LaunchedEffect(true) {
                    playlistViewModel.stream()
                        .onEach {
                            musicPlayerViewModel.setCurrentPlaylist(it)
                        }.collect()
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { _ ->
                    Column {
                        MainContent(
                            this,
                            tracks = playlistViewModel.stream().collectAsState(),
                            selectedTrackIDState = playlistViewModel
                                .selectedTrack
                                .map { it.getOrNull()?.id ?: -1 }
                                .collectAsState(-1),
                            onAudioTrackSelected = { track ->
                                playlistViewModel.onAudioTrackSelected(track)
                                musicPlayerViewModel.onAudioTrackSelected(track)
                                musicPlayerViewModel.onPlayClicked(this@MusicPlayerActivity)
                            },
                            onAudioTrackShown = playlistViewModel::loadThumbnailsForTrack
                        )
                        MediaPlayerController(
                            selectedTrackDuration = musicPlayerViewModel.nowPlayingTrack
                                .map {
                                    if (it.isPresent) {
                                        it.get().duration.toFloat()
                                    } else {
                                        0F
                                    }
                                }.collectAsState(0F),
                            playTimeProgress = musicPlayerViewModel.nowPlayingTrackProgress
                                .map { it.toFloat() }
                                .collectAsState(0F),
                            isAudioPlayingState = musicPlayerViewModel.getTrackState()
                                .map { it == TrackState.PLAYING }
                                .collectAsState(false),
                            onPlayClicked = { musicPlayerViewModel.onPlayClicked(this@MusicPlayerActivity) },
                            onPauseClicked = { musicPlayerViewModel.onPauseClicked() },
                            onPreviousClicked = { musicPlayerViewModel.onPreviousClicked(this@MusicPlayerActivity) },
                            onNextClicked = { musicPlayerViewModel.onNextClicked(this@MusicPlayerActivity) },
                            onSeekPositionChanged = {
                                musicPlayerViewModel.updateTrackCurrentPosition(
                                    it.toInt()
                                )
                            }
                        )
                    }
                }
            }
        }

        val readExternalPermission = "android.permission.READ_EXTERNAL_STORAGE"
        val readMediaAudioPermission = "android.permission.READ_MEDIA_AUDIO"
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            var missingPermission = false
            permissions.entries.find { it.key == readExternalPermission }
                ?.let { readExternal ->
                    val isValidVersion = Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2
                    if (isValidVersion && !readExternal.value) {
                        missingPermission = true
                    }
                }
            permissions.entries.find { it.key == readMediaAudioPermission }
                ?.let { readMediaAudio ->
                    if (!readMediaAudio.value) {
                        missingPermission = true
                    }
                }

            if (missingPermission) {
                finishAffinity()
            }
        }.launch(arrayOf(
            readExternalPermission,
            readMediaAudioPermission
        ))
    }
}

@Composable
fun MainContent(
    scope: ColumnScope,
    tracks: State<List<AudioTrack>>,
    selectedTrackIDState: State<Long>,
    onAudioTrackSelected: (AudioTrack) -> Unit,
    onAudioTrackShown: (AudioTrack) -> Unit
) {
    scope.run {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F, true)
                .background(colorResource(R.color.colorPrimaryDark))
                .padding(horizontal = 4.dp)
                .padding(top = 16.dp)
        ) {
            val tracksState by tracks
            items(
                items = tracksState,
                key = { it.id },
                contentType = { track -> track }
            ) { track ->
                AudioTrackListItem(
                    id = track.id,
                    title = track.title,
                    artist = track.artistName,
                    album = track.albumName,
                    duration = track.duration,
                    imageState = track.getThumbnailBitmap().collectAsState(),
                    selectedIDState = selectedTrackIDState,
                    onAudioTrackSelected = {
                        onAudioTrackSelected.invoke(track)
                    },
                    onViewShown = {
                        onAudioTrackShown.invoke(track)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    PROJECTMusicPlayerTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { _ ->
            Column {
                MainContent(
                    scope = this,
                    tracks = remember { mutableStateOf(
                        listOf(
                            AudioTrack(0, 0, "Title",  -1, "John Doe", "Example Album", 300, null, ""),
                            AudioTrack(1, 0, "Title",  -1, "John Doe", "Example Album", 300, null, ""),
                            AudioTrack(2, 0, "Title",  -1, "John Doe", "Example Album", 300, null, ""),
                            AudioTrack(3, 0, "Title",  -1, "John Doe", "Example Album", 300, null, ""),
                            AudioTrack(4, 0, "Title",  -1, "John Doe", "Example Album", 300, null, ""),
                            AudioTrack(5, 0, "Title",  -1, "John Doe", "Example Album", 300, null, ""),
                            AudioTrack(6, 0, "Title",  -1, "John Doe", "Example Album", 300, null, ""),
                            AudioTrack(7, 0, "Title",  -1, "John Doe", "Example Album", 300, null, ""),
                            AudioTrack(8, 0, "Title",  -1, "John Doe", "Example Album", 300, null, ""),
                            AudioTrack(9, 0, "Title",  -1, "John Doe", "Example Album", 300, null, ""),
                            AudioTrack(10, 0, "Title",  -1, "John Doe", "Example Album", 300, null, ""),
                            AudioTrack(11, 0, "Title",  -1, "John Doe", "Example Album", 300, null, ""),
                            AudioTrack(13, 0, "Title",  -1, "John Doe", "Example Album", 300, null, ""),
                            AudioTrack(14, 0, "Title",  -1, "John Doe", "Example Album", 300, null, ""),
                        ))
                    },
                    selectedTrackIDState = remember { mutableLongStateOf(-1) },
                    onAudioTrackSelected = {},
                    onAudioTrackShown = {}
                )
                MediaPlayerController_Preview()
            }
        }
    }
}