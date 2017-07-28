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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
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

    @Override
    public String toString() {
        return "TrackId: " + String.valueOf(id);
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

        final MusicPlaybackTrack other = (MusicPlaybackTrack) obj;

        if (id != other.id) {
            return false;
        }

        return true;
    }
}
