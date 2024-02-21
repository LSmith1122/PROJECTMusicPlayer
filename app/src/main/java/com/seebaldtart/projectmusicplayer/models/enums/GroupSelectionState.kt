package com.seebaldtart.projectmusicplayer.models.enums

import android.os.Build
import com.seebaldtart.projectmusicplayer.R

enum class GroupSelectionState(val resID: Int, val isEligible: Boolean) {
    ALL_TRACKS(R.string.track, true),
    ALBUMS(R.string.album, true),
    ARTISTS(R.string.artist, true),
    YEARS(R.string.year, true),
    GENRES(R.string.genre, Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q),
}