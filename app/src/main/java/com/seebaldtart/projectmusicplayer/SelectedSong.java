package com.seebaldtart.projectmusicplayer;

import android.content.Intent;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class SelectedSong extends AppCompatActivity {
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
    SeekBar seekBar;
    MediaPlayer media = MainActivity.media;
    int currentPosition;
    SongObject currentSong;
    ArrayList<SongObject> songList;
    ArrayList<String> songStringList;

    Handler handler = MainActivity.handler;
    Runnable runnable = MainActivity.runnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selected_song_activity);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.i("TEST", "onCreate - SelectedSong");

        currentSongTitle = findViewById(R.id.song_title);
        currentArtist = findViewById(R.id.artist_name);
        currentAlbum = findViewById(R.id.album_title);
        currentDurationText = findViewById(R.id.current_duration);
        totalDurationText = findViewById(R.id.total_duration);
        currentAlbumArt = findViewById(R.id.album_artwork);
        playButton = findViewById(R.id.play);
        nextButton = findViewById(R.id.next);
        previousButton = findViewById(R.id.previous);
        upButton = findViewById(R.id.upButton);
        seekBar = findViewById(R.id.seekbar);
        currentPosition = MainActivity.currentPosition;
        currentSong = MainActivity.currentSong;
        songList = MainActivity.songList;
        songStringList = MainActivity.songStringList;

        // TODO: FINISH... and use MainActivity to use a SongObject to get info to display

        if (MainActivity.currentSong != null) {
            currentSongTitle.setText(MainActivity.currentSong.getSongTitle());
            currentArtist.setText(MainActivity.currentSong.getArtistName());
            currentAlbum.setText(MainActivity.currentSong.getAlbumTitle());
            currentAlbumArt.setImageBitmap(MainActivity.currentSong.getBitmap());

            if (media.isPlaying()) {
                playButton.setImageResource(R.drawable.baseline_pause_white_24);
            } else {
                playButton.setImageResource(R.drawable.baseline_play_arrow_white_24);
            }
        }

        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectedSong.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);         // Retrieved from https://stackoverflow.com/questions/41344515/switching-between-activities-without-destroying-the-activity
                startActivity(intent);
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.focusGranted) {
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
                    media = MediaPlayer.create(SelectedSong.this, changeTrackTo(1));
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
                            media = MediaPlayer.create(SelectedSong.this, changeTrackTo(-1));
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
                            media = MediaPlayer.create(SelectedSong.this, changeTrackTo(-1));
                        } else {        // restart track
                            media.seekTo(0);
                        }
                    }
                }
            }
        });
        initializeSeekbar();
        playCycle();
    }

    public void playMediaFromPosition(int pos) {
        media = MediaPlayer.create(MainActivity.activityContext, getMediaAtPosition(pos));
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
        currentSongTitle.setText(MainActivity.currentSong.getSongTitle());
        currentArtist.setText(MainActivity.currentSong.getArtistName());
        currentAlbum.setText(MainActivity.currentSong.getAlbumTitle());
        currentAlbumArt.setImageBitmap(MainActivity.currentSong.getBitmap());
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
        currentSongTitle.setText(MainActivity.currentSong.getSongTitle());
        currentArtist.setText(MainActivity.currentSong.getArtistName());
        currentAlbum.setText(MainActivity.currentSong.getAlbumTitle());
        currentAlbumArt.setImageBitmap(MainActivity.currentSong.getBitmap());
        return startingValue;
    }

    public void setCurrentSong(int pos) {
        currentSong = songList.get(pos);
        currentSongTitle.setText(MainActivity.currentSong.getSongTitle());
        currentArtist.setText(MainActivity.currentSong.getArtistName());
        currentAlbum.setText(MainActivity.currentSong.getAlbumTitle());
        currentAlbumArt.setImageBitmap(MainActivity.currentSong.getBitmap());
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
        totalDurationText.setText(getMediaTime(media.getDuration()));
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

    private void playCycle() {
        if (media != null) {
            Log.i("TESTING", "Working...");
            seekBar.setProgress(MainActivity.media.getCurrentPosition());
            seekBar.setMax(media.getDuration());
            totalDurationText.setText(getMediaTime(media.getDuration()));
            runnable = new Runnable() {
                @Override
                public void run() {
                    playCycle();
                }
            };
            handler.postDelayed(runnable, 500);
        } else {
            Log.i("TESTING", "Media is Null...");
        }
    }

    @Override
    protected void onStop () {
        super.onStop();
        Log.i("TEST", "onStop - SelectedSong");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("TEST", "onDestroy - SelectedSong");
    }
}
