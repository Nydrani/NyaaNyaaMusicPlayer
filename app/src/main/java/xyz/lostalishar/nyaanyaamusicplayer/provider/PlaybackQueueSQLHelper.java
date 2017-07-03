package xyz.lostalishar.nyaanyaamusicplayer.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;

/**
 * SQLite database for saving playback queue
 */

public class PlaybackQueueSQLHelper extends SQLiteOpenHelper {
    private static final String TAG = PlaybackQueueSQLHelper.class.getSimpleName();

    public PlaybackQueueSQLHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                                  int version) {
        super(context, name, factory, version);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
    }


    // ========================================================================
    // SQLiteOpenHelper callback overrides
    // ========================================================================

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");

        String playbackQueueSQL = "CREATE TABLE IF NOT EXISTS " + PlaybackQueueColumns.NAME +
                "(" + PlaybackQueueColumns.ID + " LONG NOT NULL, " + PlaybackQueueColumns.POSITION +
                " INTEGER NOT NULL);";

        db.execSQL(playbackQueueSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onUpgrade");

        db.execSQL("DROP TABLE IF EXISTS " + PlaybackQueueColumns.NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onDowngrade");

        db.execSQL("DROP TABLE IF EXISTS " + PlaybackQueueColumns.NAME);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onOpen");
    }


    // ========================================================================
    // PlaybackQueue table column structure
    // ========================================================================

    public class PlaybackQueueColumns {
        public static final String NAME = "playbackqueue";
        public static final String ID = "id";
        public static final String POSITION = "position";
    }
}
