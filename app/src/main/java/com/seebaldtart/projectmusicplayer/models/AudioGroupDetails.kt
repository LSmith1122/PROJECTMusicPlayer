package com.seebaldtart.projectmusicplayer.models

import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Optional

class AudioGroupDetails(
    val id: String,
    val name: String,
    val description: String? = null,
    val thumbnailUri: Uri? = null
) {
    private val thumbnailBitmap = MutableStateFlow<Optional<Bitmap>>(Optional.empty())

    fun getThumbnailBitmap(): StateFlow<Optional<Bitmap>> = thumbnailBitmap.asStateFlow()

    fun updateThumbnail(thumbnail: Bitmap) {
        thumbnailBitmap.update { Optional.of(thumbnail) }
    }
}
