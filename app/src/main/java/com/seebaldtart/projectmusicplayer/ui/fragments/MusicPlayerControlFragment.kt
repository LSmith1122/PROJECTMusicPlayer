package com.seebaldtart.projectmusicplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.seebaldtart.projectmusicplayer.models.data.enums.TrackState
import com.seebaldtart.projectmusicplayer.ui.components.MediaPlayerController
import com.seebaldtart.projectmusicplayer.viewmodels.MediaPlayerStateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class MusicPlayerControlFragment : Fragment() {

    private val viewModel by activityViewModels<MediaPlayerStateViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MediaPlayerController(
                    selectedTrackDuration = viewModel.nowPlayingTrack
                        .map {
                            if (it.isPresent) {
                                it.get().duration.toFloat()
                            } else {
                                0F
                            }
                        }.collectAsState(0F),
                    playTimeProgress = viewModel.nowPlayingTrackProgress
                        .map { it.toFloat() }
                        .collectAsState(0F),
                    isAudioPlayingState = viewModel.getTrackState()
                        .map { it == TrackState.PLAYING }
                        .collectAsState(false),
                    onPlayClicked = { viewModel.onPlayClicked(requireContext()) },
                    onPauseClicked = { viewModel.onPauseClicked() },
                    onPreviousClicked = { viewModel.onPreviousClicked(requireContext()) },
                    onNextClicked = { viewModel.onNextClicked(requireContext()) },
                    onSeekPositionChanged = {
                        viewModel.updateTrackCurrentPosition(
                            it.toInt()
                        )
                    }
                )
            }
        }
    }
}