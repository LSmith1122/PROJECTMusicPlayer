@file:Suppress("FunctionName")

package com.seebaldtart.projectmusicplayer.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import com.seebaldtart.projectmusicplayer.R
import com.seebaldtart.projectmusicplayer.models.AudioTrack
import com.seebaldtart.projectmusicplayer.ui.theme.SMALL_COMPONENT_BLUR_ALPHA
import com.seebaldtart.projectmusicplayer.ui.theme.SMALL_COMPONENT_BLUR_RADIUS
import com.seebaldtart.projectmusicplayer.ui.theme.TEXT_SIZE_SMALL
import com.seebaldtart.projectmusicplayer.utils.MediaPlayerUtils
import kotlinx.coroutines.launch
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@Composable
/** Media player playback controller.
 *
 * If [isMinimized] == false, all of the optional playback callbacks become mandatory,
 * otherwise the playback buttons are not shown.
 * */
fun MediaPlayerController(
    isMinimized: Boolean = false,
    showNowPlayingTrack: Boolean = false,
    track: AudioTrack?,
    trackDurationState: State<Float>,
    playTimeProgressState: State<Float>,
    isAudioPlayingState: State<Boolean>,
    onPlayClicked: (() -> Unit)? = null,
    onPauseClicked: (() -> Unit)? = null,
    onPreviousClicked: (() -> Unit)? = null,
    onNextClicked: (() -> Unit)? = null,
    onViewShown: () -> Unit,
    onNowPlayingTrackClicked: (() -> Unit)? = null,
    onSeekPositionChanged: (Float) -> Unit
) {
    val spacerWeight = 0.5F

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(bottom = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            var paletteOptional by remember { mutableStateOf(Optional.empty<Palette>()) }

            track?.let {
                val imageOptional by it.getThumbnailBitmap().collectAsState()
                it.getPaletteForBitmap(
                    coroutineScope = rememberCoroutineScope(),
                    numberOfColors = 24
                ).collectAsState()
                .value
                .run { paletteOptional = this }

                if (showNowPlayingTrack) {
                    NowPlayingBanner(
                        title = it.title,
                        artist = it.artistName,
                        album = it.albumName,
                        imageOptional = imageOptional,
                        paletteOptional = paletteOptional,
                        isAudioPlayingState = isAudioPlayingState,
                        onClick = { onNowPlayingTrackClicked?.invoke() },
                        onPlayClicked = onPlayClicked,
                        onPauseClicked = onPauseClicked,
                        onViewShown = onViewShown
                    )
                }
            }

            SeekBarControls(
                spacerWeight = spacerWeight,
                selectedTrackDurationState = trackDurationState,
                playTimeProgressState = playTimeProgressState,
                onSeekPositionChanged = onSeekPositionChanged,
                paletteOptional = paletteOptional
            )

            if (!isMinimized
                && onPreviousClicked != null
                && onPauseClicked != null
                && onPlayClicked != null
                && onNextClicked != null) {
                MediaPlayerControls(
                    onPreviousClicked = onPreviousClicked,
                    isAudioPlayingState = isAudioPlayingState,
                    palette = paletteOptional,
                    onPauseClicked = onPauseClicked,
                    onPlayClicked = onPlayClicked,
                    onNextClicked = onNextClicked
                )
            }
        }
    }
}

@Composable
fun NowPlayingBanner (
    title: String,
    artist: String,
    album: String,
    imageOptional: Optional<Bitmap>,
    paletteOptional: Optional<Palette>,
    isAudioPlayingState: State<Boolean>,
    onClick: () -> Unit,
    onPlayClicked: (() -> Unit)?,
    onPauseClicked: (() -> Unit)?,
    onViewShown: () -> Unit,
) {
    val backgroundColor = paletteOptional.getOrNull()?.let { palette ->
        palette.vibrantSwatch?.rgb?.let { Color(it) }
    } ?: Color.Transparent

    Row(
        modifier = Modifier
            .height(dimensionResource(R.dimen.list_item_height))
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick
            ).padding(bottom = 1.dp)
    ) {
        LaunchedEffect(Unit) {
            onViewShown.invoke()
        }

        val painterResource = painterResource(R.drawable.default_icon_song_album1)
        val bitmapPainter = imageOptional.getOrNull()?.asImageBitmap()?.let { BitmapPainter(it) }
        val painter by remember(imageOptional) {
            mutableStateOf(if (imageOptional.isPresent && bitmapPainter != null) {
                bitmapPainter
            } else {
                painterResource
            })
        }
        Image(
            painter = painter,
            contentDescription = "Album Art",
            modifier = Modifier
                .aspectRatio(ratio = 1F, matchHeightConstraintsFirst = true)
                .fillMaxHeight()
                .padding(all = 4.dp)
                .shadow(elevation = 4.dp, ambientColor = Color(0xFFDEDEDE))
        )

        Row(
            modifier = Modifier
                .fillMaxHeight()
                .weight(4F, fill = true)
                .padding(vertical = 4.dp)
                .padding(start = 4.dp, end = 16.dp)
        ) {
            Column(
                modifier = Modifier.weight(weight = 1F, fill = true)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F, true)
                        .offset(y = 2.dp)
                ) {
                    // Title Shadow
                    Text(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .blur(
                                radius = SMALL_COMPONENT_BLUR_RADIUS,
                                edgeTreatment = BlurredEdgeTreatment.Unbounded
                            )
                            .alpha(SMALL_COMPONENT_BLUR_ALPHA)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                                repeatDelayMillis = 3 * 1000,
                                initialDelayMillis = 5 * 1000,
                                spacing = MarqueeSpacing(48.dp),
                                velocity = 24.dp
                            ),
                        text = title,
                        color = Color.Black,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = title,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                                repeatDelayMillis = 3 * 1000,
                                initialDelayMillis = 5 * 1000,
                                spacing = MarqueeSpacing(48.dp),
                                velocity = 24.dp
                            )
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F, true)
                        .offset(y = 4.dp)
                ) {
                    val artistAndAlbumText = "$artist - $album"
                    // Artist / Album Shadow
                    Text(
                        modifier = Modifier
                            .blur(
                                radius = SMALL_COMPONENT_BLUR_RADIUS,
                                edgeTreatment = BlurredEdgeTreatment.Unbounded
                            )
                            .alpha(SMALL_COMPONENT_BLUR_ALPHA)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                                repeatDelayMillis = 3 * 1000,
                                initialDelayMillis = 5 * 1000,
                                spacing = MarqueeSpacing(48.dp),
                                velocity = 24.dp
                            ),
                        text = artistAndAlbumText,
                        color = Color.Black,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = artistAndAlbumText,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.basicMarquee(
                            iterations = Int.MAX_VALUE,
                            animationMode = MarqueeAnimationMode.Immediately,
                            repeatDelayMillis = 3 * 1000,
                            initialDelayMillis = 5 * 1000,
                            spacing = MarqueeSpacing(48.dp),
                            velocity = 24.dp
                        )
                    )
                }
            }
            val isAudioPlaying by isAudioPlayingState
            val imageResIDPlayPause by remember(isAudioPlaying) {
                mutableIntStateOf(
                    if (isAudioPlaying) {
                        R.drawable.baseline_pause_white_24
                    } else {
                        R.drawable.baseline_play_arrow_white_24
                    }
                )
            }
            PlaybackControlButton(
                imageResID = imageResIDPlayPause,
                contentDescription = stringResource(R.string.content_description_play_pause)
            ) {
                if (isAudioPlaying) {
                    onPauseClicked?.invoke()
                } else {
                    onPlayClicked?.invoke()
                }
            }
        }
    }
}

@Composable
private fun SeekBarControls(
    spacerWeight: Float,
    selectedTrackDurationState: State<Float>,
    playTimeProgressState: State<Float>,
    onSeekPositionChanged: (Float) -> Unit,
    paletteOptional: Optional<Palette>
) {
    val scope = rememberCoroutineScope()
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.weight(spacerWeight, fill = true))

        Column(
            modifier = Modifier.weight(weight = 6F, fill = true)
        ) {
            val totalDuration by selectedTrackDurationState
            val playTimeProgress by playTimeProgressState
            var seekbarUpdate by remember { mutableFloatStateOf(0F) }
            var isManuallySliding by remember { mutableStateOf(false) }
            val actualSliderTime = if (isManuallySliding) seekbarUpdate else playTimeProgress

            val defaultColors = SliderDefaults.colors()
            val colors = paletteOptional.getOrNull()?.let { palette ->
                SliderDefaults.colors(
                    thumbColor = palette.vibrantSwatch?.rgb?.let { Color(it) }
                        ?: defaultColors.thumbColor,
                    activeTrackColor = palette.vibrantSwatch?.rgb?.let { Color(it) }
                        ?: defaultColors.activeTrackColor,
                    activeTickColor = palette.vibrantSwatch?.rgb?.let { Color(it) }
                        ?: defaultColors.activeTickColor
                )
            } ?: defaultColors

            Slider(
                modifier = Modifier.fillMaxWidth(),
                value = actualSliderTime,
                valueRange = 0F..totalDuration,
                onValueChangeFinished = {
                    scope.launch {
                        isManuallySliding = false
                        onSeekPositionChanged.invoke(seekbarUpdate)
                    }
                },
                onValueChange = {
                    scope.launch {
                        seekbarUpdate = it
                        isManuallySliding = true
                    }
                },
                colors = colors
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                TimeText(Alignment.CenterStart, actualSliderTime.toInt())
                TimeText(Alignment.CenterEnd, totalDuration.toInt())
            }
        }

        Spacer(Modifier.weight(spacerWeight, fill = true))
    }
}

@Composable
private fun BoxScope.TimeText(
    alignment: Alignment,
    time: Int
) {
    Box(
        modifier = Modifier
            .align(alignment)
            .offset(y = (-8).dp)
    ) {
        Text(
            modifier = Modifier
                .blur(
                    radius = SMALL_COMPONENT_BLUR_RADIUS,
                    edgeTreatment = BlurredEdgeTreatment.Unbounded
                )
                .alpha(SMALL_COMPONENT_BLUR_ALPHA),
            text = MediaPlayerUtils.getMediaTime(time),
            color = Color.Black,
            fontSize = TEXT_SIZE_SMALL
        )
        Text(
            text = MediaPlayerUtils.getMediaTime(time),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = TEXT_SIZE_SMALL
        )
    }
}

@Composable
private fun MediaPlayerControls(
    onPreviousClicked: () -> Unit,
    isAudioPlayingState: State<Boolean>,
    palette: Optional<Palette>,
    onPauseClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    onNextClicked: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.weight(1F, fill = true))

        PlaybackControlButton(
            modifier = Modifier.weight(weight = 2F, fill = true),
            imageResID = R.drawable.baseline_skip_previous_white_24,
            contentDescription = stringResource(R.string.content_description_skip_previous),
            onClick = onPreviousClicked
        )

        val isAudioPlaying by isAudioPlayingState
        val imageResIDPlayPause by remember(isAudioPlaying) {
            mutableIntStateOf(
                if (isAudioPlaying) {
                    R.drawable.baseline_pause_white_24
                } else {
                    R.drawable.baseline_play_arrow_white_24
                }
            )
        }
        PlaybackControlButton(
            modifier = Modifier.weight(weight = 2F, fill = true),
            imageResID = imageResIDPlayPause,
            contentDescription = stringResource(R.string.content_description_play_pause),
            onClick = {
                if (isAudioPlaying) {
                    onPauseClicked.invoke()
                } else {
                    onPlayClicked.invoke()
                }
            }
        )

        PlaybackControlButton(
            modifier = Modifier.weight(weight = 2F, fill = true),
            imageResID = R.drawable.baseline_skip_next_white_24,
            contentDescription = stringResource(R.string.content_description_skip_next),
            onClick = onNextClicked
        )

        Spacer(Modifier.weight(1F, fill = true))
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0)
private fun MediaPlayerController_Preview() {
    MediaPlayerController(
        isMinimized = false,
        showNowPlayingTrack = true,
        track = getMockAudioTrack(),
        trackDurationState = remember { mutableFloatStateOf(100F) },
        playTimeProgressState = remember { mutableFloatStateOf(0F) },
        isAudioPlayingState = remember { mutableStateOf(false) },
        onPlayClicked = {},
        onPauseClicked = {},
        onPreviousClicked = {},
        onNextClicked = {},
        onSeekPositionChanged = {},
        onViewShown = {},
        onNowPlayingTrackClicked = {}
    )
}

private fun getMockAudioTrack(): AudioTrack =
    AudioTrack(
        id = 1,
        trackNumber = 1,
        title = "A Great Song Title",
        artistID = 1L,
        artistName = "Artist Name",
        albumName = "Album Name",
        year = 2025,
        genreID = 123,
        genre = "Hip-Hop",
        duration = 100000,
        thumbnailUri = null,
        path = "/"
    )