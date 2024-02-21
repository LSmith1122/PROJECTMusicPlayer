package com.seebaldtart.projectmusicplayer.models

import com.seebaldtart.projectmusicplayer.models.enums.GroupSelectionState

data class AudioGroupData(
    val groupSelectionState: GroupSelectionState,
    val groupDetails: List<AudioGroupDetails>, // Map<ID, NAME>
    val tracks: List<AudioTrack>
)