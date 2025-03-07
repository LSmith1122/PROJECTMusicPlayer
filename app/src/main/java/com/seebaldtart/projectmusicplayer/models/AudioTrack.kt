package com.seebaldtart.projectmusicplayer.models

import android.graphics.Bitmap
import android.net.Uri
import androidx.palette.graphics.Palette
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

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

    fun getPaletteForBitmap(
        coroutineScope: CoroutineScope,
        numberOfColors: Int
    ): StateFlow<Optional<Palette>> =
        getThumbnailBitmap()
            .map {
                it.getOrNull()?.let { bitmap ->
                    val palette = Palette
                        .from(it.get())
                        .maximumColorCount(numberOfColors)
                        .generate()
                    Optional.ofNullable(palette)
                } ?: Optional.empty<Palette>()
            }.stateIn(
                scope = coroutineScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = Optional.empty<Palette>()
            )
}
