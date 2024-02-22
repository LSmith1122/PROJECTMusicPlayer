@file:Suppress(
    "FunctionName",
    "UnusedMaterial3ScaffoldPaddingParameter"
)

package com.seebaldtart.projectmusicplayer.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.seebaldtart.projectmusicplayer.R
import com.seebaldtart.projectmusicplayer.models.AudioGroupData
import com.seebaldtart.projectmusicplayer.models.AudioGroupDetails
import com.seebaldtart.projectmusicplayer.models.AudioTrack
import com.seebaldtart.projectmusicplayer.models.enums.GroupSelectionState
import com.seebaldtart.projectmusicplayer.models.enums.PlaybackError
import com.seebaldtart.projectmusicplayer.ui.components.AudioGroupListItem
import com.seebaldtart.projectmusicplayer.ui.components.AudioTrackListItem
import com.seebaldtart.projectmusicplayer.ui.theme.PROJECTMusicPlayerTheme
import com.seebaldtart.projectmusicplayer.viewmodels.AudioPlayListViewModel
import com.seebaldtart.projectmusicplayer.viewmodels.MediaPlayerStateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import kotlin.jvm.optionals.getOrNull

@AndroidEntryPoint
class AudioPlayListSelectionFragment : Fragment() {

    private val viewModel by activityViewModels<AudioPlayListViewModel>()
    private val mediaPlayerViewModel by activityViewModels<MediaPlayerStateViewModel>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                mediaPlayerViewModel.setDelegate(object : MediaPlayerStateViewModel.Delegate {
                    override fun onTrackAutomaticallyUpdated(track: AudioTrack?) {
                        viewModel.onAudioTrackSelected(track)
                    }

                    override fun onError(error: PlaybackError) {
                        // TODO: Display a Toast or SnackBar
                        val message = "Playback Error: $error"
                        Log.e(
                            this@AudioPlayListSelectionFragment::class.simpleName,
                            message,
                            RuntimeException(message)
                        )
                    }

                    override fun onRequestPlayList(): List<AudioTrack> {
                        return viewModel.selectedPlayListState.value
                    }
                })

                PROJECTMusicPlayerTheme {
                    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
                    Scaffold(
                        topBar = {
                            MainAppBar(
                                scrollBehavior = scrollBehavior,
                                selectedGroupState = viewModel.groupSelectionState.collectAsState(),
                                onGroupSelected = { viewModel.onGroupSelected(it) }
                            )
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                    ) { innerPadding ->
                        Column(modifier = Modifier.padding(innerPadding)) {
                            MainContent(
                                this,
                                listItemsState = viewModel.streamByGroup().collectAsState(),
                                selectedTrackIDState = viewModel
                                    .selectedTrack
                                    .map { it.getOrNull()?.id ?: -1 }
                                    .collectAsState(-1),
                                onAudioTrackSelected = { track ->
                                    viewModel.onAudioTrackSelected(track)
                                    mediaPlayerViewModel.onAudioTrackSelected(track)
                                    mediaPlayerViewModel.setCurrentPlaylist(viewModel.selectedPlayListState.value)
                                    mediaPlayerViewModel.onPlayClicked(requireContext())
                                },
                                onAudioTrackShown = viewModel::loadThumbnailsForTrack,
                                onAudioGroupItemSelected = {
                                    viewModel.onPlayListSelected(it.first, it.second)
                                },
                                onAudioGroupItemShown = viewModel::loadThumbnailsForGroupDetails
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun MainContent(
        scope: ColumnScope,
        listItemsState: State<AudioGroupData>,
        selectedTrackIDState: State<Long>,
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
                when (state) {
                    GroupSelectionState.ALL_TRACKS -> {
                        AudioSelectionView(
                            tracks = audioGroupData.tracks,
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

    @Composable
    private fun AudioSelectionView(
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

    @Composable
    private fun GroupSelectionView(
        groupItemsState: State<AudioGroupData>,
        onAudioGroupItemSelected: (AudioGroupDetails) -> Unit,
        onViewShown: (AudioGroupDetails) -> Unit
    ) {
        var size by remember { mutableStateOf(Size.Zero) }
        val groupItemsData by groupItemsState
        val items by remember { mutableStateOf(groupItemsData.groupDetails ?: emptyList()) }
        val maxRowCount = when (groupItemsData.groupSelectionState) {
            GroupSelectionState.ALL_TRACKS,
            GroupSelectionState.ALBUMS,
            GroupSelectionState.ARTISTS -> 3
            GroupSelectionState.YEARS,
            GroupSelectionState.GENRES -> 4
        }

        // Workaround to prevent restructuring the grid and items when going back to "Track".
        // The "Track" section might take a bit longer to load than this composition would take
        // to finish, resulting in showing the grid updating right before displaying all tracks.
        if (groupItemsData.groupSelectionState == GroupSelectionState.ALL_TRACKS) {
            return
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(maxRowCount),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
                .padding(horizontal = 8.dp)
                .onGloballyPositioned { size = it.size.toSize() }
        ) {
            items(items = items) { audioGroupDetails ->
                AudioGroupListItem(
                    id = audioGroupDetails.id,
                    name = audioGroupDetails.name,
                    description = audioGroupDetails.description,
                    height = size.width,
                    imageState = audioGroupDetails.getThumbnailBitmap().collectAsState(),
                    onAudioGroupSelected = {
                        onAudioGroupItemSelected.invoke(audioGroupDetails)
                    },
                    onViewShown = {
                        onViewShown.invoke(audioGroupDetails)
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainAppBar(
        scrollBehavior: TopAppBarScrollBehavior,
        selectedGroupState: State<GroupSelectionState>,
        onGroupSelected: (GroupSelectionState) -> Unit
    ) {
        PROJECTMusicPlayerTheme {
            MediumTopAppBar(
                modifier = Modifier.navigationBarsPadding(),
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
                    containerColor = MaterialTheme.colorScheme.secondary,
                    scrolledContainerColor = MaterialTheme.colorScheme.secondary,
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

    private data class GroupSelectionTab (
        val groupSelectionState: GroupSelectionState,
        val onClick: () -> Unit
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("MutableCollectionMutableState")
    @Preview(showBackground = true)
    @Composable
    private fun MainContent_Preview() {
    PROJECTMusicPlayerTheme {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        Scaffold(
            topBar = {
                MainAppBar(
                    scrollBehavior = scrollBehavior,
                    selectedGroupState = remember { mutableStateOf(GroupSelectionState.ALL_TRACKS) },
                    onGroupSelected = {}
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { innerPadding ->
            Column (modifier = Modifier.padding(innerPadding)) {
                val track: (Long) -> AudioTrack = { id ->
                    AudioTrack(
                        id = id,
                        trackNumber = 0,
                        title = "Title",
                        artistID = -1,
                        artistName = "John Doe",
                        albumName = "Example Album",
                        year = 2024,
                        duration = 300,
                        genreID = null,
                        genre = null,
                        thumbnailUri = null,
                        path = "")
                }
                MainContent(
                    scope = this,
                    listItemsState = remember {
                        mutableStateOf(
                            AudioGroupData(
                                groupSelectionState = GroupSelectionState.ALL_TRACKS,
                                groupDetails = emptyList(),
                                tracks = mutableListOf<AudioTrack>().apply {
                                    for (i in 0..20) {
                                        add(track(i.toLong()))
                                    }
                                }
                            )
                        )
                    },
                    selectedTrackIDState = remember { mutableLongStateOf(-1) },
                    onAudioTrackSelected = {},
                    onAudioTrackShown = {},
                    onAudioGroupItemSelected = {},
                    onAudioGroupItemShown = {}
                )
            }
        }
    }
    }
}
