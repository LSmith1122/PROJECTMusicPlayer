@file:Suppress("FunctionName")

package com.seebaldtart.projectmusicplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seebaldtart.projectmusicplayer.ui.screens.AudioDetailsScreen
import com.seebaldtart.projectmusicplayer.ui.screens.AudioPlaylistScreen
import com.seebaldtart.projectmusicplayer.ui.screens.AudioPlaylistSelectionScreen
import com.seebaldtart.projectmusicplayer.ui.theme.PROJECTMusicPlayerTheme
import com.seebaldtart.projectmusicplayer.viewmodels.AudioPlayListViewModel
import com.seebaldtart.projectmusicplayer.viewmodels.MediaPlayerStateViewModel

fun MusicPlayerNavGraph(
    playlistSelectionViewModel: AudioPlayListViewModel,
    mediaPlayerStateViewModel: MediaPlayerStateViewModel,
    mediaControlsContent: @Composable ((Boolean, Boolean) -> Unit)
): NavGraphBuilder.() -> Unit = {
    composable<Navigation.PlaylistSelectionNavigation> {
        PROJECTMusicPlayerTheme {
            AudioPlaylistSelectionScreen(
                audioPlaylistViewModel = playlistSelectionViewModel,
                mediaPlayerViewModel = mediaPlayerStateViewModel,
                mediaControlsContent = mediaControlsContent
            )
        }
    }
    composable<Navigation.PlaylistNavigation> {
        PROJECTMusicPlayerTheme {
            AudioPlaylistScreen(
                audioPlaylistViewModel = playlistSelectionViewModel,
                mediaPlayerViewModel = mediaPlayerStateViewModel,
                mediaControlsContent = mediaControlsContent
            )
        }
    }
    composable<Navigation.AudioDetailsNavigation> {
        PROJECTMusicPlayerTheme {
            AudioDetailsScreen(
                audioPlaylistViewModel = playlistSelectionViewModel,
                mediaPlayerViewModel = mediaPlayerStateViewModel,
                mediaControlsContent = mediaControlsContent
            )
        }
    }
}
