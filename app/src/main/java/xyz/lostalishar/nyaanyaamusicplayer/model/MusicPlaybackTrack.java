package xyz.lostalishar.nyaanyaamusicplayer.model;

/**
 * Class to store data about the tracks in the playback queue
 */

public class MusicPlaybackTrack {
    private long id;

    public MusicPlaybackTrack(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
