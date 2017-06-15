package xyz.velvetmilk.nyaanyaamusicplayer.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import xyz.velvetmilk.nyaanyaamusicplayer.BuildConfig;
import xyz.velvetmilk.nyaanyaamusicplayer.service.INyaaNyaaMusicService;
import xyz.velvetmilk.nyaanyaamusicplayer.service.MusicPlaybackService;

/**
 * Created by nydrani on 12/06/17.
 * Music service utilities
 */

public class MusicUtils {
    private static final String TAG = MusicUtils.class.getSimpleName();
    private static INyaaNyaaMusicService musicService;

    public MusicUtils() {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
    }


    public static boolean bindToService(Context context) {
        Intent intent = new Intent(context, MusicPlaybackService.class);
        return context.bindService(intent, musicServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public static void unbindFromService(Context context) {
        context.unbindService(musicServiceConnection);
    }

    public static void play(long songId) {
        if (BuildConfig.DEBUG) Log.d(TAG, "play");

        if (musicService == null) {
            return;
        }

        try {
            musicService.load(songId);
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Music service reference lost");
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
            if (BuildConfig.DEBUG) Log.d(TAG, "Music service reference lost");
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
