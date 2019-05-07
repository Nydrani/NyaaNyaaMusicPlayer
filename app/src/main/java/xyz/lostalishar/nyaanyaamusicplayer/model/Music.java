package xyz.lostalishar.nyaanyaamusicplayer.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.Objects;

/**
 * Class for holding music information
 */

public class Music {
    private long id;
    private String name;
    private String artistName;
    private String albumName;
    private long duration;
    private String mimeType;

    public Music(long id, String name, String artistName,
                 String albumName, long duration, String mimeType) {
        this.id = id;
        this.name = name;
        this.artistName = artistName;
        this.albumName = albumName;
        this.duration = duration;
        this.mimeType = mimeType;
    }


    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public long getDuration() {
        return duration;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public static Music fromId(Context context, long id) {
        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[5];
        String selection = MediaStore.Audio.Media.IS_MUSIC + "=? AND " + MediaStore.Audio.Media._ID + "=?";
        String[] args = { "1", String.valueOf(id) };
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        projection[0] = MediaStore.Audio.Media.TITLE;
        projection[1] = MediaStore.Audio.Media.ARTIST;
        projection[2] = MediaStore.Audio.Media.ALBUM;
        projection[3] = MediaStore.Audio.Media.DURATION;
        projection[4] = MediaStore.Audio.Media.MIME_TYPE;

        Cursor cursor = musicResolver.query(musicUri, projection, selection, args, sortOrder);
        if (cursor == null) {
            return null;
        }

        if (!cursor.moveToFirst()) {
            return null;
        }

        int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
        int mimeTypeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE);

        final String name = cursor.getString(titleColumn);
        final String artistName = cursor.getString(artistColumn);
        final String albumName = cursor.getString(albumColumn);
        final long duration = cursor.getLong(durationColumn);
        final String mimeType = cursor.getString(mimeTypeColumn);

        final Music music = new Music(id, name, artistName, albumName, duration, mimeType);

        cursor.close();

        return music;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final Music other = (Music)obj;

        if (id != other.id) {
            return false;
        }
        if (!Objects.equals(name, other.name)) {
            return false;
        }
        if (!Objects.equals(artistName, other.artistName)) {
            return false;
        }
        if (!Objects.equals(albumName, other.albumName)) {
            return false;
        }
        if (duration != other.duration) {
            return false;
        }
        if (!Objects.equals(mimeType, other.mimeType)) {
            return false;
        }

        // return true at the end if every field is the same but not same object
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}
