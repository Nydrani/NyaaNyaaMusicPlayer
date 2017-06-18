package xyz.lostalishar.nyaanyaamusicplayer.model;

import java.util.Objects;

/**
 * Created by nydrani on 27/05/17.
 * Class for holding music information
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
