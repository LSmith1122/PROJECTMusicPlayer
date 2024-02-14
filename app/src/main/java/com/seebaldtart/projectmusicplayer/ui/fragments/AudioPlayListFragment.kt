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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.seebaldtart.projectmusicplayer.R
import com.seebaldtart.projectmusicplayer.models.AudioTrack
import com.seebaldtart.projectmusicplayer.models.data.enums.PlaybackError
import com.seebaldtart.projectmusicplayer.ui.components.AudioTrackListItem
import com.seebaldtart.projectmusicplayer.ui.theme.PROJECTMusicPlayerTheme
import com.seebaldtart.projectmusicplayer.viewmodels.AudioPlayListViewModel
import com.seebaldtart.projectmusicplayer.viewmodels.MediaPlayerStateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlin.jvm.optionals.getOrNull

@AndroidEntryPoint
class AudioPlayListFragment : Fragment() {

    private val viewModel by activityViewModels<AudioPlayListViewModel>()
    private val mediaPlayerViewModel by activityViewModels<MediaPlayerStateViewModel>()

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
                            this@AudioPlayListFragment::class.simpleName,
                            message,
                            RuntimeException(message)
                        )
                    }
                })

                LaunchedEffect(true) {
                    viewModel.stream()
                        .onEach {
                            mediaPlayerViewModel.setCurrentPlaylist(it)
                        }.collect()
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { _ ->
                    Column {
                        MainContent(
                            this,
                            tracks = viewModel.stream().collectAsState(),
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
                .padding(top = 8.dp)
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

@SuppressLint("MutableCollectionMutableState")
@Preview(showBackground = true)
@Composable
fun MainContent_Preview() {
    PROJECTMusicPlayerTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { _ ->
            Column {
                val listItem: (Long) -> AudioTrack = { id ->
                    AudioTrack(id, 0, "Title", -1, "John Doe", "Example Album", 300, null, "")
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
