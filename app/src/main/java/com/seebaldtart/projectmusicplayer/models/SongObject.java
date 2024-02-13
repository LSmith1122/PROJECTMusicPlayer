package com.seebaldtart.projectmusicplayer.models;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import java.lang.reflect.Field;
public class SongObject {
    private Field mField;
    private String mSongTitle;
    private String mArtistName;
    private String mAlbumTitle;
    private int resID;
    Bitmap bitmap;
    private boolean hasImage = false;
    public SongObject(Field field) {
        mField = field;
        compileData();
    }
    private void compileData() {
//        resID = MainActivity.activityContext.getResources().getIdentifier(mField.getName(), "raw", MainActivity.activityContext.getPackageName());
//        MediaMetadataRetriever metaRet = new MediaMetadataRetriever();
//        Uri uri = Uri.parse("android.resource://" + MainActivity.activityContext.getPackageName() + "/raw/" + resID);       // Information retrieved from https://stackoverflow.com/questions/7977348/how-to-get-uri-of-res-folder
//        metaRet.setDataSource(MainActivity.activityContext, uri);
//        mArtistName = metaRet.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
//        mSongTitle = metaRet.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
//        mAlbumTitle = metaRet.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
//        byte [] data = metaRet.getEmbeddedPicture();
//        if(data != null) {
//            hasImage = true;
//            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//        }
//        else {
//            hasImage = false;
//        }
    }
    public String getSongTitle() {
        return mSongTitle;
    }
    public String getArtistName() {
        return mArtistName;
    }
    public String getAlbumTitle() {
        return mAlbumTitle;
    }
    public Bitmap getBitmap() {
        return bitmap;
    }
    public boolean hasImage() {
        return hasImage;
    }
}