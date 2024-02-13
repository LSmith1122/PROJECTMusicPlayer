package com.seebaldtart.projectmusicplayer.ui.activities

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Bundle
import android.os.Handler
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.seebaldtart.projectmusicplayer.R
import com.seebaldtart.projectmusicplayer.models.SongObject
import com.seebaldtart.projectmusicplayer.viewmodels.MusicPlayerStateViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Field
import java.text.DecimalFormat
import java.text.NumberFormat
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var playButton: ImageButton? = null
    private var nextButton: ImageButton? = null
    private var previousButton: ImageButton? = null
    private var currentDurationText: TextView? = null
    private var totalDurationText: TextView? = null
    private var seekBar: SeekBar? = null

    private var currentPosition: Int = 0 // Current Playback Position
    private var cycle: Int = 0
    private var songStringList: ArrayList<String> = ArrayList()
    private var songList: ArrayList<SongObject> = ArrayList()
    private var media: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var fields: Array<Field>? = null
    private var activityContext: Context? = null
    private var currentSong: SongObject? = null
    private var focusGranted: Boolean = false
    private var wasPlaying: Boolean = false
    private var selectedTrack: TextView? = null
    private var runnable: Runnable? = null
    private var handler: Handler? = null

    lateinit var viewModel: MusicPlayerStateViewModel

    var mFocusChangeListener: AudioManager.OnAudioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                releaseMediaPlayer()
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                if (media!!.isPlaying) {
                    media!!.pause()
                    wasPlaying = true
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                if (wasPlaying) {
                    media!!.start()
                }
            }
        }

    private fun releaseMediaPlayer() {
        if (media != null) {
            media!!.release()
            media = null
            audioManager!!.abandonAudioFocus(mFocusChangeListener)
            currentPosition = 0
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activityContext = applicationContext
        playButton = findViewById(R.id.play) as? ImageButton
        nextButton = findViewById(R.id.next) as? ImageButton
        previousButton = findViewById(R.id.previous) as? ImageButton
        selectedTrack = findViewById(R.id.current_song_text)
        currentDurationText = findViewById(R.id.current_duration)
        totalDurationText = findViewById(R.id.total_duration)
        seekBar = findViewById(R.id.seekbar)
        audioManager = applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager
        handler = Handler()

        setContent {
            viewModel = hiltViewModel<MusicPlayerStateViewModel>()
        }

//        playButton.setOnClickListener(View.OnClickListener { v: View? ->
//            if (isFocusGranted) {
//                if (media == null) {        // Nothing has been played...
//                    playMediaFromPosition(0)
//                    playButton.setImageResource(R.drawable.baseline_pause_white_24)
//                } else {        // If media has already been defined (if something has been played...)
//                    if (media!!.isPlaying) {     // If media IS currently playing...
//                        media!!.pause()
//                        playButton.setImageResource(R.drawable.baseline_play_arrow_white_24)
//                    } else {
//                        if (media != null) {
//                            media!!.start()
//                            playButton.setImageResource(R.drawable.baseline_pause_white_24)
//                        }
//                    }
//                }
//            }
//        })
//        nextButton.setOnClickListener(View.OnClickListener { v: View? ->
//            if (media!!.isPlaying) {
//                media!!.stop()
//                media!!.release()
//                playMediaFromPosition(changeTrackTo(currentPosition, 1))
//                playButton.setImageResource(R.drawable.baseline_pause_white_24)
//            } else {
//                media!!.stop()
//                media!!.release()
//                media = MediaPlayer.create(this@MainActivity, changeTrackTo(1))
//                playButton.setImageResource(R.drawable.baseline_play_arrow_white_24)
//            }
//        })
//        previousButton.setOnClickListener(View.OnClickListener { v: View? ->
//            val currentDurationPosition = media!!.currentPosition
//            if (media != null) {
//                if (media!!.isPlaying) {
//                    media!!.pause()
//                    playButton.setImageResource(R.drawable.baseline_play_arrow_white_24)
//                    if (currentDurationPosition < 3000) {       // change track
//                        media!!.stop()
//                        media!!.release()
//                        media = MediaPlayer.create(this@MainActivity, changeTrackTo(-1))
//                    } else {        // restart track
//                        media!!.seekTo(0)
//                        media!!.start()
//                        playButton.setImageResource(R.drawable.baseline_pause_white_24)
//                    }
//                    media!!.start()
//                    playButton.setImageResource(R.drawable.baseline_pause_white_24)
//                } else {
//                    if (currentDurationPosition < 3000) {       // change track
//                        media!!.stop()
//                        media!!.release()
//                        media = MediaPlayer.create(this@MainActivity, changeTrackTo(-1))
//                    } else {        // restart track
//                        media!!.seekTo(0)
//                    }
//                }
//            }
//        })
//        fields = raw::class.java.fields
//        for (count in fields.indices) {
//            songList.add(SongObject(fields[count]))
//            songStringList.add(fields[count].name)
//        }
//        val adapter =
//            AudioAdapter(
//                this,
//                songList
//            )
//        val listView = findViewById<ListView>(R.id.item_list)
//        listView.adapter = adapter
//        if (media == null) {
//            media = MediaPlayer.create(this@MainActivity, getMediaAtPosition(0))
//            media.setOnCompletionListener(mCompletionListener)
//        }
//        listView.onItemClickListener =
//            OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
//                if (isFocusGranted) {
//                    if (media != null) {
//                        media!!.release()
//                        playButton.setImageResource(R.drawable.baseline_play_arrow_white_24)
//                    }
//                    playMediaFromPosition(position)
//                    playButton.setImageResource(R.drawable.baseline_pause_white_24)
//                }
//            }
//        selectedTrack.setOnClickListener(View.OnClickListener { v: View? ->
//            val intent = Intent(this@MainActivity, SelectedSongActivity::class.java)
//            startActivity(intent)
//        })
        initializeSeekbar()
        playCycle()
    }

    fun playNextSong() {
        media!!.stop()
        media!!.release()
        playMediaFromPosition(changeTrackTo(currentPosition, 1))
        playButton!!.setImageResource(R.drawable.baseline_pause_white_24)
    }

    fun repeatCurrentSong() {
        media!!.pause()
        media!!.seekTo(0)
        media!!.start()
        playButton!!.setImageResource(R.drawable.baseline_pause_white_24)
    }

    var mCompletionListener: OnCompletionListener = OnCompletionListener {
        val capOff = songList.size - 1
        if (cycle == 0) {           // Repeat List of songs...
            playNextSong()
        } else {            // Repeat current song only...
            repeatCurrentSong()
        }
    }
    val isFocusGranted: Boolean
        get() {
            val result = audioManager!!.requestAudioFocus(
                mFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                focusGranted = true
                return true
            }
            focusGranted = false
            return false
        }

    fun playMediaFromPosition(pos: Int) {
//        media = MediaPlayer.create(this@MainActivity, getMediaAtPosition(pos))
//        media.start()
    }

    fun getMediaAtPosition(pos: Int): Int {
        currentPosition = pos
        setCurrentSong(pos)
        val resID = resources.getIdentifier(songStringList[pos], "raw", packageName)
        return resID
    }

    fun changeTrackTo(value: Int): Int {
        if (media != null) {
            val nextPos = currentPosition + value
            if (nextPos < 0) {     // Out of bounds: reverting to bottom of list
                currentPosition = songList.size - 1
            } else if (nextPos >= 0 && nextPos < songList.size) {
                currentPosition = nextPos
            } else if (nextPos >= songList.size) {     // Out of bounds: reverting back to top of list
                currentPosition = 0
            }
        } else {
            currentPosition = 0
        }
        currentSong = songList[currentPosition]
        selectedTrack!!.text =
            getSelectedTrackText(currentSong)
        val resID = resources.getIdentifier(songStringList[currentPosition], "raw", packageName)
        return resID
    }

    fun changeTrackTo(sv: Int, increment: Int): Int {
        var startingValue = sv
        if (media != null) {
            val nextPos = startingValue + increment
            if (nextPos < 0) {     // Out of bounds: reverting to bottom of list
                currentPosition = songList.size - 1
                startingValue = startingValue - 1
            } else if (nextPos >= 0 && nextPos < songList.size) {
                currentPosition = nextPos
                startingValue = nextPos
            } else if (nextPos >= songList.size) {     // Out of bounds: reverting back to top of list
                currentPosition = 0
                startingValue = 0
            }
        } else {
            currentPosition = 0
            startingValue = 0
        }
        currentSong = songList[currentPosition]
        selectedTrack!!.text =
            getSelectedTrackText(currentSong)
        return startingValue
    }

    fun setCurrentSong(pos: Int) {
        currentSong = songList[pos]
        selectedTrack!!.text =
            getSelectedTrackText(currentSong)
    }

    fun getSelectedTrackText(song: SongObject?): String {
        val songTitle = song!!.songTitle
        val artistName = song.artistName
        return "$songTitle - $artistName"
    }

    fun getMediaTime(time: Int): String {
        val min = (time / 1000) / 60
        val sec = (time / 1000) % 60
        val formatter: NumberFormat = DecimalFormat("00")
        var minutes = ""
        minutes = if (min < 10) {
            String.format("%02d", min)
        } else {
            String.format("%01d", min)
        }
        val seconds = formatter.format(sec.toLong())
        return "$minutes:$seconds"
    }

    private fun initializeSeekbar() {
        seekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            var currentProgress: Int = 0
            var mediaWasPlaying: Boolean = false

            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                inputFromUser: Boolean
            ) {
                seekBar.progress = progress
                currentProgress = progress
                if (inputFromUser) {
                    currentDurationText!!.text = getMediaTime(progress)
                }
                currentDurationText!!.text = getMediaTime(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                if (media!!.isPlaying) {
                    media!!.pause()
                    mediaWasPlaying = true
                }
                currentDurationText!!.text = getMediaTime(currentProgress)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                currentDurationText!!.text = getMediaTime(currentProgress)
                if (mediaWasPlaying) {
                    media!!.seekTo(currentProgress)
                    media!!.start()
                    mediaWasPlaying = false
                }
            }
        })
    }

    private fun playCycle() {
//        if (media != null) {
//            seekBar!!.progress = media!!.currentPosition
//            seekBar!!.max = media!!.duration
//            totalDurationText!!.text = getMediaTime(media!!.duration)
//            media!!.setOnCompletionListener(mCompletionListener)
//            runnable = Runnable { playCycle() }
//            handler!!.postDelayed(runnable, 100)
//        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (media != null) {
            if (!media!!.isPlaying) {
                wasPlaying = false
            } else {
                wasPlaying = true
            }
        }
        initializeSeekbar()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
        handler!!.removeCallbacks(runnable!!)
    }

}