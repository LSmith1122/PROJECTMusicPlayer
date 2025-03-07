package com.seebaldtart.projectmusicplayer.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import com.seebaldtart.projectmusicplayer.ui.theme.SMALL_COMPONENT_BLUR_ALPHA
import com.seebaldtart.projectmusicplayer.ui.theme.SMALL_COMPONENT_BLUR_RADIUS
import com.seebaldtart.projectmusicplayer.ui.theme.SearchBarRoundedCorner
import com.seebaldtart.projectmusicplayer.utils.pointerSlidingInput
import kotlinx.coroutines.launch
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@Composable
fun PlaylistIndexScrollBar(
    sources: List<String>,
    paletteOptional: Optional<Palette>,
    onIndexSelected: (Char) -> Unit
) {
    val scope = rememberCoroutineScope()
    // Track the currently highlighted item (or null if none)
    var highlightedIndex by remember { mutableStateOf<Int?>(null) }
    val items = sources
        .map { character ->
            character.first()
                .uppercaseChar()
                .let { c -> if (c.isLetter()) { c } else { '#' } }
        }.distinct()
        .sorted()

    highlightedIndex?.let {
        val highlightedCharacter = items[it].toString()
        val backgroundColor = paletteOptional.getOrNull()?.let { palette ->
            palette.vibrantSwatch?.rgb?.let { Color(it) }
        } ?: MaterialTheme.colorScheme.primary

        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.align(Alignment.Center)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(45.dp)
                        .drawBehind {
                            drawCircle(color = backgroundColor, radius = this.size.width)
                        }
                )
                // Title Shadow
                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .blur(
                            radius = SMALL_COMPONENT_BLUR_RADIUS,
                            edgeTreatment = BlurredEdgeTreatment.Unbounded
                        )
                        .alpha(SMALL_COMPONENT_BLUR_ALPHA),
                    text = highlightedCharacter,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 64.sp
                )
                Text(
                    text = highlightedCharacter,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 64.sp
                )
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row (modifier = Modifier.align(Alignment.End)) {
            val listState = rememberLazyListState()

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .align(Alignment.Top)
                    .padding(vertical = 16.dp, horizontal = 4.dp)
                    .border(
                        width = 0.dp,
                        color = Color(0x30FFFFFF),
                        shape = RoundedCornerShape(size = SearchBarRoundedCorner)
                    )
                    .pointerSlidingInput(
                        listState = listState,
                        items = items,
                        onHighlightedIndexUpdated = {
                            scope.launch {
                                highlightedIndex = it
                            }
                        },
                        onIndexSelected = onIndexSelected,
                    ),
                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
                userScrollEnabled = false,
            ) {
                itemsIndexed(
                    items = items,
                    key = { index, item -> item }
                ) { index, item ->
                    IndexItem(
                        character = item,
                        isHighlighted = highlightedIndex == index
                    )
                }
            }
        }
    }
}

@Composable
private fun IndexItem(
    character: Char,
    isHighlighted: Boolean
) {
    val fontSizeHighlighted = MaterialTheme.typography.bodyLarge.fontSize
    val fontSizeNotHighlighted = MaterialTheme.typography.bodyMedium.fontSize
    var fontSize by remember(isHighlighted) {
        mutableStateOf(
            if (isHighlighted) {
                fontSizeHighlighted
            } else {
                fontSizeNotHighlighted
            }
        )
    }
    var fontWeight by remember(isHighlighted) {
        mutableStateOf(
            if (isHighlighted) {
                FontWeight.ExtraBold
            } else {
                FontWeight.Normal
            }
        )
    }

    Box(modifier = Modifier) {
        Text(
            modifier = Modifier
                .blur(
                    radius = SMALL_COMPONENT_BLUR_RADIUS,
                    edgeTreatment = BlurredEdgeTreatment.Unbounded
                )
                .alpha(SMALL_COMPONENT_BLUR_ALPHA),
            text = character.toString(),
            color = Color.Black,
            textAlign = TextAlign.End,
            fontWeight = fontWeight,
            fontSize = fontSize,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = character.toString(),
            color = Color.White,
            textAlign = TextAlign.End,
            fontWeight = fontWeight,
            fontSize = fontSize,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(backgroundColor = 0xFF000000, showBackground = true)
@Composable
private fun Preview_PlaylistIndexScrollBar() {
    PlaylistIndexScrollBar(
        sources = listOf(
            "Ablahblahblah",
            "bblahblahblah",
            "Ablahblahblah",
            "cblahblahblah",
            "7blahblahblah",
            "0blahblahblah",
            "Cblahblahblah",
            "Bblahblahblah",
            "Ablahblahblah",
            "Bblahblahblah",
            "[b-lahblahblah",
        ),
        paletteOptional = Optional.empty<Palette>(),
        onIndexSelected = {}
    )
}
