@file:Suppress("FunctionName")

package com.seebaldtart.projectmusicplayer.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.seebaldtart.projectmusicplayer.ui.theme.PROJECTMusicPlayerTheme
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@Composable
fun AudioGroupListItem(
    id: String,
    name: String,
    description: String?,
    height: Float,
    imageState: State<Optional<Bitmap>>,
    onAudioGroupSelected: () -> Unit,
    onViewShown: () -> Unit
) {
    PROJECTMusicPlayerTheme {
        Button(
            shape = RoundedCornerShape(4.dp),
            onClick = {
                onAudioGroupSelected.invoke()
            },
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .aspectRatio(1F)
                .fillMaxWidth()
        ) {
            LaunchedEffect(Unit) {
                onViewShown.invoke()
            }

            val image by imageState
            val bitmapPainter = image.getOrNull()?.asImageBitmap()?.let { BitmapPainter(it) }
            Box(modifier = Modifier.fillMaxSize()) {
                bitmapPainter?.let {
                    Image(
                        painter = it,
                        "",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize())
                }

                val textBackground = if (isSystemInDarkTheme()) {
                    Color(0x70FFFFFF) // White w/ Alpha 45%
                } else {
                    Color(0x70000000) // Black w/ Alpha 45%
                }
                Column (modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
                    .align(Alignment.BottomCenter)
                    .background(textBackground)
                ) {
                    Text(
                        text = name,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                    description?.let {
                        Text(
                            text = it,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = false)
fun AudioGroupListItem_Preview() {
    PROJECTMusicPlayerTheme {
        val width = 200
        val height = 200
        val bitmap = createBitmap(width, height, Bitmap.Config.ALPHA_8)

        for (y in 1..< height) {
            for (x in 1 ..< width) {
                val color = Color(
                    red = 0.3F,
                    green = 0.67F,
                    blue = 0.8F
                )
                bitmap[x, y] = color.toArgb()
            }
        }
        Column (modifier = Modifier
            .width(width.dp)
            .height(height.dp)
        ) {
            AudioGroupListItem(
                id = "",
                name = "Example Name",
                description = null,
                height = width.toFloat(),
                imageState = remember { mutableStateOf(Optional.ofNullable(bitmap)) },
                onAudioGroupSelected = {},
                onViewShown = {}
            )
        }
    }
}