package com.seebaldtart.projectmusicplayer.repositories

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.seebaldtart.projectmusicplayer.models.AudioTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.Optional

interface AudioTrackRepository {
    fun stream(): StateFlow<List<AudioTrack>>
    fun getAllTracksByArtist(artistName: String): List<AudioTrack>
    fun getAllTracksByAlbumName(albumName: String): List<AudioTrack>
    fun getAllTracksByTitleName(titleName: String): List<AudioTrack>
    fun getThumbnailForUri(context: Context, uri: Uri): Flow<Optional<Bitmap>>
    fun refresh()
}