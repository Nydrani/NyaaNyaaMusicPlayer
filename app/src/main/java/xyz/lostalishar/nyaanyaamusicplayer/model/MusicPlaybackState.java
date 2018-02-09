package xyz.lostalishar.nyaanyaamusicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;

/**
 * State containing data about the current playstate
 * @TODO currently using SharedPreferences to store state about pos + id
 * @TODO change to use just sql database later when playlists are added
 */

public class MusicPlaybackState implements Parcelable {
    private int queuePos;
    private int seekPos;

    public MusicPlaybackState() {
        queuePos = MusicPlaybackService.UNKNOWN_POS;
        seekPos = 0;
    }

    public MusicPlaybackState(Parcel in) {
        readFromParcel(in);
    }

    public MusicPlaybackState(int queuePos, int seekPos) {
        this.queuePos = queuePos;
        this.seekPos = seekPos;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(queuePos);
        out.writeInt(seekPos);
    }

    public void readFromParcel(Parcel in) {
        this.queuePos = in.readInt();
        this.seekPos = in.readInt();
    }

    public static final Parcelable.Creator<MusicPlaybackState> CREATOR
            = new Parcelable.Creator<MusicPlaybackState>() {
        @Override
        public MusicPlaybackState createFromParcel(Parcel in) {
            return new MusicPlaybackState(in);
        }

        @Override
        public MusicPlaybackState[] newArray(int size) {
            return new MusicPlaybackState[size];
        }
    };

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

        final MusicPlaybackState other = (MusicPlaybackState) obj;

        if (queuePos != other.queuePos) {
            return false;
        }
        if (seekPos != other.seekPos) {
            return false;
        }

        // return true at the end if every field is the same but not same object
        return true;
    }

    @Override
    public String toString() {
        return "QueuePos: " + String.valueOf(queuePos) + " | seekPos: " + String.valueOf(seekPos);
    }
}
