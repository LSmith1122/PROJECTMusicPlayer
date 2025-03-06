package com.seebaldtart.projectmusicplayer.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import kotlinx.serialization.Serializable

interface MediaPlayerNavigation

object Navigation {
    @Serializable
    @OptIn(ExperimentalMaterial3Api::class)
    object PlaylistSelectionNavigation: MediaPlayerNavigation

    @Serializable
    @OptIn(ExperimentalMaterial3Api::class)
    object PlaylistNavigation: MediaPlayerNavigation

    @Serializable
    @OptIn(ExperimentalMaterial3Api::class)
    object AudioDetailsNavigation: MediaPlayerNavigation
}
