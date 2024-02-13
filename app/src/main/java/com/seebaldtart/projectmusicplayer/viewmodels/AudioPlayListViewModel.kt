package com.seebaldtart.projectmusicplayer.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seebaldtart.projectmusicplayer.models.AudioTrack
import com.seebaldtart.projectmusicplayer.repositories.AudioTrackRepository
import com.seebaldtart.projectmusicplayer.utils.DispatcherProvider
import com.seebaldtart.projectmusicplayer.models.data.enums.AudioTrackFilter
import com.seebaldtart.projectmusicplayer.models.AudioSelectionData
import com.seebaldtart.projectmusicplayer.models.data.enums.GroupSelectionState
import com.seebaldtart.projectmusicplayer.models.data.enums.TrackState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
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
    private val groupSelection = MutableStateFlow<Optional<AudioSelectionData>>(Optional.empty())

    /** The [StateFlow] for the currently selected [AudioTrack] */
    val selectedTrack: StateFlow<Optional<AudioTrack>> = currentlySelectedTrack.asStateFlow()
    /** The [StateFlow] for the currently selected [AudioTrackFilter] */
    val trackFilters: StateFlow<AudioTrackFilter>
        get() = selectedTrackFilter.asStateFlow()
    /** The [StateFlow] for the state of the groups selected */
    val groupSelectionState: StateFlow<GroupSelectionState>
        get() = selectedGroupSelectionState.asStateFlow()

    /** This method streams all audio tracks found or currently focused on (via Groups) */
    fun stream(): StateFlow<List<AudioTrack>> = repository.stream()

    /** Calling this method will notify the ViewModel that a specific [AudioTrack] and should be played. */
    fun onAudioTrackSelected(track: AudioTrack?) {
        currentlySelectedTrack.update { Optional.ofNullable(track) }
    }

    /** Calling this method will display all [AudioTrack]s, grouped by their [GroupSelectionState]. */
    fun onGroupSelected(group: GroupSelectionState) {
        selectedGroupSelectionState.update { group }
    }

    /** Sets the currently selected [GroupSelectionState] based on the Artist, Album, and [AudioTrack] playlist, if available. */
    fun setSelectionGroup(selectionData: AudioSelectionData?) {
        groupSelection.update { Optional.ofNullable(selectionData) }
    }

    /** Attempts to load the thumbnail [Bitmap] image for the provided [AudioTrack]s.
     * If the [Bitmap] is already available */
    fun loadThumbnailsForTrack(track: AudioTrack) {
        val shouldUpdate = track.thumbnailUri != null && track.getThumbnailBitmap().value.getOrNull() == null
        if (shouldUpdate) {
            // Retrieve the bitmap and update the track's thumbnail bitmap
            viewModelScope.launch(dispatchersProvider.io) {
                repository.getThumbnailForUri(application, track.thumbnailUri!!)
                    .filter { bitmapOptional -> bitmapOptional.isPresent }
                    .onEach { bitmapOptional -> track.updateThumbnail(bitmapOptional.get()) }
                    .firstOrNull()
            }
        }
    }

    /** Attempts to load the thumbnail [Bitmap] image for the provided [AudioTrack]s.
     * If the [Bitmap] is already available */
    fun loadThumbnailsForTracks(tracks: List<AudioTrack>) {
        // Update each selected track's thumbnail
        tracks.onEach { track ->
            loadThumbnailsForTrack(track)
        }
    }
}
