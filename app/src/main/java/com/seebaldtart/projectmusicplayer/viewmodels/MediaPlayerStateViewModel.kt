package com.seebaldtart.projectmusicplayer.viewmodels

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seebaldtart.projectmusicplayer.models.AudioTrack
import com.seebaldtart.projectmusicplayer.models.data.enums.LoopState
import com.seebaldtart.projectmusicplayer.models.data.enums.PlaybackError
import com.seebaldtart.projectmusicplayer.models.data.enums.TrackState
import com.seebaldtart.projectmusicplayer.utils.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Optional
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

// If the user presses the rewind button within 3 seconds of its current play-time,
// the previous or last track will be played. Otherwise, the current track will be
// restarted from the beginning.
private const val MIN_ELAPSED_TIME_FOR_REWIND_MS = 3 * 1000 // 3 seconds

@HiltViewModel
class MediaPlayerStateViewModel @Inject constructor(
    private val application: Application,
    dispatchersProvider: DispatcherProvider
) : ViewModel() {

    private val audioManager: AudioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var delegate: Delegate? = null
    private val trackState = MutableStateFlow(TrackState.STOPPED)
    private val currentAudioTrack = MutableStateFlow<Optional<AudioTrack>>(Optional.empty())
    private val previouslySelectedAudioTrack = MutableStateFlow<Optional<AudioTrack>>(Optional.empty())
    private val currentPlaylist = MutableStateFlow<List<AudioTrack>>(emptyList())
    private val loopState = MutableStateFlow(LoopState.NONE)
    private val audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> releaseMediaPlayerAndFocus()
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> pauseAndPersistState()
                AudioManager.AUDIOFOCUS_GAIN -> resumePreviousState()
            }
        }
    private val audioCompletionListener: MediaPlayer.OnCompletionListener =
        MediaPlayer.OnCompletionListener {
            when (loopState.value) {
                LoopState.NONE -> onAudioCompleteForNoLoop()
                LoopState.ONE -> onAudioCompleteForSingleLoop()
                LoopState.ALL -> onAudioCompleteForLoopAll()
            }
        }
    val nowPlayingTrack: Flow<Optional<AudioTrack>> =
        combine(trackState, currentAudioTrack) { state, trackOptional ->
            val optional = if (isMediaPlaying() || state == TrackState.PAUSED) {
                val track = trackOptional.getOrNull()
                Optional.ofNullable(track)
            } else {
                Optional.empty()
            }
            delegate?.onTrackAutomaticallyUpdated(optional.getOrNull())
            optional
        }.stateIn(viewModelScope, SharingStarted.Lazily, Optional.empty())
    val nowPlayingTrackProgress: Flow<Int> =
        flow {
            while (currentCoroutineContext().isActive) {
                (mediaPlayer?.currentPosition ?: 0).run { emit(this) }
                delay(100)
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        addCloseable {
            releaseMediaPlayerAndFocus()
        }
        viewModelScope.launch(dispatchersProvider.io) {
            currentAudioTrack
                .filter { it.isPresent }
                .map { it.get() }
                .onEach { track: AudioTrack ->
                    currentAudioTrack.replayCache.let { replayCache ->
                        val previouslySelectedTrackIndex = 0.coerceAtLeast(replayCache.lastIndex.minus(1))
                        val previouslySelectedTrack = replayCache[previouslySelectedTrackIndex]
                            .takeUnless { it.get() == track }
                            ?: Optional.empty()
                        if (previouslySelectedTrack.isPresent && previouslySelectedTrack.get().id != track.id) {
                            delegate?.onTrackAutomaticallyUpdated(track)
                        }
                    }
                }.collect()
        }
    }

    fun getTrackState(): StateFlow<TrackState> = trackState

    fun setCurrentPlaylist(tracks: List<AudioTrack>) {
        currentPlaylist.update { tracks }
    }

    fun setDelegate(delegate: Delegate?) {
        this.delegate = delegate
    }

    fun onAudioTrackSelected(id: Long) {
        currentPlaylist
            .value
            .firstOrNull { it.id == id }
            ?.let { track ->
                updateCurrentAudioTrack(track)
            }
            ?: run {
                Log.e(
                    this::class.simpleName,
                    null,
                    RuntimeException("No track with the given ID was found in selection. Was the playlist updated prior to selecting a track?")
                )
            }
    }

    fun onAudioTrackSelected(track: AudioTrack) {
        updateCurrentAudioTrack(track)
    }

    fun onPlayClicked(context: Context) {
        currentAudioTrack
            .value
            .getOrNull()
            ?.let { playTrack(context, it) }
            ?: run {
                delegate?.onError(PlaybackError.PLAY_FAILED)
            }
    }

    fun onPauseClicked() {
        currentAudioTrack
            .value
            .getOrNull()
            ?.let { pauseTrack() }
            ?: run {
                if (mediaPlayer?.isPlaying == true) {
                    delegate?.onError(PlaybackError.UNEXPECTED)
                }
            }
    }

    fun onStopClicked() {
        updateMediaPlayerState(TrackState.STOPPED)
    }

    fun onPreviousClicked(context: Context) {
        val previousTrack = getPreviousTrack()
        if (isMediaPlaying()) {
            if (mediaPlayer!!.currentPosition < MIN_ELAPSED_TIME_FOR_REWIND_MS) {
                previousTrack?.let { playTrack(context, it, true) }
            } else {
                pauseTrack()
                mediaPlayer!!.seekTo(0)
                mediaPlayer!!.start()
                updateMediaPlayerState(TrackState.PLAYING)
            }
        } else {
            if (mediaPlayer?.let { it.currentPosition < MIN_ELAPSED_TIME_FOR_REWIND_MS } == true) {
                updateMediaPlayerState(TrackState.STOPPED)
                previousTrack?.let {
                    mediaPlayer = getMediaPlayer(context, previousTrack).apply {
                        prepare()
                        updateCurrentAudioTrack(previousTrack)
                    }
                }
            } else {
                mediaPlayer?.seekTo(0)
            }
        }
    }

    fun onNextClicked(context: Context) {
        getNextTrack()?.let { nextTrack ->
            if (isMediaPlaying()) {
                playTrack(context, nextTrack, true)
            } else {
                updateMediaPlayerState(TrackState.STOPPED)
                mediaPlayer = getMediaPlayer(context, nextTrack).apply {
                    prepare()
                    updateCurrentAudioTrack(nextTrack)
                }
            }
        }
    }

    fun onLoopClicked() {
        val newState = when (loopState.value) {
            LoopState.NONE -> LoopState.ONE
            LoopState.ONE -> LoopState.ALL
            LoopState.ALL -> LoopState.NONE
        }

        mediaPlayer?.isLooping = newState == LoopState.ONE
        loopState.update { newState }
    }

    fun updateTrackCurrentPosition(seekMilliseconds: Int) {
        if (!isAudioFocusGranted()) {
            return
        }

        if (isMediaPlaying()) {
            pauseTrack()
            mediaPlayer?.seekTo(seekMilliseconds)
            mediaPlayer?.start()
            updateMediaPlayerState(TrackState.PLAYING)
        } else {
            mediaPlayer?.seekTo(seekMilliseconds)
        }
    }

    private fun getMediaPlayer(context: Context, track: AudioTrack) =
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(context, Uri.parse(track.path))
            setOnCompletionListener(audioCompletionListener)
            isLooping = loopState.value == LoopState.ONE
        }

    private fun playTrack(context: Context, track: AudioTrack, force: Boolean = false) {
        val previouslySelectedTrack = previouslySelectedAudioTrack.value.getOrNull()
        val shouldPlayNewTrack = previouslySelectedTrack?.id != track.id
        if (force || mediaPlayer == null || shouldPlayNewTrack) {
            updateMediaPlayerState(TrackState.STOPPED, false)
            mediaPlayer = getMediaPlayer(context, track)
                .apply {
                    setOnPreparedListener {
                        // Prepare the next song, but don't play it if we do not have focus
                        updateMediaPlayerState(TrackState.PLAYING)
                        updateCurrentAudioTrack(track)
                    }
                    prepareAsync()
                }
        } else {
            updateMediaPlayerState(TrackState.PLAYING)
        }
    }

    private fun updateCurrentAudioTrack(track: AudioTrack) {
        previouslySelectedAudioTrack.update { currentAudioTrack.value }
        currentAudioTrack.update { Optional.of(track) }
    }

    private fun clearCurrentAudioTrack() {
        previouslySelectedAudioTrack.update { Optional.empty() }
        currentAudioTrack.update { Optional.empty() }
    }

    private fun getCurrentTrackIndex(): Int? =
        currentAudioTrack.value.getOrNull()?.let {
            currentPlaylist.value
                .mapIndexedNotNull { index, audioTrack ->
                    if (audioTrack.id == it.id) {
                        index
                    } else {
                        null
                    }
                }.firstOrNull()
        }

    private fun pauseTrack() {
        if (isMediaPlaying()) {
            updateMediaPlayerState(TrackState.PAUSED)
        }
    }

    private fun getPreviousTrack(): AudioTrack? {
        return if (currentPlaylist.value.isNotEmpty() && currentAudioTrack.value.getOrNull() != null) {
            getCurrentTrackIndex()?.let { currentPosition ->
                val isLastPosition = currentPosition == 0
                val lastIndex = currentPlaylist.value.lastIndex
                val previousIndex = if (isLastPosition) lastIndex else currentPosition - 1
                currentPlaylist.value[previousIndex]
            }
        } else {
            null
        }
    }

    private fun getNextTrack(): AudioTrack? {
        return if (currentPlaylist.value.isNotEmpty() && currentAudioTrack.value.getOrNull() != null) {
            getCurrentTrackIndex()?.let { currentIndex ->
                val isWithinRange = currentIndex < currentPlaylist.value.size - 1
                val nextTrackIndex = if (isWithinRange) currentIndex + 1 else 0
                currentPlaylist.value[nextTrackIndex]
            }
        } else {
            null
        }
    }

    private fun isMediaPlaying() = try {
        mediaPlayer?.isPlaying == true
    } catch (e: Exception) {
        Log.e(this::class.simpleName, null, e)
        false
    }

    private fun updateMediaPlayerState(
        trackState: TrackState,
        updateTrackState: Boolean = true
    ) {
        try {
            when (trackState) {
                TrackState.PLAYING -> {
                    if (isAudioFocusGranted()) {
                        mediaPlayer?.start()
                    }
                }
                TrackState.PAUSED -> mediaPlayer?.pause()
                TrackState.STOPPED -> mediaPlayer?.stop()
            }
        } catch (exception: IllegalStateException) {
            val notifyDelegate: () -> Unit
            val errorMessage = when (trackState) {
                TrackState.PLAYING -> {
                    val message = "Unexpected error. Media Player is in an invalid state while attempting to start..."
                    notifyDelegate = { delegate?.onError(PlaybackError.PLAY_FAILED) }
                    message
                }
                TrackState.PAUSED -> {
                    val message = "Unexpected error. Media Player is in an invalid state while attempting to pause..."
                    notifyDelegate = { delegate?.onError(PlaybackError.UNEXPECTED) }
                    message
                }
                TrackState.STOPPED -> {
                    val message = "Media Player has not been initialized before stopping"
                    notifyDelegate = { delegate?.onError(PlaybackError.UNEXPECTED) }
                    message
                }
            }
            Log.e(this::class.simpleName, errorMessage, exception)
            notifyDelegate.invoke()
        } finally {
            if (updateTrackState) {
                mediaPlayer?.let {
                    this.trackState.update { trackState }
                    if (trackState == TrackState.STOPPED) {
                        it.release()
                        clearCurrentAudioTrack()
                    }
                } ?: this.trackState.update { TrackState.STOPPED }
            }
        }
    }

    private fun isAudioFocusGranted(): Boolean {
        val request = AudioFocusRequest
            .Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()
        return audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun releaseMediaPlayerAndFocus() {
        mediaPlayer?.reset()
        mediaPlayer?.release()
        mediaPlayer = null
        val audioFocusRequest = AudioFocusRequest
            .Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()
        audioManager.abandonAudioFocusRequest(audioFocusRequest)
        clearCurrentAudioTrack()
    }

    private fun resumePreviousState() {
        mediaPlayer?.run {
            val trackState = trackState.replayCache.lastOrNull()
            if (trackState == TrackState.PLAYING) {
                start()
            }
        }
    }

    private fun pauseAndPersistState() {
        mediaPlayer?.run {
            if (isPlaying) {
                pause()
                trackState.update { TrackState.PLAYING }
            }
        }
    }

    private fun onAudioCompleteForNoLoop() {
        getCurrentTrackIndex()?.let { currentIndex ->
            if (currentIndex < currentPlaylist.value.lastIndex) {
                getNextTrack()?.let { track -> playTrack(application, track, true) }
            } else {
                updateMediaPlayerState(TrackState.STOPPED)
            }
        }
    }

    private fun onAudioCompleteForSingleLoop() {
        updateMediaPlayerState(TrackState.PAUSED, false)
        mediaPlayer?.seekTo(0)
        updateMediaPlayerState(TrackState.PLAYING, false)
    }

    private fun onAudioCompleteForLoopAll() {
        getNextTrack()?.let { track -> playTrack(application, track, true) }
    }

    interface Delegate {
        fun onError(error: PlaybackError)
        fun onTrackAutomaticallyUpdated(track: AudioTrack?)
    }
}
