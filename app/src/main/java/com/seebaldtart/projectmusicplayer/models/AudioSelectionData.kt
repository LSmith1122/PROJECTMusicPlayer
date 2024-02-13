package com.seebaldtart.projectmusicplayer.models

import com.seebaldtart.projectmusicplayer.models.AudioTrack

data class AudioSelectionData(
    val artistGroupItemNames: Map<Long, String>?, // Map<ID, NAME>
    val albumGroupItemNames: Map<Long, String>?, // Map<ID, NAME>
    val tracks: List<AudioTrack>?
)
