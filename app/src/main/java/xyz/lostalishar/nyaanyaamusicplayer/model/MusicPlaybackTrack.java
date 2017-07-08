package xyz.lostalishar.nyaanyaamusicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class to store data about the tracks in the playback queue
 */

public class MusicPlaybackTrack implements Parcelable {
    private long id;

    public MusicPlaybackTrack(long id) {
        this.id = id;
    }

    public MusicPlaybackTrack(Parcel in) {
        readFromParcel(in);
    }


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
    }

    public void readFromParcel(Parcel in) {
        this.id = in.readLong();
    }

    public static final Parcelable.Creator<MusicPlaybackTrack> CREATOR
            = new Parcelable.Creator<MusicPlaybackTrack>() {
        @Override
        public MusicPlaybackTrack createFromParcel(Parcel in) {
            return new MusicPlaybackTrack(in);
        }

        @Override
        public MusicPlaybackTrack[] newArray(int size) {
            return new MusicPlaybackTrack[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
