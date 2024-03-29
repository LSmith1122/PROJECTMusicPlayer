package com.seebaldtart.projectmusicplayer.ui.activities;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SelectedSongActivity extends AppCompatActivity {
    TextView currentSongTitle;
    TextView currentArtist;
    TextView currentAlbum;
    TextView currentDurationText;
    TextView totalDurationText;
    ImageView currentAlbumArt;
    ImageButton playButton;
    ImageButton nextButton;
    ImageButton previousButton;
    ImageButton upButton;
    ImageButton cycleButton;
    SeekBar seekBar;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.selected_song_activity);
//        currentSongTitle = findViewById(R.id.song_title);
//        currentArtist = findViewById(R.id.artist_name);
//        currentAlbum = findViewById(R.id.album_title);
//        currentDurationText = findViewById(R.id.current_duration);
//        totalDurationText = findViewById(R.id.total_duration);
//        currentAlbumArt = findViewById(R.id.album_artwork);
//        playButton = findViewById(R.id.play);
//        nextButton = findViewById(R.id.next);
//        previousButton = findViewById(R.id.previous);
//        upButton = findViewById(R.id.upButton);
//        seekBar = findViewById(R.id.seekbar);
//        cycleButton = findViewById(R.id.cycle);
//        if (MainActivity.cycle == 0) {
//            cycleButton.setImageResource(R.drawable.baseline_repeat_white_24);
//        } else {
//            cycleButton.setImageResource(R.drawable.baseline_repeat_one_white_24);
//        }
//        cycleButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MainActivity.cycle++;
//                if (MainActivity.cycle > 1) {
//                    MainActivity.cycle = 0;
//                }
//                if (MainActivity.cycle == 0) {
//                    cycleButton.setImageResource(R.drawable.baseline_repeat_white_24);
//                } else {
//                    cycleButton.setImageResource(R.drawable.baseline_repeat_one_white_24);
//                }
//            }
//        });
//        if (MainActivity.currentSong != null && MainActivity.media.isPlaying()) {
//            playButton.setImageResource(R.drawable.baseline_pause_white_24);
//        } else {
//            playButton.setImageResource(R.drawable.baseline_play_arrow_white_24);
//        }
//        upButton.setOnClickListener(v -> {
//            Intent intent = new Intent(SelectedSongActivity.this, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);         // Retrieved from https://stackoverflow.com/questions/41344515/switching-between-activities-without-destroying-the-activity
//            startActivity(intent);
//        });
//        playButton.setOnClickListener(v -> {
//            if (isFocusGranted()) {
//                if (MainActivity.media == null) {        // Nothing has been played...
//                    playMediaFromPosition(0);
//                    playButton.setImageResource(R.drawable.baseline_pause_white_24);
//                } else {        // If MainActivity.media has already been defined (if something has been played...)
//                    if (MainActivity.media.isPlaying()) {     // If MainActivity.media IS currently playing...
//                        MainActivity.media.pause();
//                        playButton.setImageResource(R.drawable.baseline_play_arrow_white_24);
//                    } else {
//                        if (MainActivity.media != null) {
//                            MainActivity.media.start();
//                            playButton.setImageResource(R.drawable.baseline_pause_white_24);
//                        }
//                    }
//                }
//            }
//        });
//        nextButton.setOnClickListener(v -> {
//            if (MainActivity.media.isPlaying()) {
//                MainActivity.media.stop();
//                MainActivity.media.release();
//                playMediaFromPosition(changeTrackTo(MainActivity.currentPosition, 1));
//                playButton.setImageResource(R.drawable.baseline_pause_white_24);
//            } else {
//                MainActivity.media.stop();
//                MainActivity.media.release();
//                MainActivity.media = MediaPlayer.create(SelectedSongActivity.this, changeTrackTo(1));
//                playButton.setImageResource(R.drawable.baseline_play_arrow_white_24);
//            }
//        });
//        previousButton.setOnClickListener(v -> {
//            int currentDurationPosition = MainActivity.media.getCurrentPosition();
//            if (MainActivity.media != null) {
//                if (MainActivity.media.isPlaying()) {
//                    MainActivity.media.pause();
//                    playButton.setImageResource(R.drawable.baseline_play_arrow_white_24);
//                    if (currentDurationPosition < 3000) {       // change track
//                        MainActivity.media.stop();
//                        MainActivity.media.release();
//                        MainActivity.media = MediaPlayer.create(SelectedSongActivity.this, changeTrackTo(-1));
//                    } else {        // restart track
//                        MainActivity.media.seekTo(0);
//                        MainActivity.media.start();
//                        playButton.setImageResource(R.drawable.baseline_pause_white_24);
//                    }
//                    MainActivity.media.start();
//                    playButton.setImageResource(R.drawable.baseline_pause_white_24);
//                } else {
//                    if (currentDurationPosition < 3000) {       // change track
//                        MainActivity.media.stop();
//                        MainActivity.media.release();
//                        MainActivity.media = MediaPlayer.create(SelectedSongActivity.this, changeTrackTo(-1));
//                    } else {        // restart track
//                        MainActivity.media.seekTo(0);
//                    }
//                }
//            }
//        });
//        initializeSeekbar();
//        playCycle();
//    }
//    public boolean isFocusGranted() {
//        int result = MainActivity.audioManager.requestAudioFocus(MainActivity.mFocusChangeListener, MainActivity.audioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
//        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//            return true;
//        }
//        return false;
//    }
//    public void playMediaFromPosition(int pos) {
//        MainActivity.media = MediaPlayer.create(MainActivity.activityContext, getMediaAtPosition(pos));
//        MainActivity.media.start();
//    }
//    public int getMediaAtPosition(int pos) {
//        MainActivity.currentPosition = pos;
//        setCurrentSong(pos);
//        int resID = getResources().getIdentifier(MainActivity.songStringList.get(pos), "raw", getPackageName());
//        return resID;
//    }
//    public int changeTrackTo(int value) {
//        if (MainActivity.media != null) {
//            int nextPos = MainActivity.currentPosition + value;
//            if (nextPos < 0) {     // Out of bounds: reverting to bottom of list
//                MainActivity.currentPosition = MainActivity.songList.size() - 1;
//            }
//            else if (nextPos >= 0 && nextPos < MainActivity.songList.size()){
//                MainActivity.currentPosition = nextPos;
//            }
//            else if (nextPos >= MainActivity.songList.size()) {     // Out of bounds: reverting back to top of list
//                MainActivity.currentPosition = 0;
//            }
//        } else {
//            MainActivity.currentPosition = 0;
//        }
//        MainActivity.currentSong = MainActivity.songList.get(MainActivity.currentPosition);
//        currentSongTitle.setText(MainActivity.currentSong.getSongTitle());
//        currentArtist.setText(MainActivity.currentSong.getArtistName());
//        currentAlbum.setText(MainActivity.currentSong.getAlbumTitle());
//        currentAlbumArt.setImageBitmap(MainActivity.currentSong.getBitmap());
//        int resID = getResources().getIdentifier(MainActivity.songStringList.get(MainActivity.currentPosition), "raw", getPackageName());
//        return resID;
//    }
//    public int changeTrackTo(int sv, int increment) {
//        int startingValue = sv;
//        if (MainActivity.media != null) {
//            int nextPos = startingValue + increment;
//            if (nextPos < 0) {     // Out of bounds: reverting to bottom of list
//                MainActivity.currentPosition = MainActivity.songList.size() - 1;
//                startingValue = startingValue - 1;
//            }
//            else if (nextPos >= 0 && nextPos < MainActivity.songList.size()){
//                MainActivity.currentPosition = nextPos;
//                startingValue = nextPos;
//            }
//            else if (nextPos >= MainActivity.songList.size()) {     // Out of bounds: reverting back to top of list
//                MainActivity.currentPosition = 0;
//                startingValue = 0;
//            }
//        } else {
//            MainActivity.currentPosition = 0;
//            startingValue = 0;
//        }
//        MainActivity.currentSong = MainActivity.songList.get(MainActivity.currentPosition);
//        return startingValue;
//    }
//    public void setCurrentSong(int pos) {
//        MainActivity.currentSong = MainActivity.songList.get(pos);
//    }
//    public String getMediaTime(int time) {
//        int min = (time / 1000) / 60;
//        int sec = (time / 1000) % 60;
//        NumberFormat formatter = new DecimalFormat("00");
//        String minutes = "";
//        if (min < 10) {
//            minutes = String.format("%02d", min);
//        } else {
//            minutes = String.format("%01d", min);
//        }
//        String seconds = formatter.format(sec);
//        return minutes + ":" + seconds;
//    }
//    private void initializeSeekbar() {
//        totalDurationText.setText(getMediaTime(MainActivity.media.getDuration()));
//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            int currentProgress;
//            boolean mediaWasPlaying = false;
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean inputFromUser) {
//                seekBar.setProgress(progress);
//                currentProgress = progress;
//                if (inputFromUser) {
//                    currentDurationText.setText(getMediaTime(progress));
//                }
//                currentDurationText.setText(getMediaTime(progress));
//            }
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                if (MainActivity.media.isPlaying()) {
//                    MainActivity.media.pause();
//                    mediaWasPlaying = true;
//                }
//                currentDurationText.setText(getMediaTime(currentProgress));
//            }
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                currentDurationText.setText(getMediaTime(currentProgress));
//                if (mediaWasPlaying) {
//                    MainActivity.media.seekTo(currentProgress);
//                    MainActivity.media.start();
//                    mediaWasPlaying = false;
//                }
//            }
//        });
//    }
//    public String getSelectedTrackText(SongObject song) {
//        String songTitle = song.getSongTitle();
//        String artistName = song.getArtistName();
//        return songTitle + " - " + artistName;
//    }
//    private void playCycle() {          // Retrieved from: https://www.youtube.com/watch?v=HB3DoZh1QWU
//        if (MainActivity.media != null) {
//            seekBar.setProgress(MainActivity.media.getCurrentPosition());
//            seekBar.setMax(MainActivity.media.getDuration());
//            totalDurationText.setText(getMediaTime(MainActivity.media.getDuration()));
//            currentSongTitle.setText(MainActivity.currentSong.getSongTitle());
//            currentArtist.setText(MainActivity.currentSong.getArtistName());
//            currentAlbum.setText(MainActivity.currentSong.getAlbumTitle());
//            currentAlbumArt.setImageBitmap(MainActivity.currentSong.getBitmap());
//            MainActivity.selectedTrack.setText(getSelectedTrackText(MainActivity.currentSong));
//            MainActivity.runnable = new Runnable() {
//                @Override
//                public void run() {
//                    playCycle();
//                }
//            };
//            MainActivity.handler.postDelayed(MainActivity.runnable, 100);
//        }
//    }
//    @Override
//    protected void onStop () {
//        super.onStop();
//    }
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//    }
}