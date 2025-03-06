package com.seebaldtart.projectmusicplayer.repositories

import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.annotation.RequiresApi
import com.seebaldtart.projectmusicplayer.models.AudioTrack
import com.seebaldtart.projectmusicplayer.services.ThumbnailService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import java.io.FileNotFoundException
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import javax.inject.Inject

private const val ALBUM_ART_URI_PATH = "content://media/external/audio/albumart"
private const val DEFAULT_THUMBNAIL_SIZE = 750

class AudioTrackRepositoryImpl @Inject constructor(
    private val application: Application,
    private val thumbnailService: ThumbnailService
): AudioTrackRepository {

    private val allAudioTracks = MutableStateFlow<List<AudioTrack>>(emptyList())
    private val allAudioThumbnails = ConcurrentHashMap<String, Bitmap?>()
    private var isInitialized = false

    override fun initialize() {
        if (!isInitialized) {
            retrieveAllAudioTracks()
            isInitialized = true
        }
    }

    override fun stream(): StateFlow<List<AudioTrack>> = allAudioTracks

    override fun getAllTracksByArtist(artistName: String): List<AudioTrack> =
        allAudioTracks
            .value
            .filter { it.artistName.lowercase() == artistName.lowercase() }

    override fun getAllTracksByAlbumName(albumName: String): List<AudioTrack> =
        allAudioTracks
            .value
            .filter { it.albumName.lowercase() == albumName.lowercase() }

    override fun getAllTracksByTitleName(titleName: String): List<AudioTrack> =
        allAudioTracks
            .value
            .filter { it.title.lowercase() == titleName.lowercase() }

    override fun getThumbnailForUri(context: Context, uri: Uri): Flow<Optional<Bitmap>> {
        return flow {
            allAudioThumbnails.computeIfAbsent(uri.toString()) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            // Attempts to load the bitmap from local resources
                            loadBitmapFromLocal(context.contentResolver, uri)
                        } catch (e: FileNotFoundException) {
                            // The Uri might be a URL. Attempt making a service call to retrieve...
                            thumbnailService.fetchBitmap(context, uri, Size(DEFAULT_THUMBNAIL_SIZE, DEFAULT_THUMBNAIL_SIZE))
                        }
                    } else {
                        thumbnailService.fetchBitmap(context, uri, Size(DEFAULT_THUMBNAIL_SIZE, DEFAULT_THUMBNAIL_SIZE))
                    }
                } catch (_: ExecutionException) {
                    Log.e(this@AudioTrackRepositoryImpl::class.simpleName, "No album art found")
                    null
                } catch (e: Exception) {
                    logError(e)
                    null
                }
            }

            val bitmap = allAudioThumbnails[uri.toString()]
            emit(Optional.ofNullable(bitmap))
        }.catch {
            logError(it)
        }
    }

    override fun refresh() {
        retrieveAllAudioTracks()
    }

    private fun retrieveAllAudioTracks() {
        val audioTracks = mutableListOf<AudioTrack>()

        // TODO: Fix this later...
//        retrieveAudioData(MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
//            "${MediaStore.Audio.Media.DATA} like ?",
//            arrayOf("%/Music/%.mp3"),
//            audioTracks
//        )
        retrieveAudioData(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            audioTracks
        )

        allAudioTracks.update { audioTracks }
    }

    private fun retrieveAudioData(
        contentUri: Uri,
        selection: String?,
        selectionArgs: Array<String>?,
        audioTracks: MutableList<AudioTrack>
    ) {
        val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.GENRE_ID,
                MediaStore.Audio.Media.GENRE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
            )
        } else {
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
            )
        }
        val cursor = application.contentResolver.query(
            contentUri,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        )

        cursor?.run {
            val idColumn = getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val trackNumberColumn = getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val titleColumn = getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIDColumn = getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val artistColumn = getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIDColumn = getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val albumColumn = getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val yearColumn = getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            var genreIDColumn: Int? = null
            var genreColumn: Int? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                genreIDColumn = getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE)
                genreColumn = getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE_ID)
            }
            val durationColumn = getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (moveToNext()) {
                val id = getLong(idColumn)
                val trackNumber = getInt(trackNumberColumn)
                val title = getString(titleColumn)
                val artistID = getLong(artistIDColumn)
                val artist = getString(artistColumn)
                val albumID = getLong(albumIDColumn)
                val albumName = getString(albumColumn)
                val yearName = getInt(yearColumn)
                val genreIDName = genreIDColumn?.let { getLong(it) }
                val genreName = genreColumn?.let { getString(it) }
                val duration = getInt(durationColumn)
                val path = getString(dataColumn)

                val audioTrack = AudioTrack(
                    id,
                    trackNumber,
                    title,
                    artistID,
                    artist,
                    albumName,
                    yearName,
                    genreIDName,
                    genreName,
                    duration,
                    getAlbumCoverUri(albumID),
                    path
                )
                audioTracks.add(audioTrack)
            }
            cursor.close()
        }
    }

    private fun logError(e: Throwable) {
        Log.e(this@AudioTrackRepositoryImpl::class.simpleName, null, e)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun loadBitmapFromLocal(
        contentResolver: ContentResolver,
        uri: Uri
    ) = contentResolver.loadThumbnail(
        uri,
        Size(DEFAULT_THUMBNAIL_SIZE, DEFAULT_THUMBNAIL_SIZE),
        CancellationSignal()
    )

    private fun getAlbumCoverUri(albumId: Long): Uri {
        val albumArtUri = Uri.parse(ALBUM_ART_URI_PATH)
        return ContentUris.withAppendedId(albumArtUri, albumId)
    }
}
