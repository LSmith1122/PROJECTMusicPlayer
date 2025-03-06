package com.seebaldtart.projectmusicplayer.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.seebaldtart.projectmusicplayer.R
import com.seebaldtart.projectmusicplayer.models.AudioTrack
import com.seebaldtart.projectmusicplayer.models.enums.GroupItemSelectionState
import com.seebaldtart.projectmusicplayer.models.enums.LoopState
import com.seebaldtart.projectmusicplayer.repositories.AudioTrackRepository
import com.seebaldtart.projectmusicplayer.ui.components.PlaybackControlButton
import com.seebaldtart.projectmusicplayer.ui.theme.SMALL_COMPONENT_BLUR_ALPHA
import com.seebaldtart.projectmusicplayer.ui.theme.SMALL_COMPONENT_BLUR_RADIUS
import com.seebaldtart.projectmusicplayer.utils.DispatcherProvider
import com.seebaldtart.projectmusicplayer.viewmodels.AudioPlayListViewModel
import com.seebaldtart.projectmusicplayer.viewmodels.MediaPlayerStateViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioDetailsScreen(
    audioPlaylistViewModel: AudioPlayListViewModel,
    mediaPlayerViewModel: MediaPlayerStateViewModel,
    mediaControlsContent: @Composable ((Boolean, Boolean) -> Unit)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val onBackPressed: (GroupItemSelectionState) -> Unit = {
        // We do not want to clear the Group Item Selection (via viewModel.clearGroupItemSelection())
        // because we want the Media Player to continue playing the previously selected playlist
        audioPlaylistViewModel.setGroupItemSelectionState(it)
    }
    // The GroupItemSelectionState FlowState is a SharedFlow with a replay of 2.
    // The first cached state is the previous state. If we want to navigate backwards, we
    // want to set the previous state as the new state.
    val previousGroupItemState = audioPlaylistViewModel
        .groupItemSelectionState
        .replayCache
        .let {
            if (it.size >= 2) {
                it.first()
            } else {
                GroupItemSelectionState.AUDIO_PLAY_LIST_SELECTION
            }
        }
    BackHandler {
        // There is a bug that causes the back presses to intermittently not trigger this callback
        Log.i("DIAMOND", "BackHandler - pressed")
        onBackPressed.invoke(previousGroupItemState)
    }

    val track by audioPlaylistViewModel
        .selectedTrack
        .map { it.getOrNull() }
        .collectAsState(null)

    var painter by remember(track?.id) {
        mutableStateOf<Painter?>(null)
    }
    if (track != null) {
        val image by track!!.getThumbnailBitmap().collectAsState()
        val bitmapPainter = image.getOrNull()?.asImageBitmap()?.let { BitmapPainter(it) }
        if (image.isPresent && bitmapPainter != null) {
            painter = bitmapPainter
        }
    }

    // Background blurred album image
    if (painter != null) {
        Image(
            painter = painter!!,
            contentDescription = "Album Art",
            contentScale = ContentScale.FillHeight,
            modifier = Modifier
                .fillMaxHeight()
                .blur(radius = 60.dp, edgeTreatment = BlurredEdgeTreatment.Rectangle)
        )
    }

    Scaffold(
        topBar = {
            AudioDetailsAppBar(
                scrollBehavior = scrollBehavior,
                showBackground = painter == null,
                onBackPressed = { onBackPressed(previousGroupItemState) }
            )
        },
        bottomBar = { mediaControlsContent.invoke(false, false) },
        containerColor = if (painter != null) Color.Transparent else MaterialTheme.colorScheme.secondary,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (track == null) {
                // TODO: Show an error dialog, toast or etc.
                return@Column
            }

            ArtistInfo(artist = track?.artistName ?: "")

            AlbumCoverArtDisplay(painter)

            TrackInfo(title = track?.title ?: "", album = track?.albumName ?: "")

            PlaybackControlButtons(mediaPlayerViewModel)
        }
    }
}

@Composable
private fun PlaybackControlButtons(mediaPlayerViewModel: MediaPlayerStateViewModel) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        val loopState by mediaPlayerViewModel.loopState.collectAsState()
        val repeatIcon by remember(loopState) {
            mutableIntStateOf(
                when (loopState) {
                    LoopState.NONE -> R.drawable.baseline_repeat_none_white_24
                    LoopState.ONE -> R.drawable.baseline_repeat_one_white_24
                    LoopState.ALL -> R.drawable.baseline_repeat_white_24
                }
            )
        }
        PlaybackControlButton(
            modifier = Modifier.aspectRatio(1F),
            imageResID = repeatIcon,
            contentDescription = "Repeat None",
            onClick = {
                mediaPlayerViewModel.onLoopClicked()
            }
        )
    }
}

@Composable
private fun ArtistInfo(artist: String) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Artist Shadow
                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .blur(
                            radius = SMALL_COMPONENT_BLUR_RADIUS,
                            edgeTreatment = BlurredEdgeTreatment.Unbounded
                        )
                        .alpha(SMALL_COMPONENT_BLUR_ALPHA),
                    text = artist,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = artist,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun TrackInfo(title: String, album: String) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Title Shadow
                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .blur(
                            radius = SMALL_COMPONENT_BLUR_RADIUS,
                            edgeTreatment = BlurredEdgeTreatment.Unbounded
                        )
                        .alpha(SMALL_COMPONENT_BLUR_ALPHA),
                    text = title,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = title,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                // Album Shadow
                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .blur(
                            radius = SMALL_COMPONENT_BLUR_RADIUS,
                            edgeTreatment = BlurredEdgeTreatment.Unbounded
                        )
                        .alpha(SMALL_COMPONENT_BLUR_ALPHA),
                    text = album,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = album,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.AlbumCoverArtDisplay(painter: Painter?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val defaultImage = painterResource(R.drawable.default_icon_song_album1)
        Image(
            painter = painter ?: defaultImage,
            contentDescription = "Album Art",
            modifier = Modifier
                .aspectRatio(ratio = 1F, matchHeightConstraintsFirst = false)
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
                .shadow(elevation = 16.dp, ambientColor = Color(0xFFDEDEDE))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudioDetailsAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    showBackground: Boolean,
    onBackPressed: () -> Unit,
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
            containerColor = if (showBackground) MaterialTheme.colorScheme.secondary else Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.secondary,
            titleContentColor = Color.White
        ),
        title = { stringResource(R.string.app_name) }
    )
}

@Preview
@Composable
private fun Preview_AudioDetailsScreen() {
    val dispatchersProvider = DispatcherProvider(
        Dispatchers.Main,
        Dispatchers.Unconfined,
        Dispatchers.IO,
        Dispatchers.Default
    )
    AudioDetailsScreen(
        audioPlaylistViewModel = AudioPlayListViewModel(
            dispatchersProvider = dispatchersProvider,
            repository = object : AudioTrackRepository {
                override fun initialize() {}
                override fun stream(): StateFlow<List<AudioTrack>> = MutableStateFlow(emptyList())
                override fun getAllTracksByArtist(artistName: String): List<AudioTrack> = emptyList()
                override fun getAllTracksByAlbumName(albumName: String): List<AudioTrack> = emptyList()
                override fun getAllTracksByTitleName(titleName: String): List<AudioTrack> = emptyList()
                override fun getThumbnailForUri(context: Context, uri: Uri): Flow<Optional<Bitmap>> = flow { }
                override fun refresh() {}
            }
        ),
        mediaControlsContent = { _, _ -> },
        mediaPlayerViewModel = MediaPlayerStateViewModel(null, DispatcherProvider(
            Dispatchers.Main,
            Dispatchers.Unconfined,
            Dispatchers.IO,
            Dispatchers.Default
        )),
    )
}
