package com.seebaldtart.projectmusicplayer.repositories

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.seebaldtart.projectmusicplayer.models.AudioTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.Optional

interface AudioTrackRepository {
    /** Calling this function retrieves all available audio data on the device.
     * This should only be called when this repository needs to be initialized.
     * Additional calls to this function will have no effect. */
    fun initialize()
    /** This function returns a stream of all [AudioTrack]s retrieved. */
    fun stream(): StateFlow<List<AudioTrack>>
    /** This helper function will return the most recent list of [AudioTrack]s based on the provided [artistName]. */
    fun getAllTracksByArtist(artistName: String): List<AudioTrack>
    /** This helper function will return the most recent list of [AudioTrack]s based on the provided [albumName]. */
    fun getAllTracksByAlbumName(albumName: String): List<AudioTrack>
    /** This helper function will return the most recent list of [AudioTrack]s based on the provided [titleName]. */
    fun getAllTracksByTitleName(titleName: String): List<AudioTrack>
    /** This function will return the album artwork for the [Uri] in a stream. */
    fun getThumbnailForUri(context: Context, uri: Uri): Flow<Optional<Bitmap>>
    /** Calling this function will refresh the data streamed and cached with new data. */
    fun refresh()
}