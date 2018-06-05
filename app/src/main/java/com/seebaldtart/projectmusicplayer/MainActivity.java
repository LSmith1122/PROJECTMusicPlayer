package com.seebaldtart.projectmusicplayer;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
public class MainActivity extends AppCompatActivity {
    static int currentPosition = 0;        // Current Playback Position
    static int cycle = 0;
    static ArrayList<String> songStringList = new ArrayList<>();
    static ArrayList<SongObject> songList = new ArrayList<>();
    static MediaPlayer media;
    static AudioManager audioManager;
    static java.lang.reflect.Field[] fields;
    static Context activityContext;
    static SongObject currentSong;
    static boolean focusGranted;
    static boolean wasPlaying;
    ImageButton playButton;
    ImageButton nextButton;
    ImageButton previousButton;
    static TextView selectedTrack;
    TextView currentDurationText;
    TextView totalDurationText;
    SeekBar seekBar;
    static Runnable runnable;
    static Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activityContext = getApplicationContext();
        playButton = findViewById(R.id.play);
        nextButton = findViewById(R.id.next);
        previousButton = findViewById(R.id.previous);
        selectedTrack = findViewById(R.id.current_song_text);
        currentDurationText = findViewById(R.id.current_duration);
        totalDurationText = findViewById(R.id.total_duration);
        seekBar = findViewById(R.id.seekbar);
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        handler = new Handler();
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFocusGranted()) {
                    if (media == null) {        // Nothing has been played...
                        playMediaFromPosition(0);
                        playButton.setImageResource(R.drawable.baseline_pause_white_24);
                    } else {        // If media has already been defined (if something has been played...)
                        if (media.isPlaying()) {     // If media IS currently playing...
                            media.pause();
                            playButton.setImageResource(R.drawable.baseline_play_arrow_white_24);
                        } else {
                            if (media != null) {
                                media.start();
                                playButton.setImageResource(R.drawable.baseline_pause_white_24);
                            }
                        }
                    }
                }
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (media.isPlaying()) {
                    media.stop();
                    media.release();
                    playMediaFromPosition(changeTrackTo(currentPosition, 1));
                    playButton.setImageResource(R.drawable.baseline_pause_white_24);
                } else {
                    media.stop();
                    media.release();
                    media = MediaPlayer.create(MainActivity.this, changeTrackTo(1));
                    playButton.setImageResource(R.drawable.baseline_play_arrow_white_24);
                }
            }
        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentDurationPosition = media.getCurrentPosition();
                if (media != null) {
                    if (media.isPlaying()) {
                        media.pause();
                        playButton.setImageResource(R.drawable.baseline_play_arrow_white_24);
                        if (currentDurationPosition < 3000) {       // change track
                            media.stop();
                            media.release();
                            media = MediaPlayer.create(MainActivity.this, changeTrackTo(-1));
                        } else {        // restart track
                            media.seekTo(0);
                            media.start();
                            playButton.setImageResource(R.drawable.baseline_pause_white_24);
                        }
                        media.start();
                        playButton.setImageResource(R.drawable.baseline_pause_white_24);
                    } else {
                        if (currentDurationPosition < 3000) {       // change track
                            media.stop();
                            media.release();
                            media = MediaPlayer.create(MainActivity.this, changeTrackTo(-1));
                        } else {        // restart track
                            media.seekTo(0);
                        }
                    }
                }
            }
        });
        fields = R.raw.class.getFields();
        for (int count = 0; count < fields.length; count++) {
            songList.add(new SongObject(fields[count]));
            songStringList.add(fields[count].getName());
        }
        AudioAdapter adapter = new AudioAdapter(this, songList);
        ListView listView = findViewById(R.id.item_list);
        listView.setAdapter(adapter);
        if (media == null) {
            media = MediaPlayer.create(MainActivity.this, getMediaAtPosition(0));
            media.setOnCompletionListener(mCompletionListener);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isFocusGranted()) {
                    if (media != null) {
                        media.release();
                        playButton.setImageResource(R.drawable.baseline_play_arrow_white_24);
                    }
                    playMediaFromPosition(position);
                    playButton.setImageResource(R.drawable.baseline_pause_white_24);
                }
            }
        });
        selectedTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SelectedSong.class);
                startActivity(intent);
            }
        });
        initializeSeekbar();
        playCycle();
    }
    public void playNextSong() {
        media.stop();
        media.release();
        playMediaFromPosition(changeTrackTo(currentPosition, 1));
        playButton.setImageResource(R.drawable.baseline_pause_white_24);
    }
    public void repeatCurrentSong() {
        media.pause();
        media.seekTo(0);
        media.start();
        playButton.setImageResource(R.drawable.baseline_pause_white_24);
    }
    MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            int capOff = songList.size() - 1;
            if (cycle == 0) {           // Repeat List of songs...
                playNextSong();
            } else {            // Repeat current song only...
                repeatCurrentSong();
            }
        }
    };
    static AudioManager.OnAudioFocusChangeListener mFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                releaseMediaPlayer();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                if (media.isPlaying()) {
                    media.pause();
                    wasPlaying = true;
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                if (wasPlaying) {
                    media.start();
                }
            }
        }
    };
    public boolean isFocusGranted() {
        int result = audioManager.requestAudioFocus(mFocusChangeListener, audioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            focusGranted = true;
            return true;
        }
        focusGranted = false;
        return false;
    }
    public static void releaseMediaPlayer () {
        if (media != null) {
            media.release();
            media = null;
            audioManager.abandonAudioFocus(mFocusChangeListener);
            currentPosition = 0;
        }
    }
    public void playMediaFromPosition(int pos) {
        media = MediaPlayer.create(MainActivity.this, getMediaAtPosition(pos));
        media.start();
    }
    public int getMediaAtPosition(int pos) {
        currentPosition = pos;
        setCurrentSong(pos);
        int resID = getResources().getIdentifier(songStringList.get(pos), "raw", getPackageName());
        return resID;
    }
    public int changeTrackTo(int value) {
        if (media != null) {
            int nextPos = currentPosition + value;
            if (nextPos < 0) {     // Out of bounds: reverting to bottom of list
                currentPosition = songList.size() - 1;
            }
            else if (nextPos >= 0 && nextPos < songList.size()){
                currentPosition = nextPos;
            }
            else if (nextPos >= songList.size()) {     // Out of bounds: reverting back to top of list
                currentPosition = 0;
            }
        } else {
            currentPosition = 0;
        }
        currentSong = songList.get(currentPosition);
        selectedTrack.setText(getSelectedTrackText(currentSong));
        int resID = getResources().getIdentifier(songStringList.get(currentPosition), "raw", getPackageName());
        return resID;
    }
    public int changeTrackTo(int sv, int increment) {
        int startingValue = sv;
        if (media != null) {
            int nextPos = startingValue + increment;
            if (nextPos < 0) {     // Out of bounds: reverting to bottom of list
                currentPosition = songList.size() - 1;
                startingValue = startingValue -1;
            }
            else if (nextPos >= 0 && nextPos < songList.size()){
                currentPosition = nextPos;
                startingValue = nextPos;
            }
            else if (nextPos >= songList.size()) {     // Out of bounds: reverting back to top of list
                currentPosition = 0;
                startingValue = 0;
            }
        } else {
            currentPosition = 0;
            startingValue = 0;
        }
        currentSong = songList.get(currentPosition);
        selectedTrack.setText(getSelectedTrackText(currentSong));
        return startingValue;
    }
    public void setCurrentSong(int pos) {
        currentSong = songList.get(pos);
        selectedTrack.setText(getSelectedTrackText(currentSong));
    }
    public String getSelectedTrackText(SongObject song) {
        String songTitle = song.getSongTitle();
        String artistName = song.getArtistName();
        return songTitle + " - " + artistName;
    }
    public String getMediaTime(int time) {
        int min = (time / 1000) / 60;
        int sec = (time / 1000) % 60;
        NumberFormat formatter = new DecimalFormat("00");
        String minutes = "";
        if (min < 10) {
            minutes = String.format("%02d", min);
        } else {
            minutes = String.format("%01d", min);
        }
        String seconds = formatter.format(sec);
        return minutes + ":" + seconds;
    }
    private void initializeSeekbar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int currentProgress;
            boolean mediaWasPlaying = false;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean inputFromUser) {
                seekBar.setProgress(progress);
                currentProgress = progress;
                if (inputFromUser) {
                    currentDurationText.setText(getMediaTime(progress));
                }
                currentDurationText.setText(getMediaTime(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (media.isPlaying()) {
                    media.pause();
                    mediaWasPlaying = true;
                }
                currentDurationText.setText(getMediaTime(currentProgress));
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                currentDurationText.setText(getMediaTime(currentProgress));
                if (mediaWasPlaying) {
                    media.seekTo(currentProgress);
                    media.start();
                    mediaWasPlaying = false;
                }
            }
        });
    }
    private void playCycle() {              // Retrieved from: https://www.youtube.com/watch?v=HB3DoZh1QWU
        if (media != null) {
            seekBar.setProgress(media.getCurrentPosition());
            seekBar.setMax(media.getDuration());
            totalDurationText.setText(getMediaTime(media.getDuration()));
            media.setOnCompletionListener(mCompletionListener);
            runnable = new Runnable() {
                @Override
                public void run() {
                    playCycle();
                }
            };
            handler.postDelayed(runnable, 100);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (media != null) {
            if (!media.isPlaying()) {
                wasPlaying = false;
            } else {
                wasPlaying = true;
            }
        }
        initializeSeekbar();
    }
    @Override
    protected void onStop () {
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        handler.removeCallbacks(runnable);          // Retrieved from: https://www.youtube.com/watch?v=HB3DoZh1QWU
    }
}