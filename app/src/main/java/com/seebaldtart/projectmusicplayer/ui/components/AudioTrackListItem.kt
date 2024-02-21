@file:Suppress("FunctionName")

package com.seebaldtart.projectmusicplayer.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.seebaldtart.projectmusicplayer.R
import com.seebaldtart.projectmusicplayer.ui.theme.PROJECTMusicPlayerTheme
import com.seebaldtart.projectmusicplayer.utils.MediaPlayerUtils
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@Composable
fun AudioTrackListItem (
    id: Long,
    title: String,
    artist: String,
    album: String,
    duration: Int,
    imageState: State<Optional<Bitmap>>,
    selectedIDState: State<Long>,
    onAudioTrackSelected: () -> Unit,
    onViewShown: () -> Unit
) {
    PROJECTMusicPlayerTheme {
        val selectedID by selectedIDState
        val primaryColor = MaterialTheme.colorScheme.primary
        val background by remember(selectedID) {
            mutableStateOf(
                if (selectedID == id) {
                    primaryColor
                } else {
                    Color.Transparent
                }
            )
        }

        Row(
            modifier = Modifier
                .height(dimensionResource(R.dimen.list_item_height))
                .fillMaxWidth()
                .background(background)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = LocalIndication.current,
                    onClick = {
                        onAudioTrackSelected.invoke()
                    }
                )
                .padding(bottom = 1.dp)
        ) {
            LaunchedEffect(Unit) {
                onViewShown.invoke()
            }

            val image by imageState
            val painterResource = painterResource(R.drawable.default_icon_song_album1)
            val bitmapPainter = image.getOrNull()?.asImageBitmap()?.let { BitmapPainter(it) }
            val painter by remember(image) {
                mutableStateOf(if (image.isPresent && bitmapPainter != null) {
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
            )

            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(4F, fill = true)
                    .padding(all = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1F, true)
                            .offset(y = 2.dp)
                    ) {
                        Text(
                            text = title,
                            textAlign = TextAlign.Start,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.align(Alignment.BottomStart),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1F, true)
                            .offset(y = 4.dp)
                    ) {
                        Text(
                            text = "$artist - $album",
                            textAlign = TextAlign.Start,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1F, true)
                    .fillMaxHeight()
            ) {
                Text(
                    text = MediaPlayerUtils.getMediaTime(duration),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

        }
    }
}

@Composable
@Preview(showBackground = false)
fun AudioTrackItem_Preview() {
    PROJECTMusicPlayerTheme {
        AudioTrackListItem(
            id = -1,
            title = "Title",
            artist = "Artist",
            album = "Album",
            duration = 300,
            imageState = remember { mutableStateOf(Optional.empty<Bitmap>()) },
            selectedIDState = remember { mutableLongStateOf(-1) },
            onAudioTrackSelected = {},
            onViewShown = {}
        )
    }
}