@file:Suppress(
    "FunctionName",
    "UnusedMaterial3ScaffoldPaddingParameter"
)

package com.seebaldtart.projectmusicplayer.ui.components.playlistselection

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.seebaldtart.projectmusicplayer.R
import com.seebaldtart.projectmusicplayer.models.enums.GroupSelectionState
import com.seebaldtart.projectmusicplayer.ui.theme.PROJECTMusicPlayerTheme
import com.seebaldtart.projectmusicplayer.viewmodels.AudioPlayListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlaylistSelectionAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    audioPlayListViewModel: AudioPlayListViewModel
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
                            .map { GroupSelectionTab(it) { audioPlayListViewModel.onGroupSelected(it) } },
                        key = { it.groupSelectionState.resID }
                    ) {
                        val selectedID by audioPlayListViewModel.groupSelectionState.collectAsState()
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
