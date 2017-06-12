package xyz.velvetmilk.nyaanyaamusicplayer.model;

import android.text.TextUtils;

/**
 * Created by nydrani on 27/05/17.
 */

public class Music {
    private long id;
    private String name;
    private String artistName;
    private String albumName;
    private int duration;
    private String mimeType;

    public Music(long id, String name, String artistName,
                 String albumName, int duration, String mimeType) {
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

    public int getDuration() {
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

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public boolean equals(Object obj) {
        final Music other = (Music) obj;

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (id != other.getId()) {
            return false;
        }
        if (!TextUtils.equals(name, other.getName())) {
            return false;
        }
        if (!TextUtils.equals(artistName, other.getArtistName())) {
            return false;
        }
        if (!TextUtils.equals(albumName, other.getAlbumName())) {
            return false;
        }
        if (duration != other.getDuration()) {
            return false;
        }
        if (!TextUtils.equals(mimeType, other.getMimeType())) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}
