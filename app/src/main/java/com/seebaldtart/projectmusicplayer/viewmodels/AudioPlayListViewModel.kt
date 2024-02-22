package com.seebaldtart.projectmusicplayer.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seebaldtart.projectmusicplayer.models.AudioTrack
import com.seebaldtart.projectmusicplayer.repositories.AudioTrackRepository
import com.seebaldtart.projectmusicplayer.utils.DispatcherProvider
import com.seebaldtart.projectmusicplayer.models.enums.AudioTrackFilter
import com.seebaldtart.projectmusicplayer.models.AudioGroupData
import com.seebaldtart.projectmusicplayer.models.AudioGroupDetails
import com.seebaldtart.projectmusicplayer.models.enums.GroupItemSelectionState
import com.seebaldtart.projectmusicplayer.models.enums.GroupSelectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Optional
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

@HiltViewModel
class AudioPlayListViewModel @Inject constructor(
    private val application: Application,
    private val dispatchersProvider: DispatcherProvider,
    private val repository: AudioTrackRepository
) : ViewModel() {

    private val selectedGroupSelectionState = MutableStateFlow(GroupSelectionState.ALL_TRACKS)
    private val currentlySelectedTrack = MutableStateFlow<Optional<AudioTrack>>(Optional.empty())
    private val selectedTrackFilter = MutableStateFlow(AudioTrackFilter.NONE)
    private val selectedPlayList = MutableStateFlow<List<AudioTrack>>(emptyList())
    private val _groupItemSelectionState = MutableStateFlow(GroupItemSelectionState.AUDIO_PLAY_LIST_SELECTION)

    /** The [StateFlow] for the currently selected [AudioTrack] */
    val selectedTrack: StateFlow<Optional<AudioTrack>> = currentlySelectedTrack
    /** The [StateFlow] for the currently selected [AudioTrackFilter] */
    val trackFilters: StateFlow<AudioTrackFilter> = selectedTrackFilter
    /** The [StateFlow] for the state of the groups selected */
    val groupSelectionState: StateFlow<GroupSelectionState> = selectedGroupSelectionState
    /** The [StateFlow] for the currently selected playlist */
    val selectedPlayListState: StateFlow<List<AudioTrack>> = selectedPlayList
    /** The [StateFlow] for the current [GroupItemSelectionState] */
    val groupItemSelectionState: StateFlow<GroupItemSelectionState> = _groupItemSelectionState

    init {
        viewModelScope.launch {
            stream()
                .onEach {
                    if (selectedPlayList.value.isEmpty()) {
                        selectedPlayList.emit(it)
                    }
                }.collect()
        }
    }

    /** This method streams all audio tracks found or currently focused on (via Groups) */
    fun stream(): StateFlow<List<AudioTrack>> = repository.stream()

    /** This function streams all audio track group data */
    fun streamByGroup(): StateFlow<AudioGroupData> =
        combine(selectedGroupSelectionState, stream()) { selection, audioTracks ->
            when (selection) {
                GroupSelectionState.ALL_TRACKS -> getAudioGroupDataForAllTracks(audioTracks)
                GroupSelectionState.ARTISTS -> getAudioGroupDataForArtists(audioTracks)
                GroupSelectionState.ALBUMS -> getAudioGroupDataForAlbums(audioTracks)
                GroupSelectionState.YEARS -> getAudioGroupDataForYears(audioTracks)
                GroupSelectionState.GENRES -> getAudioGroupDataForGenres(audioTracks)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), getAudioGroupDataForAllTracks(emptyList()))

    /** Calling this method will notify the ViewModel that a specific [AudioTrack] and should be played. */
    fun onAudioTrackSelected(track: AudioTrack?) {
        currentlySelectedTrack.update { Optional.ofNullable(track) }
    }

    /** Calling this method will display all [AudioTrack]s, grouped by their [GroupSelectionState]. */
    fun onGroupSelected(group: GroupSelectionState) {
        selectedGroupSelectionState.update { group }
    }

    /** Calling this method will update the currently selected playlist and set the [GroupItemSelectionState] to
     * [GroupItemSelectionState.AUDIO_PLAY_LIST] */
    fun onPlayListSelected(groupSelectionState: GroupSelectionState, groupDetails: AudioGroupDetails) {
        viewModelScope.launch(dispatchersProvider.io) {
            val tracks = stream().value
            val groupedTracks = when (groupSelectionState) {
                GroupSelectionState.ALL_TRACKS -> tracks
                GroupSelectionState.ALBUMS -> {
                    tracks.filter { track ->
                        getAlbumID(track.artistID, track.albumName) == groupDetails.id
                    }.sortedBy { it.albumName }
                }
                GroupSelectionState.ARTISTS -> {
                    tracks.filter { track ->
                        track.artistID.toString() == groupDetails.id
                    }.sortedBy { it.artistName }
                }
                GroupSelectionState.YEARS -> {
                    tracks.filter { track ->
                        track.year.toString() == groupDetails.id
                    }.sortedBy { it.year }
                }
                GroupSelectionState.GENRES -> {
                    tracks.filter { track ->
                        track.genreID.toString() == groupDetails.id
                    }.sortedBy { it.genre }
                }
            }

            selectedPlayList.update { groupedTracks }
            _groupItemSelectionState.update { GroupItemSelectionState.AUDIO_PLAY_LIST }
        }
    }

    /** Clears the previously cached group items. This will also set the [GroupItemSelectionState] back to
     * [GroupItemSelectionState.AUDIO_PLAY_LIST_SELECTION]. */
    fun clearGroupItemSelection() {
        _groupItemSelectionState.update { GroupItemSelectionState.AUDIO_PLAY_LIST_SELECTION }
        selectedPlayList.update { emptyList() }
    }

    /** Manually set the current [GroupItemSelectionState]. */
    fun setGroupItemSelectionState(groupItemSelectionState: GroupItemSelectionState) {
        _groupItemSelectionState.update { groupItemSelectionState }
    }

    /** Attempts to load the thumbnail [Bitmap] image for the provided [AudioTrack]s.
     * If the [Bitmap] is already available (cached), the cached image will be retrieved. */
    fun loadThumbnailsForTrack(track: AudioTrack) {
        val update = track.getThumbnailBitmap().value.getOrNull() == null
        loadThumbnailForUri(track.thumbnailUri, update, track::updateThumbnail)
    }

    /** Attempts to load the thumbnail [Bitmap] image for the provided [AudioGroupDetails]s.
     * If the [Bitmap] is already available (cached), the cached image will be retrieved. */
    fun loadThumbnailsForGroupDetails(audioGroupDetails: AudioGroupDetails) {
        val update = audioGroupDetails.getThumbnailBitmap().value.getOrNull() == null
        loadThumbnailForUri(audioGroupDetails.thumbnailUri, update, audioGroupDetails::updateThumbnail)
    }

    private fun loadThumbnailForUri(uri: Uri?, update: Boolean, block: (Bitmap) -> Unit) {
        if (update && uri != null) {
            // Retrieve the bitmap and update the track's thumbnail bitmap
            viewModelScope.launch(dispatchersProvider.io) {
                repository.getThumbnailForUri(application, uri)
                    .filter { bitmapOptional -> bitmapOptional.isPresent }
                    .onEach { bitmapOptional -> block.invoke(bitmapOptional.get()) }
                    .firstOrNull()
            }
        }
    }

    private fun getAudioGroupDataForAllTracks(audioTracks: List<AudioTrack>) =
        AudioGroupData(
            groupSelectionState = GroupSelectionState.ALL_TRACKS,
            groupDetails = emptyList(),
            tracks = audioTracks
        )

    private fun getAudioGroupDataForArtists(audioTracks: List<AudioTrack>) =
        AudioGroupData(
            groupSelectionState = GroupSelectionState.ARTISTS,
            groupDetails = audioTracks
                .distinctBy { it.artistID }
                .map {
                    AudioGroupDetails(
                        id = it.artistID.toString(),
                        name = it.artistName,
                        thumbnailUri = it.thumbnailUri
                    )
                }.sortedBy { it.name },
            tracks = emptyList()
        )

    private fun getAudioGroupDataForAlbums(audioTracks: List<AudioTrack>) =
        AudioGroupData(
            groupSelectionState = GroupSelectionState.ALBUMS,
            groupDetails = audioTracks
                .distinctBy { getAlbumID(it.artistID, it.albumName) }
                .map {
                    AudioGroupDetails(
                        id = getAlbumID(it.artistID, it.albumName),
                        name = it.albumName,
                        thumbnailUri = it.thumbnailUri
                    )
                }.sortedBy { it.name },
            tracks = emptyList()
        )

    private fun getAudioGroupDataForYears(audioTracks: List<AudioTrack>) =
        AudioGroupData(
            groupSelectionState = GroupSelectionState.YEARS,
            groupDetails = audioTracks
                .distinctBy { it.year }
                .map {
                    AudioGroupDetails(
                        id = it.year.toString(),
                        name = it.year.toString()
                    )
                }.sortedBy { it.name },
            tracks = emptyList()
        )

    private fun getAudioGroupDataForGenres(audioTracks: List<AudioTrack>) =
        AudioGroupData(
            groupSelectionState = GroupSelectionState.GENRES,
            groupDetails = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                audioTracks
                    .filter { it.genreID != null && it.genre != null }
                    .distinctBy { it.genreID }
                    .map {
                        AudioGroupDetails(
                            id = it.genreID.toString(),
                            name = it.genre!!
                        )
                    }.sortedBy { it.name }
            } else {
                emptyList()
            },
            tracks = emptyList()
        )

    private fun getAlbumID(artistID: Long, albumName: String) = "$artistID-$albumName"
}
