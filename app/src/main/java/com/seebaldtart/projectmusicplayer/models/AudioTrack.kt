package com.seebaldtart.projectmusicplayer.models

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Optional

class AudioTrack(val id: Long,
                 val trackNumber: Int,
                 val title: String,
                 val artistID: Long,
                 val artistName: String,
                 val albumName: String,
                 val duration: Int,
                 val thumbnailUri: Uri?,
                 /** See [MediaStore.Audio.Media.DATA] */
                 val path: String) {
    private val thumbnailBitmap = MutableStateFlow<Optional<Bitmap>>(Optional.empty())

    fun getThumbnailBitmap(): StateFlow<Optional<Bitmap>> = thumbnailBitmap.asStateFlow()

    suspend fun updateThumbnail(thumbnail: Bitmap) {
        thumbnailBitmap.emit(Optional.of(thumbnail))
    }

}
