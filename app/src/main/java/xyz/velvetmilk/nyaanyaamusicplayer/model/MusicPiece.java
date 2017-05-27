package xyz.velvetmilk.nyaanyaamusicplayer.model;

import android.text.TextUtils;

/**
 * Created by nydrani on 27/05/17.
 */

public class MusicPiece {
    private long pieceId;
    private String pieceName;
    private String artistName;
    private String albumName;
    private int duration;
    private String mimeType;

    public MusicPiece(final long pieceId, final String pieceName, final String artistName,
                      final String albumName, final int duration, final String mimeType) {
        this.pieceId = pieceId;
        this.pieceName = pieceName;
        this.artistName = artistName;
        this.albumName = albumName;
        this.duration = duration;
        this.mimeType = mimeType;
    }

    public long getPieceId() {
        return pieceId;
    }

    public String getPieceName() {
        return pieceName;
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

    public void setPieceId(final long pieceId) {
        this.pieceId = pieceId;
    }

    public void setPieceName(final String pieceName) {
        this.pieceName = pieceName;
    }

    public void setArtistName(final String artistName) {
        this.artistName = artistName;
    }

    public void setAlbumName(final String albumName) {
        this.albumName = albumName;
    }

    public void setDuration(final int duration) {
        this.duration = duration;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public boolean equals(final Object obj) {
        final MusicPiece other = (MusicPiece) obj;

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (pieceId != other.getPieceId()) {
            return false;
        }
        if (!TextUtils.equals(pieceName, other.getPieceName())) {
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
        return pieceName;
    }
}
