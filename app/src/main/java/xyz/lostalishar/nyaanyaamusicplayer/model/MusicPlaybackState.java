package xyz.lostalishar.nyaanyaamusicplayer.model;

/**
 * State containing data about the current playstate
 * @TODO currently using SharedPreferences to store state about pos + id
 * @TODO change to use just sql database later when playlists are added
 */

public class MusicPlaybackState {
    private int queuePos;
    private int seekPos;

    public MusicPlaybackState() {
    }

    public MusicPlaybackState(int queuePos, int seekPos) {
        this.queuePos = queuePos;
        this.seekPos = seekPos;
    }


    public int getQueuePos() {
        return queuePos;
    }

    public int getSeekPos() {
        return seekPos;
    }

    public void setQueuePos(int queuePos) {
        this.queuePos = queuePos;
    }

    public void setSeekPos(int seekPos) {
        this.seekPos = seekPos;
    }

    @Override
    public String toString() {
        return "QueuePos: " + String.valueOf(queuePos) + " | seekPos: " + String.valueOf(seekPos);
    }
}
