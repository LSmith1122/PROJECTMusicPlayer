@file:Suppress(
    "FunctionName",
    "UnusedMaterial3ScaffoldPaddingParameter"
)

package com.seebaldtart.projectmusicplayer.ui.components.playlistselection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.palette.graphics.Palette
import com.seebaldtart.projectmusicplayer.models.AudioGroupData
import com.seebaldtart.projectmusicplayer.models.AudioGroupDetails
import com.seebaldtart.projectmusicplayer.models.enums.GroupSelectionState
import com.seebaldtart.projectmusicplayer.ui.components.AudioGroupListItem
import com.seebaldtart.projectmusicplayer.ui.components.PlaylistIndexScrollBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Optional

@Composable
fun GroupSelectionView(
    groupItemsState: State<AudioGroupData>,
    paletteOptional: Optional<Palette>,
    onAudioGroupItemSelected: (AudioGroupDetails) -> Unit,
    onViewShown: (AudioGroupDetails) -> Unit
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyGridState()
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
        state = listState,
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
    PlaylistIndexScrollBar(
        sources = items.map { it.name },
        paletteOptional = paletteOptional,
        onIndexSelected = { indexCharacter ->
            val indexOfFirst = items.indexOfFirst {
                if (!indexCharacter.isLetter()) {
                    !it.name.first().isLetter()
                } else {
                    it.name.first().uppercase() == indexCharacter.toString()
                }
            }
            scope.launch(Dispatchers.Main) {
                listState.scrollToItem(indexOfFirst)
            }
        }
    )
}
