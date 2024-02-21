@file:Suppress(
    "FunctionName",
    "UnusedMaterial3ScaffoldPaddingParameter"
)

package com.seebaldtart.projectmusicplayer.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.seebaldtart.projectmusicplayer.R
import com.seebaldtart.projectmusicplayer.models.AudioTrack
import com.seebaldtart.projectmusicplayer.models.enums.GroupItemSelectionState
import com.seebaldtart.projectmusicplayer.ui.components.AudioTrackListItem
import com.seebaldtart.projectmusicplayer.ui.theme.PROJECTMusicPlayerTheme
import com.seebaldtart.projectmusicplayer.viewmodels.AudioPlayListViewModel
import com.seebaldtart.projectmusicplayer.viewmodels.MediaPlayerStateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import kotlin.jvm.optionals.getOrNull

@AndroidEntryPoint
class AudioPlayListFragment : Fragment() {

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
//                mediaPlayerViewModel.setDelegate(object : MediaPlayerStateViewModel.Delegate {
//                    override fun onTrackAutomaticallyUpdated(track: AudioTrack?) {
//                        viewModel.onAudioTrackSelected(track)
//                    }
//
//                    override fun onError(error: PlaybackError) {
//                        // TODO: Display a Toast or SnackBar
//                        val message = "Playback Error: $error"
//                        Log.e(
//                            this@AudioPlayListFragment::class.simpleName,
//                            message,
//                            RuntimeException(message)
//                        )
//                    }
//                })

                PROJECTMusicPlayerTheme {
                    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                    Scaffold(
                        topBar = {
                            MainAppBar(
                                scrollBehavior = scrollBehavior,
                                onBackPressed = {
                                    // We do not want to clear the Group Item Selection (via viewModel.clearGroupItemSelection())
                                    // because we want the Media Player to continue playing the previously selected playlist
                                    viewModel.setGroupItemSelectionState(GroupItemSelectionState.AUDIO_PLAY_LIST_SELECTION)
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                    ) { innerPadding ->
                        Column(modifier = Modifier.padding(innerPadding)) {
                            MainContent(
                                this,
                                tracks = viewModel.selectedPlayListState.collectAsState(),
                                selectedTrackIDState = viewModel
                                    .selectedTrack
                                    .map { it.getOrNull()?.id ?: -1 }
                                    .collectAsState(-1),
                                onAudioTrackSelected = { track ->
                                    viewModel.onAudioTrackSelected(track)
                                    mediaPlayerViewModel.onAudioTrackSelected(track)
                                    mediaPlayerViewModel.onPlayClicked(requireContext())
                                },
                                onAudioTrackShown = viewModel::loadThumbnailsForTrack
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun MainContent(
        scope: ColumnScope,
        tracks: State<List<AudioTrack>>,
        selectedTrackIDState: State<Long>,
        onAudioTrackSelected: (AudioTrack) -> Unit,
        onAudioTrackShown: (AudioTrack) -> Unit
    ) {
        scope.run {
            AudioSelectionView(
                tracks = tracks,
                selectedTrackIDState = selectedTrackIDState,
                onAudioTrackSelected = onAudioTrackSelected,
                onAudioTrackShown = onAudioTrackShown
            )
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
                .background(MaterialTheme.colorScheme.secondary)
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
    private fun MainAppBar(
        scrollBehavior: TopAppBarScrollBehavior,
        onBackPressed: () -> Unit
    ) {
        PROJECTMusicPlayerTheme {
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
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("MutableCollectionMutableState")
    @Preview(showBackground = true)
    @Composable
    fun MainContent_Preview() {
        PROJECTMusicPlayerTheme {
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            Scaffold(
                topBar = {
                    MainAppBar(
                        scrollBehavior = scrollBehavior,
                        onBackPressed = {}
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) { innerPadding ->
                Column (modifier = Modifier.padding(innerPadding)) {
                    val listItem: (Long) -> AudioTrack = { id ->
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
                        tracks = remember {
                            mutableStateOf(
                                mutableListOf<AudioTrack>().apply {
                                    for (i in 0..20) {
                                        add(listItem(i.toLong()))
                                    }
                                }
                            )
                        },
                        selectedTrackIDState = remember { mutableLongStateOf(-1) },
                        onAudioTrackSelected = {},
                        onAudioTrackShown = {}
                    )
                }
            }
        }
    }
}
