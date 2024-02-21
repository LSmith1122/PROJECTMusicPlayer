@file:Suppress("FunctionName")

package com.seebaldtart.projectmusicplayer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.seebaldtart.projectmusicplayer.R
import com.seebaldtart.projectmusicplayer.ui.theme.PROJECTMusicPlayerTheme
import com.seebaldtart.projectmusicplayer.utils.MediaPlayerUtils

@Composable
fun MediaPlayerController(
    selectedTrackDuration: State<Float>,
    playTimeProgress: State<Float>,
    isAudioPlayingState: State<Boolean>,
    onPlayClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onPreviousClicked: () -> Unit,
    onNextClicked: () -> Unit,
    onSeekPositionChanged: (Float) -> Unit
) {
    val spacerWeight = 0.5F

    PROJECTMusicPlayerTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary)
                .padding(bottom = 8.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.weight(spacerWeight, fill = true))

                    Column(
                        modifier = Modifier.weight(weight = 6F, fill = true)
                    ) {
                        val totalDuration by selectedTrackDuration
                        val playTimeProgressState by playTimeProgress
                        var seekbarUpdate by remember { mutableFloatStateOf(0F) }
                        Slider(
                            value = playTimeProgressState,
                            valueRange = 0F..totalDuration,
                            onValueChangeFinished = {
                                onSeekPositionChanged.invoke(seekbarUpdate)
                            },
                            onValueChange = {
                                seekbarUpdate = it
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = MediaPlayerUtils.getMediaTime(playTimeProgressState.toInt()),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .offset(y = (-8).dp)
                            )

                            Text(
                                text = MediaPlayerUtils.getMediaTime(totalDuration.toInt()),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .offset(y = (-8).dp)
                            )
                        }
                    }

                    Spacer(Modifier.weight(spacerWeight, fill = true))
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.weight(1F, fill = true))

                    val imageResIDPrevious by remember { mutableIntStateOf(R.drawable.baseline_skip_previous_white_24) }
                    PlaybackControlButton(
                        this,
                        imageResIDPrevious,
                        stringResource(R.string.content_description_skip_previous),
                        onPreviousClicked
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
                        this,
                        imageResIDPlayPause,
                        stringResource(R.string.content_description_play_pause),
                    ) {
                        if (isAudioPlaying) {
                            onPauseClicked.invoke()
                        } else {
                            onPlayClicked.invoke()
                        }
                    }

                    val imageResIDNext by remember { mutableIntStateOf(R.drawable.baseline_skip_next_white_24) }
                    PlaybackControlButton(
                        this,
                        imageResIDNext,
                        stringResource(R.string.content_description_skip_next),
                        onNextClicked
                    )

                    Spacer(Modifier.weight(1F, fill = true))
                }
            }
        }
    }
}

@Composable
private fun PlaybackControlButton(
    scope: RowScope,
    imageResID: Int,
    contentDescription: String? = null,
    onClick: () -> Unit
) {
    scope.run {
        Button(
            colors = ButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = MaterialTheme.colorScheme.primary
            ),
            onClick = onClick,
            shape = AbsoluteCutCornerShape(corner = CornerSize(0.dp)),
            modifier = Modifier
                .weight(weight = 2F, fill = true)
                .height(60.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = true, color = Color(0x000000BF)),
                    onClick = onClick
                )
        ) {
            Image(
                painter = painterResource(imageResID),
                contentDescription = contentDescription
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun MediaPlayerController_Preview() {
    MediaPlayerController(
        selectedTrackDuration = remember { mutableFloatStateOf(100F) },
        playTimeProgress = remember { mutableFloatStateOf(0F) },
        isAudioPlayingState = remember { mutableStateOf(false) },
        onPlayClicked = {},
        onPauseClicked = {},
        onPreviousClicked = {},
        onNextClicked = {},
        onSeekPositionChanged = {}
    )
}