@file:Suppress(
    "FunctionName",
    "UnusedMaterial3ScaffoldPaddingParameter"
)

package com.seebaldtart.projectmusicplayer.ui.components.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.seebaldtart.projectmusicplayer.ui.components.AudioTrackListItem
import com.seebaldtart.projectmusicplayer.viewmodels.AudioPlayListViewModel
import com.seebaldtart.projectmusicplayer.viewmodels.MediaPlayerStateViewModel
import kotlinx.coroutines.flow.map
import kotlin.jvm.optionals.getOrNull

@Composable
fun AudioPlaylistContent(
    innerPadding: PaddingValues,
    playlistSelectionViewModel: AudioPlayListViewModel,
    mediaPlayerStateViewModel: MediaPlayerStateViewModel
) {
    val context = LocalContext.current
    val tracksState by playlistSelectionViewModel.selectedPlayListState.collectAsState()
    Column(modifier = Modifier.padding(innerPadding)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondary)
                .padding(horizontal = 4.dp)
        ) {
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
                    selectedIDState = playlistSelectionViewModel
                        .selectedTrack
                        .map { it.getOrNull()?.id ?: -1 }
                        .collectAsState(-1),
                    onAudioTrackSelected = {
                        playlistSelectionViewModel.onAudioTrackSelected(track)
                        mediaPlayerStateViewModel.onAudioTrackSelected(track)
                        mediaPlayerStateViewModel.setCurrentPlaylist(playlistSelectionViewModel.selectedPlayListState.value)
                        mediaPlayerStateViewModel.onPlayClicked(context)
                    },
                    onViewShown = {
                        playlistSelectionViewModel.loadThumbnailsForTrack(context, track)
                    }
                )
            }
        }
    }
}