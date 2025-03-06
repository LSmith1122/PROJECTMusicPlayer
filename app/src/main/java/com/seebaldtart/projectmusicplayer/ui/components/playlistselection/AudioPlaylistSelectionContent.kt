@file:Suppress(
    "FunctionName",
    "UnusedMaterial3ScaffoldPaddingParameter"
)

package com.seebaldtart.projectmusicplayer.ui.components.playlistselection

import android.content.Context
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.seebaldtart.projectmusicplayer.R
import com.seebaldtart.projectmusicplayer.models.AudioTrack
import com.seebaldtart.projectmusicplayer.models.enums.GroupSelectionState
import com.seebaldtart.projectmusicplayer.models.enums.PlaybackError
import com.seebaldtart.projectmusicplayer.viewmodels.AudioPlayListViewModel
import com.seebaldtart.projectmusicplayer.viewmodels.MediaPlayerStateViewModel
import kotlinx.coroutines.flow.map
import kotlin.jvm.optionals.getOrNull

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudioPlaylistSelectionContent(
    innerPadding: PaddingValues,
    audioPlayListViewModel: AudioPlayListViewModel,
    mediaPlayerStateViewModel: MediaPlayerStateViewModel
) {
    val context = LocalContext.current
    mediaPlayerStateViewModel.setDelegate(object : MediaPlayerStateViewModel.Delegate {
        override fun getContext(): Context = context

        override fun onTrackAutomaticallyUpdated(track: AudioTrack?) {
            audioPlayListViewModel.onAudioTrackSelected(track)
        }

        override fun onError(error: PlaybackError) {
            // TODO: Display a Toast or SnackBar
            val message = "Playback Error: $error"
            Log.e(
                "AudioPlayListSelection",
                message,
                RuntimeException(message)
            )
        }

        override fun onRequestPlayList(): List<AudioTrack> {
            return audioPlayListViewModel.selectedPlayListState.value
        }
    })

    Column(modifier = Modifier.padding(innerPadding)) {
        val listItemsState = audioPlayListViewModel.streamByGroup().collectAsState()
        val audioGroupData by listItemsState
        val availableGroups = GroupSelectionState
            .entries
            .filter { it.isEligible }
            .mapIndexed { index, state -> state to index }
        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { availableGroups.size }
        )

        LaunchedEffect(audioGroupData) {
            pagerState.scrollToPage(
                page = availableGroups.indexOfFirst { it.first == audioGroupData.groupSelectionState }
            )
        }

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            pageSize = PageSize.Fill,
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.colorPrimaryDark))
        ) { pageIndex ->
            when (val state = availableGroups[pageIndex].first) {
                GroupSelectionState.ALL_TRACKS -> {
                    AudioSelectionView(
                        tracks = audioGroupData.tracks,
                        selectedTrackIDState = audioPlayListViewModel
                            .selectedTrack
                            .map { it.getOrNull()?.id ?: -1 }
                            .collectAsState(-1),
                        onAudioTrackSelected = { track ->
                            audioPlayListViewModel.onAudioTrackSelected(track)
                            mediaPlayerStateViewModel.onAudioTrackSelected(track)
                            mediaPlayerStateViewModel.setCurrentPlaylist(audioPlayListViewModel.selectedPlayListState.value)
                            mediaPlayerStateViewModel.onPlayClicked(context)
                        },
                        onAudioTrackShown = {
                            audioPlayListViewModel.loadThumbnailsForTrack(
                                context,
                                it
                            )
                        }
                    )
                }
                GroupSelectionState.ARTISTS,
                GroupSelectionState.ALBUMS,
                GroupSelectionState.YEARS,
                GroupSelectionState.GENRES -> GroupSelectionView(
                    groupItemsState = listItemsState,
                    onAudioGroupItemSelected = {
                        audioPlayListViewModel.onPlayListSelected(state, it)
                    },
                    onViewShown = {
                        audioPlayListViewModel.loadThumbnailsForGroupDetails(
                            context,
                            it
                        )
                    }
                )
            }
        }
    }
}
