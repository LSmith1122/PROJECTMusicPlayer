package com.seebaldtart.projectmusicplayer.models

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Optional

class AudioTrack(val id: Long,
                 val trackNumber: Int,
                 val title: String,
                 val artistID: Long,
                 val artistName: String,
                 val albumName: String,
                 val year: Int,
                 val genreID: Long?,
                 val genre: String?,
                 val duration: Int,
                 val thumbnailUri: Uri?,
                 /** See [MediaStore.Audio.Media.DATA] */
                 val path: String) {
    private val thumbnailBitmap = MutableStateFlow<Optional<Bitmap>>(Optional.empty())

    fun getThumbnailBitmap(): StateFlow<Optional<Bitmap>> = thumbnailBitmap.asStateFlow()

    fun updateThumbnail(thumbnail: Bitmap) {
        thumbnailBitmap.update { Optional.of(thumbnail) }
    }

}
