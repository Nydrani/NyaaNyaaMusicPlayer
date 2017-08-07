package xyz.lostalishar.nyaanyaamusicplayer.model;

import java.util.Objects;

/**
 * Class for holding album information
 */

public class Album {
    private long id;
    private String name;
    private String artistName;
    private int numSongs;

    public Album(long id, String name, String artistName, int numSongs) {
        this.id = id;
        this.name = name;
        this.artistName = artistName;
        this.numSongs = numSongs;
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

    public int getNumSongs() {
        return numSongs;
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

    public void setNumSongs(int numSongs) {
        this.numSongs = numSongs;
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

        final Album other = (Album)obj;

        if (id != other.id) {
            return false;
        }
        if (!Objects.equals(name, other.name)) {
            return false;
        }
        if (!Objects.equals(artistName, other.artistName)) {
            return false;
        }
        if (numSongs != other.numSongs) {
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
