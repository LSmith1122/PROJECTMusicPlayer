@file:Suppress(
    "FunctionName",
    "UnusedMaterial3ScaffoldPaddingParameter"
)

package com.seebaldtart.projectmusicplayer.ui.components.playlistselection

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seebaldtart.projectmusicplayer.models.AudioTrack
import com.seebaldtart.projectmusicplayer.ui.components.AudioTrackListItem

@Composable
fun AudioSelectionView(
    tracks: List<AudioTrack>,
    selectedTrackIDState: State<Long>,
    onAudioTrackSelected: (AudioTrack) -> Unit,
    onAudioTrackShown: (AudioTrack) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp)
    ) {
        items(
            items = tracks,
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
