package xyz.lostalishar.nyaanyaamusicplayer.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;

public class MusicDatabaseProvider extends ContentProvider {
    private static final String TAG = MusicDatabaseProvider.class.getSimpleName();

    private static final String DATABASE_NAME = "music_database.db";
    private static final int DATABASE_VERSION = 1;

    private static final int QUEUE = 1;

    private static final String URI_AUTHORITY = "xyz.lostalishar.nyaanyaamusicplayer.provider";
    private static final String BASE_PATH = "music";

    public static final Uri EXTERNAL_CONTENT_URI = Uri.parse("content://" + URI_AUTHORITY +
            "/" + BASE_PATH);
    public static final Uri QUEUE_CONTENT_URI = Uri.parse("content://" + URI_AUTHORITY +
            "/" + PlaybackQueueSQLHelper.PlaybackQueueColumns.NAME);

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(URI_AUTHORITY, PlaybackQueueSQLHelper.PlaybackQueueColumns.NAME, QUEUE);
    }

    private PlaybackQueueSQLHelper queueSQLHelper;

    public MusicDatabaseProvider() {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
    }


    // ========================================================================
    // ContentProvider overrides
    // ========================================================================

    @Override
    public boolean onCreate() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");

        queueSQLHelper = new PlaybackQueueSQLHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        return false;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        if (BuildConfig.DEBUG) Log.d(TAG, "delete");

        SQLiteDatabase db = queueSQLHelper.getWritableDatabase();
        int id = 0;

        switch (uriMatcher.match(uri)) {
            case QUEUE:
                id = db.delete(PlaybackQueueSQLHelper.PlaybackQueueColumns.NAME, selection, selectionArgs);
                break;
            default:
                if (BuildConfig.DEBUG) Log.d(TAG, "Unsupported URI: " + uri);
        }

        // notify people listening to me
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return id;
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
        long id;

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

        return ContentUris.withAppendedId(QUEUE_CONTENT_URI, id);
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        if (BuildConfig.DEBUG) Log.d(TAG, "query");

        SQLiteDatabase db = queueSQLHelper.getReadableDatabase();
        Cursor cursor;

        switch (uriMatcher.match(uri)) {
            case QUEUE:
                cursor = db.query(PlaybackQueueSQLHelper.PlaybackQueueColumns.NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                if (BuildConfig.DEBUG) Log.d(TAG, "Unsupported URI: " + uri);
                return null;
        }

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

    @Override
    public
    @NonNull
    ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        if (BuildConfig.DEBUG) Log.d(TAG, "applyBatch");

        SQLiteDatabase db = queueSQLHelper.getWritableDatabase();

        int numOperations = operations.size();
        ContentProviderResult[] results = new ContentProviderResult[numOperations];

        db.beginTransaction();
        try {
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return results;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        if (BuildConfig.DEBUG) Log.d(TAG, "bulkInsert");

        SQLiteDatabase db = queueSQLHelper.getWritableDatabase();
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case QUEUE:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        db.insert(PlaybackQueueSQLHelper.PlaybackQueueColumns.NAME, null, value);
                    }
                    db.setTransactionSuccessful();
                    count = values.length;
                } finally {
                    db.endTransaction();
                }
                break;
            default:
                if (BuildConfig.DEBUG) Log.d(TAG, "Unsupported URI: " + uri);
                return count;
        }

        // notify people listening to me
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }
}
