package xyz.lostalishar.nyaanyaamusicplayer.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.activity.AlbumListActivity;
import xyz.lostalishar.nyaanyaamusicplayer.activity.ArtistListActivity;
import xyz.lostalishar.nyaanyaamusicplayer.activity.HomeActivity;
import xyz.lostalishar.nyaanyaamusicplayer.activity.QueueActivity;
import xyz.lostalishar.nyaanyaamusicplayer.activity.SettingsActivity;

/**
 * Utilities for general use
 */

public class NyaaUtils {
    private static final String TAG = NyaaUtils.class.getSimpleName();
    public static final int PERMISSION_REQUEST_CODE = 0;

    public static final String PACKAGE_NAME = "xyz.lostalishar.nyaanyaamusicplayer";
    public static final String QUEUE_CHANGED = PACKAGE_NAME + ".queuechanged";
    public static final String META_CHANGED = PACKAGE_NAME + ".metachanged";
    public static final String SERVICE_READY = PACKAGE_NAME + ".serviceready";
    public static final String SERVICE_EXIT = PACKAGE_NAME + ".serviceexit";

    public NyaaUtils() {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
    }


    //=========================================================================
    // Exposed functions
    //=========================================================================

    /**
     * Given an activity, it requests for missing permissions
     */
    public static void requestMissingPermissions(Activity activity) {
        if (BuildConfig.DEBUG) Log.d(TAG, "requestMissingPermissions");

        // early exit for when android version < android M
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        List<String> missingPermissionsList = getMissingPermissions(activity);

        // convert to string array
        String[] missingPermissions = new String[missingPermissionsList.size()];
        missingPermissions = missingPermissionsList.toArray(missingPermissions);

        if (missingPermissions.length > 0) {
            activity.requestPermissions(missingPermissions, PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Given the context, it returns whether this list is empty or not
     * Function for clarity of logic
     */
    public static boolean needsPermissions(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "needsPermissions");

        return getMissingPermissions(context).size() > 0;
    }

    /**
     * Given a context and what, it will send a broadcast intent to listeners.
     * Usually used to notify events
     */
    public static void notifyChange(Context context, String what) {
        if (BuildConfig.DEBUG) Log.d(TAG, "notifyChange");
        if (BuildConfig.DEBUG) Log.d(TAG, "notifyChange: " + what);

        Intent intent = new Intent(what);
        context.sendBroadcast(intent);
    }

    /**
     * Start up the AlbumListActivity
     */
    public static void openAlbumList(Activity activity, long albumId, String albumName) {
        if (BuildConfig.DEBUG) Log.d(TAG, "openAlbumList");

        Intent intent = new Intent(activity, AlbumListActivity.class);
        intent.putExtra("albumId", albumId);
        intent.putExtra("albumName", albumName);
        activity.startActivity(intent);
    }

    /**
     * Start up the ArtistListActivity
     */
    public static void openArtistList(Activity activity, long artistId, String artistName) {
        if (BuildConfig.DEBUG) Log.d(TAG, "openArtistList");

        Intent intent = new Intent(activity, ArtistListActivity.class);
        intent.putExtra("artistId", artistId);
        intent.putExtra("artistName", artistName);
        activity.startActivity(intent);
    }

    /**
     * Start up the QueueActivity
     */
    public static void openQueue(Activity activity) {
        if (BuildConfig.DEBUG) Log.d(TAG, "openQueue");

        Intent intent = new Intent(activity, QueueActivity.class);
        activity.startActivity(intent);
    }

    /**
     * Start up the SettingsActivity
     */
    public static void openSettings(Activity activity) {
        if (BuildConfig.DEBUG) Log.d(TAG, "openArtistList");

        Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivity(intent);
    }

    /**
     * Restart the entire application
     */
    public static void triggerRebirth(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        if (context instanceof Activity) {
            ((Activity) context).finishAffinity();
        }

        // Kill application
        Runtime.getRuntime().exit(0);
    }


    //=========================================================================
    // Internal helper functions
    //=========================================================================

    /**
     * Returns a list of all the required permissions for the app
     */
    private static List<String> getRequiredPermissions() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getRequiredPermissions");

        List<String> permissionList = new ArrayList<>();
        permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        return permissionList;
    }

    /**
     * Checks if app has permissions and return list of missing permissions
     */
    private static List<String> getMissingPermissions(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getMissingPermissions");

        List<String> missingPermissions = new ArrayList<>();

        // early exit for when android version < android M
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return missingPermissions;
        }

        // doing this because im not sure if the for loop is internally optimised
        List<String> requiredPermissions = getRequiredPermissions();

        // add permissions that are missing
        for (String permission : requiredPermissions) {
            if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                missingPermissions.add(permission);
            }
        }

        return missingPermissions;
    }
}
