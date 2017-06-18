package xyz.lostalishar.nyaanyaamusicplayer.model;

/**
 * State containing data about the current playstate
 * @TODO currently using SharedPreferences to store state about pos + id
 * @TODO change to use just sql database later when playlists are added
 */

public class MusicPlaybackState {
    private long musicId;
    private int musicPos;

    public MusicPlaybackState() {
    }

    public MusicPlaybackState(long musicId, int musicPos) {
        this.musicId = musicId;
        this.musicPos = musicPos;
    }


    public long getMusicId() {
        return musicId;
    }

    public int getMusicPos() {
        return musicPos;
    }

    public void setMusicId(long musicId) {
        this.musicId = musicId;
    }

    public void setMusicPos(int musicPos) {
        this.musicPos = musicPos;
    }

    @Override
    public String toString() {
        return "Id: " + String.valueOf(musicId) + " | Pos: " + String.valueOf(musicPos);
    }
}
