package xyz.lostalishar.nyaanyaamusicplayer.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;

public class MusicDatabaseProvider extends ContentProvider {
    private static final String TAG = MusicDatabaseProvider.class.getSimpleName();

    private static final String DATABASE_NAME = "music_database.db";
    private static final int DATABASE_VERSION = 1;

    private static final int QUEUE = 1;

    private static final String URI_AUTHORITY = "xyz.lostalishar.nyaanyaamusicplayer.provider";
    private static final String BASE_PATH = "music";
    public static final Uri EXTERNAL_CONTENT_URI = Uri.parse("content://" + URI_AUTHORITY+
            "/" + BASE_PATH);

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(URI_AUTHORITY, BASE_PATH, QUEUE);
    }

    private PlaybackQueueSQLHelper queueSQLHelper;

    public MusicDatabaseProvider() {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
    }


    // ========================================================================
    // ContentProvider overrides
    // ========================================================================

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        if (BuildConfig.DEBUG) Log.d(TAG, "delete");

        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(@NonNull Uri uri) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getType");

        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        if (BuildConfig.DEBUG) Log.d(TAG, "insert");

        SQLiteDatabase db = queueSQLHelper.getWritableDatabase();
        long id = 0;

        switch (uriMatcher.match(uri)) {
            case QUEUE:
                id = db.insert(PlaybackQueueSQLHelper.PlaybackQueueColumns.NAME, null, values);
                break;
            default:
                if (BuildConfig.DEBUG) Log.d(TAG, "Unsupported URI: " + uri);
                return null;
        }

        // notify people listening to me
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public boolean onCreate() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");

        queueSQLHelper = new PlaybackQueueSQLHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        return false;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        if (BuildConfig.DEBUG) Log.d(TAG, "query");

        switch (uriMatcher.match(uri)) {
            case QUEUE:
                break;
            default:
                if (BuildConfig.DEBUG) Log.d(TAG, "Unsupported URI: " + uri);
                return null;
        }

        SQLiteDatabase db = queueSQLHelper.getReadableDatabase();
        Cursor cursor = db.query(PlaybackQueueSQLHelper.PlaybackQueueColumns.NAME, projection,
                selection, selectionArgs, null, null, sortOrder);

        // notify people listening to me
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if (BuildConfig.DEBUG) Log.d(TAG, "update");

        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
