package xyz.lostalishar.nyaanyaamusicplayer.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.service.INyaaNyaaMusicService;
import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;

/**
 * Utilities for interacting with the music service
 */

public class MusicUtils {
    private static final String TAG = MusicUtils.class.getSimpleName();
    private static INyaaNyaaMusicService musicService;

    public MusicUtils() {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
    }


    public static ComponentName startService(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "startService");

        Intent intent = new Intent(context, MusicPlaybackService.class);
        return context.startService(intent);
    }

    public static boolean bindToService(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "bindToService");

        Intent intent = new Intent(context, MusicPlaybackService.class);
        return context.bindService(intent, musicServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public static void unbindFromService(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "unbindFromService");

        context.unbindService(musicServiceConnection);
    }

    public static boolean stopService(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "stopService");

        Intent intent = new Intent(context, MusicPlaybackService.class);
        intent.setAction(MusicPlaybackService.ACTION_SHUTDOWN);
        return context.stopService(intent);
    }


    public static void play(long songId) {
        if (BuildConfig.DEBUG) Log.d(TAG, "play");

        if (musicService == null) {
            return;
        }

        try {
            musicService.reset();
            if (musicService.load(songId)) {
                musicService.start();
            }
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Music service reference lost");
        }
    }

    public static void stop() {
        if (BuildConfig.DEBUG) Log.d(TAG, "stop");

        if (musicService == null) {
            return;
        }

        try {
            musicService.stop();
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Music service reference lost");
        }
    }


    //=========================================================================
    // ServiceConnection implementation
    //=========================================================================

    /** Defines callbacks for service binding, passed to bindService() */
    private static ServiceConnection musicServiceConnection = new ServiceConnection() {
        private final String TAG = ServiceConnection.class.getSimpleName();


        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (BuildConfig.DEBUG) Log.d(TAG, "onServiceConnected");

            musicService = INyaaNyaaMusicService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (BuildConfig.DEBUG) Log.d(TAG, "onServiceDisconnected");

            musicService = null;
        }
    };
}
