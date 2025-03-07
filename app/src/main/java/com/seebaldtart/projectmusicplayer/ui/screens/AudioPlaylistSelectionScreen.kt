package com.seebaldtart.projectmusicplayer.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import com.seebaldtart.projectmusicplayer.R
import com.seebaldtart.projectmusicplayer.models.AudioGroupData
import com.seebaldtart.projectmusicplayer.models.AudioGroupDetails
import com.seebaldtart.projectmusicplayer.models.AudioTrack
import com.seebaldtart.projectmusicplayer.models.enums.GroupSelectionState
import com.seebaldtart.projectmusicplayer.ui.components.AudioTrackListItem
import com.seebaldtart.projectmusicplayer.ui.components.PlaylistIndexScrollBar
import com.seebaldtart.projectmusicplayer.ui.components.playlistselection.GroupSelectionView
import com.seebaldtart.projectmusicplayer.ui.theme.PROJECTMusicPlayerTheme
import com.seebaldtart.projectmusicplayer.viewmodels.AudioPlayListViewModel
import com.seebaldtart.projectmusicplayer.viewmodels.MediaPlayerStateViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun AudioPlaylistSelectionScreen(
    audioPlaylistViewModel: AudioPlayListViewModel,
    mediaPlayerViewModel: MediaPlayerStateViewModel,
    mediaControlsContent: @Composable ((Boolean, Boolean) -> Unit)
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary)
    )
    Scaffold(
        topBar = {
            PlaylistSelectionAppBar(
                scrollBehavior = scrollBehavior,
                selectedGroupState = audioPlaylistViewModel.groupSelectionState.collectAsState(),
                onGroupSelected = { audioPlaylistViewModel.onGroupSelected(it) }
            )
        },
        bottomBar = { mediaControlsContent.invoke(true, true) },
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            MainContent(
                scope = this,
                listItemsState = audioPlaylistViewModel.streamByGroup().collectAsState(),
                selectedTrackIDState = audioPlaylistViewModel
                    .selectedTrack
                    .map { it.getOrNull()?.id ?: -1 }
                    .collectAsState(-1),
                paletteOptionalState = audioPlaylistViewModel
                    .selectedTrack
                    .flatMapConcat {
                        it.getOrNull()?.getPaletteForBitmap(
                            coroutineScope = scope,
                            numberOfColors = 24
                        ) ?: emptyFlow()
                    }.collectAsState(Optional.empty<Palette>()),
                onAudioTrackSelected = { track ->
                    audioPlaylistViewModel.onAudioTrackSelected(track)
                    mediaPlayerViewModel.onAudioTrackSelected(track)
                    mediaPlayerViewModel.setCurrentPlaylist(audioPlaylistViewModel.selectedPlayListState.value)
                    mediaPlayerViewModel.onPlayClicked(context)
                },
                onAudioTrackShown = {
                    audioPlaylistViewModel.loadThumbnailsForTrack(context, it)
                },
                onAudioGroupItemSelected = {
                    audioPlaylistViewModel.onPlayListSelected(it.first, it.second)
                },
                onAudioGroupItemShown = {
                    audioPlaylistViewModel.loadThumbnailsForGroupDetails(context, it)
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainContent(
    scope: ColumnScope,
    listItemsState: State<AudioGroupData>,
    selectedTrackIDState: State<Long>,
    paletteOptionalState: State<Optional<Palette>>,
    onAudioTrackSelected: (AudioTrack) -> Unit,
    onAudioTrackShown: (AudioTrack) -> Unit,
    onAudioGroupItemSelected: (Pair<GroupSelectionState, AudioGroupDetails>) -> Unit,
    onAudioGroupItemShown: (AudioGroupDetails) -> Unit
) {
    scope.run {
        val audioGroupData by listItemsState
        val availableGroups = GroupSelectionState
            .entries
            .filter { it.isEligible }
            .mapIndexed { index, state -> state to index }
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { availableGroups.size })

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
            val state = availableGroups[pageIndex].first
            val paletteOptional by paletteOptionalState
            Box(modifier = Modifier.fillMaxSize()) {
                when (state) {
                    GroupSelectionState.ALL_TRACKS -> {
                        AudioSelectionView(
                            tracks = audioGroupData.tracks,
                            paletteOptional = paletteOptional,
                            selectedTrackIDState = selectedTrackIDState,
                            onAudioTrackSelected = onAudioTrackSelected,
                            onAudioTrackShown = onAudioTrackShown
                        )
                    }
                    GroupSelectionState.ARTISTS,
                    GroupSelectionState.ALBUMS,
                    GroupSelectionState.YEARS,
                    GroupSelectionState.GENRES -> GroupSelectionView(
                        groupItemsState = listItemsState,
                        paletteOptional = paletteOptional,
                        onAudioGroupItemSelected = {
                            onAudioGroupItemSelected(state to it)
                        },
                        onViewShown = {
                            onAudioGroupItemShown.invoke(it)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistSelectionAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    selectedGroupState: State<GroupSelectionState>,
    onGroupSelected: (GroupSelectionState) -> Unit
) {
    PROJECTMusicPlayerTheme {
        MediumTopAppBar(
            modifier = Modifier
                .navigationBarsPadding()
                .background(Color.Transparent),
            scrollBehavior = scrollBehavior,
            navigationIcon = {
                IconButton(onClick = {
                    // TODO: Finish implementation...
                }) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = stringResource(R.string.content_description_options_menu),
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                titleContentColor = Color.White
            ),
            title = {
                LazyRow {
                    items(
                        items = GroupSelectionState
                            .entries
                            .filter { it.isEligible }
                            .map { GroupSelectionTab(it) { onGroupSelected(it) } },
                        key = { it.groupSelectionState.resID }
                    ) {
                        val selectedID by selectedGroupState
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val background by remember(selectedID) {
                            mutableStateOf(
                                if (selectedID.name == it.groupSelectionState.name) {
                                    primaryColor
                                } else {
                                    Color.Transparent
                                }
                            )
                        }
                        Button(
                            onClick = it.onClick,
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonColors(
                                containerColor = background,
                                contentColor = Color.White,
                                disabledContentColor = Color.White,
                                disabledContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = stringResource(it.groupSelectionState.resID),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun AudioSelectionView(
    tracks: List<AudioTrack>,
    selectedTrackIDState: State<Long>,
    paletteOptional: Optional<Palette>,
    onAudioTrackSelected: (AudioTrack) -> Unit,
    onAudioTrackShown: (AudioTrack) -> Unit
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
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

    PlaylistIndexScrollBar(
        sources = tracks.map { it.title },
        paletteOptional = paletteOptional,
        onIndexSelected = { indexCharacter ->
            val indexOfFirst = tracks.indexOfFirst {
                if (!indexCharacter.isLetter()) {
                    !it.title.first().isLetter()
                } else {
                    it.title.first().uppercase() == indexCharacter.toString()
                }
            }
            scope.launch(Dispatchers.Main) {
                listState.scrollToItem(indexOfFirst)
            }
        }
    )
}

private data class GroupSelectionTab (
    val groupSelectionState: GroupSelectionState,
    val onClick: () -> Unit
)
