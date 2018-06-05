package com.seebaldtart.projectmusicplayer;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class AudioAdapter extends ArrayAdapter<SongObject> {
    public AudioAdapter(Activity context, ArrayList<SongObject> songList) {
        super(context, 0, songList);
    }

    ImageView image;
    TextView songTitle;
    TextView songInfoText;

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_list_item, parent, false);
        }

        image = convertView.findViewById(R.id.album_artwork);
        songInfoText = convertView.findViewById(R.id.artist_and_album_info);
        songTitle = convertView.findViewById(R.id.song_title);

        SongObject currentSong = MainActivity.songList.get(position);

        songTitle.setText(currentSong.getSongTitle());
        songInfoText.setText(currentSong.getArtistName() + " - " + currentSong.getAlbumTitle());

        if (currentSong.hasImage()) {
            image.setImageBitmap(currentSong.bitmap);
//            image.setAdjustViewBounds(false);
//            image.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
        } else {
            image.setImageResource(R.drawable.default_icon_song_album1);
        }

        return convertView;
    }
}
