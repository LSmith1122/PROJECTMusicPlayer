package com.seebaldtart.projectmusicplayer.ui.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.seebaldtart.projectmusicplayer.models.AudioTrack
import com.seebaldtart.projectmusicplayer.models.enums.GroupItemSelectionState
import com.seebaldtart.projectmusicplayer.models.enums.PlaybackError
import com.seebaldtart.projectmusicplayer.models.enums.TrackState
import com.seebaldtart.projectmusicplayer.repositories.AudioTrackRepository
import com.seebaldtart.projectmusicplayer.ui.components.MediaPlayerController
import com.seebaldtart.projectmusicplayer.ui.navigation.MusicPlayerNavGraph
import com.seebaldtart.projectmusicplayer.ui.navigation.Navigation
import com.seebaldtart.projectmusicplayer.utils.PermissionHelper
import com.seebaldtart.projectmusicplayer.viewmodels.AudioPlayListViewModel
import com.seebaldtart.projectmusicplayer.viewmodels.MediaPlayerStateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

@AndroidEntryPoint
class MusicPlayerActivity : FragmentActivity() {

    @Inject
    lateinit var audioTrackRepo: AudioTrackRepository
    private lateinit var navController: NavHostController
    private val audioPlayListViewModel: AudioPlayListViewModel by viewModels()
    private val mediaPlayerStateViewModel: MediaPlayerStateViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            navController = rememberNavController()
            val navGraph = remember(navController) {
                navController.createGraph(
                    startDestination = Navigation.PlaylistSelectionNavigation,
                    builder = MusicPlayerNavGraph(
                        playlistSelectionViewModel = audioPlayListViewModel,
                        mediaPlayerStateViewModel = mediaPlayerStateViewModel,
                        mediaControlsContent = { minimize, showTrack ->
                            MediaControlsContent(minimize, showTrack)
                        }
                    )
                )
            }
            NavHost(navController = navController, graph = navGraph)
            navController.setLifecycleOwner(this)
            navController.setOnBackPressedDispatcher(OnBackPressedDispatcher())

            LaunchedEffect(audioPlayListViewModel.groupItemSelectionState) {
                lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.CREATED) {
                        audioPlayListViewModel.groupItemSelectionState
                            .onEach {
                                handleFragmentByState(it)
                            }.collect()
                    }
                }
            }
        }

        PermissionHelper.checkNecessaryPermissions(
            activity = this,
            onSuccess = {
                audioTrackRepo.initialize()
                mediaPlayerStateViewModel.setDelegate(object : MediaPlayerStateViewModel.Delegate {
                    override fun getContext(): Context = this@MusicPlayerActivity

                    override fun onTrackAutomaticallyUpdated(track: AudioTrack?) {
                        audioPlayListViewModel.onAudioTrackSelected(track)
                    }

                    override fun onError(error: PlaybackError) {
                        // TODO: Display a Toast or SnackBar
                        val message = "Playback Error: $error"
                        Log.e(
                            this@MusicPlayerActivity::class.simpleName,
                            message,
                            RuntimeException(message)
                        )
                    }

                    override fun onRequestPlayList(): List<AudioTrack> {
                        return audioPlayListViewModel.selectedPlayListState.value
                    }
                })
            },
            onFailure = {
                finishAffinity()
            }
        )
    }

    @Composable
    private fun MediaControlsContent(
        isMinimized: Boolean = false,
        showNowPlayingTrack: Boolean = false
    ) {
        val context = LocalContext.current
        val track by mediaPlayerStateViewModel.nowPlayingTrack
            .map { it.getOrNull() }
            .collectAsState(null)

        MediaPlayerController(
            isMinimized = isMinimized,
            showNowPlayingTrack = showNowPlayingTrack,
            track = track,
            trackDurationState = mediaPlayerStateViewModel.nowPlayingTrack
                .map { it.getOrNull()?.duration?.toFloat() ?: 0F }.collectAsState(0F),
            playTimeProgressState = mediaPlayerStateViewModel.nowPlayingTrackProgress
                .map { it.toFloat() }
                .collectAsState(0F),
            isAudioPlayingState = mediaPlayerStateViewModel.getTrackState()
                .map { it == TrackState.PLAYING }
                .collectAsState(false),
            onPlayClicked = { mediaPlayerStateViewModel.onPlayClicked(context) },
            onPauseClicked = { mediaPlayerStateViewModel.onPauseClicked() },
            onPreviousClicked = { mediaPlayerStateViewModel.onPreviousClicked(context) },
            onNextClicked = { mediaPlayerStateViewModel.onNextClicked(context) },
            onSeekPositionChanged = {
                mediaPlayerStateViewModel.updateTrackCurrentPosition(it.toInt())
            },
            onViewShown = {
                track?.let { audioPlayListViewModel.loadThumbnailsForTrack(context, it) }
            },
            onNowPlayingTrackClicked = {
                track?.let {
                    audioPlayListViewModel.onAudioTrackSelected(it)
                    mediaPlayerStateViewModel.onAudioTrackSelected(it)
                    mediaPlayerStateViewModel.setCurrentPlaylist(audioPlayListViewModel.selectedPlayListState.value)
                    audioPlayListViewModel.setGroupItemSelectionState(GroupItemSelectionState.AUDIO_PLAY_DETAILS)
                }
            }
        )
    }

    private fun handleFragmentByState(state: GroupItemSelectionState) {
        when (state) {
            GroupItemSelectionState.AUDIO_PLAY_LIST_SELECTION -> showAudioPlayListSelection()
            GroupItemSelectionState.AUDIO_PLAY_LIST -> showAudioPlayList()
            GroupItemSelectionState.AUDIO_PLAY_DETAILS -> showAudioDetails()
        }
    }

    private fun showAudioPlayListSelection() {
        navController.navigate(Navigation.PlaylistSelectionNavigation)
    }

    private fun showAudioPlayList() {
        navController.navigate(Navigation.PlaylistNavigation)
    }

    private fun showAudioDetails() {
        navController.navigate(Navigation.AudioDetailsNavigation)
    }
}
