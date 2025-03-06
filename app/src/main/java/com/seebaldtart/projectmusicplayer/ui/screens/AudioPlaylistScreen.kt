package com.seebaldtart.projectmusicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.seebaldtart.projectmusicplayer.R
import com.seebaldtart.projectmusicplayer.models.AudioTrack
import com.seebaldtart.projectmusicplayer.models.enums.GroupItemSelectionState
import com.seebaldtart.projectmusicplayer.ui.components.AudioTrackListItem
import com.seebaldtart.projectmusicplayer.viewmodels.AudioPlayListViewModel
import com.seebaldtart.projectmusicplayer.viewmodels.MediaPlayerStateViewModel
import kotlinx.coroutines.flow.map
import kotlin.jvm.optionals.getOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlaylistScreen(
    audioPlaylistViewModel: AudioPlayListViewModel,
    mediaPlayerViewModel: MediaPlayerStateViewModel,
    mediaControlsContent: @Composable ((Boolean, Boolean) -> Unit)
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary)
    )
    Scaffold(
        topBar = {
            AudioPlaylistAppBar(
                scrollBehavior = scrollBehavior,
                onBackPressed = {
                    // We do not want to clear the Group Item Selection (via viewModel.clearGroupItemSelection())
                    // because we want the Media Player to continue playing the previously selected playlist
                    audioPlaylistViewModel.setGroupItemSelectionState(GroupItemSelectionState.AUDIO_PLAY_LIST_SELECTION)
                }
            )
        },
        bottomBar = { mediaControlsContent.invoke(true, true) },
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            AudioSelectionView(
                tracks = audioPlaylistViewModel.selectedPlayListState.collectAsState(),
                selectedTrackIDState = audioPlaylistViewModel
                    .selectedTrack
                    .map { it.getOrNull()?.id ?: -1 }
                    .collectAsState(-1),
                onAudioTrackSelected = { track ->
                    audioPlaylistViewModel.onAudioTrackSelected(track)
                    mediaPlayerViewModel.onAudioTrackSelected(track)
                    mediaPlayerViewModel.setCurrentPlaylist(audioPlaylistViewModel.selectedPlayListState.value)
                    mediaPlayerViewModel.onPlayClicked(context)
                    audioPlaylistViewModel.setGroupItemSelectionState(GroupItemSelectionState.AUDIO_PLAY_DETAILS)
                },
                onAudioTrackShown = { audioPlaylistViewModel.loadThumbnailsForTrack(context, it) }
            )
        }
    }
}

@Composable
private fun AudioSelectionView(
    tracks: State<List<AudioTrack>>,
    selectedTrackIDState: State<Long>,
    onAudioTrackSelected: (AudioTrack) -> Unit,
    onAudioTrackShown: (AudioTrack) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudioPlaylistAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onBackPressed: () -> Unit
) {
    TopAppBar(
        modifier = Modifier.navigationBarsPadding(),
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(onClick = {
                onBackPressed.invoke()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.content_description_options_menu),
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            scrolledContainerColor = MaterialTheme.colorScheme.secondary,
            titleContentColor = Color.White
        ),
        title = {
            stringResource(R.string.app_name)
        }
    )
}