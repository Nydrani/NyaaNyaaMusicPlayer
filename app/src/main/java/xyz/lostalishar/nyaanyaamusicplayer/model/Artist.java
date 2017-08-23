package xyz.lostalishar.nyaanyaamusicplayer.model;

import java.util.Objects;

/**
 * Class for holding artist information
 */

public class Artist {
    private long id;
    private String name;
    private int numTracks;
    private int numAlbums;

    public Artist(long id, String name, int numTracks, int numAlbums) {
        this.id = id;
        this.name = name;
        this.numTracks = numTracks;
        this.numAlbums = numAlbums;
    }


    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getNumTracks() {
        return numTracks;
    }

    public int getNumAlbums() {
        return numAlbums;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumTracks(int numTracks) {
        this.numTracks = numTracks;
    }

    public void setNumAlbums(int numAlbums) {
        this.numAlbums = numAlbums;
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

        final Artist other = (Artist)obj;

        if (id != other.id) {
            return false;
        }
        if (!Objects.equals(name, other.name)) {
            return false;
        }
        if (numAlbums != other.numAlbums) {
            return false;
        }
        if (numTracks != other.numTracks) {
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
