package xyz.lostalishar.nyaanyaamusicplayer.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackState;
import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackTrack;
import xyz.lostalishar.nyaanyaamusicplayer.service.INyaaNyaaMusicService;
import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;

/**
 * Utilities for interacting with the music service
 */

public class MusicUtils {
    private static final String TAG = MusicUtils.class.getSimpleName();
    private static INyaaNyaaMusicService musicService;
    private static MusicServiceConnection musicServiceConnection = new MusicServiceConnection();

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
        // only bind to services that are already running
        return context.bindService(intent, musicServiceConnection, 0);
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


    public static boolean load(int queuePos) {
        if (BuildConfig.DEBUG) Log.d(TAG, "load");

        if (musicService == null) {
            return false;
        }

        try {
            musicService.reset();
            // @TODO debugging
            boolean loaded =  musicService.load(queuePos);
            Log.v(TAG, "ABLE TO LOAD: " + String.valueOf(loaded));
            return loaded;
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Music service reference lost");
        }

        return false;
    }

    public static void play(long songId) {
        if (BuildConfig.DEBUG) Log.d(TAG, "play");

        if (musicService == null) {
            return;
        }

        try {
            musicService.reset();
            int pos = musicService.enqueue(new long[] { songId }, null);
            if (musicService.load(pos)) {
                musicService.play();
            }
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Music service reference lost");
        }
    }

    public static void start() {
        if (BuildConfig.DEBUG) Log.d(TAG, "start");

        if (musicService == null) {
            return;
        }

        try {
            musicService.reset();
            if (musicService.load(musicService.getState().getQueuePos())) {
                musicService.play();
            } else if (musicService.load(0)) {
                musicService.play();
            }
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Music service reference lost");
        }
    }

    public static void resume() {
        if (BuildConfig.DEBUG) Log.d(TAG, "resume");

        if (musicService == null) {
            return;
        }

        try {
            musicService.play();
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Music service reference lost");
        }
    }

    public static void pause() {
        if (BuildConfig.DEBUG) Log.d(TAG, "pause");

        if (musicService == null) {
            return;
        }

        try {
            musicService.pause();
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Music service reference lost");
        }
    }

    public static void previous() {
        if (BuildConfig.DEBUG) Log.d(TAG, "previous");

        if (musicService == null) {
            return;
        }

        try {
            musicService.previous();
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Music service reference lost");
        }
    }

    public static void next() {
        if (BuildConfig.DEBUG) Log.d(TAG, "next");

        if (musicService == null) {
            return;
        }

        try {
            musicService.next();
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

    public static List<MusicPlaybackTrack> getQueue() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getQueue");

        if (musicService == null) {
            return new ArrayList<>();
        }

        try {
            // @TODO debugging
            List<MusicPlaybackTrack> queue = musicService.getQueue();
            if (BuildConfig.DEBUG) Log.v(TAG, queue.toString());
            return queue;
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Music service reference lost");
        }

        return new ArrayList<>();
    }

    public static MusicPlaybackTrack getCurrentPlaying() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getCurrentPlaying");

        if (musicService == null) {
            return null;
        }

        try {
            // @TODO debugging
            MusicPlaybackTrack track = musicService.getCurrentPlaying();
            if (BuildConfig.DEBUG  && track != null) {
                Log.v(TAG, track.toString());
            }
            return track;
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Music service reference lost");
        }

        return null;
    }

    public static int enqueue(long[] musicIdList, int[] addedList) {
        if (BuildConfig.DEBUG) Log.d(TAG, "enqueue");

        if (musicService == null) {
            return MusicPlaybackService.UNKNOWN_POS;
        }

        try {
            return musicService.enqueue(musicIdList, addedList);
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Music service reference lost");
        }

        return MusicPlaybackService.UNKNOWN_POS;
    }

    public static int dequeue(long[] musicIdList, long[] removedList) {
        if (BuildConfig.DEBUG) Log.d(TAG, "dequeue");

        if (musicService == null) {
            return MusicPlaybackService.UNKNOWN_POS;
        }

        try {
            return musicService.dequeue(musicIdList, removedList);
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Music service reference lost");
        }

        return MusicPlaybackService.UNKNOWN_POS;
    }

    public static MusicPlaybackState getState() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getState");

        if (musicService == null) {
            return null;
        }

        try {
            // @TODO debugging
            MusicPlaybackState state = musicService.getState();
            if (BuildConfig.DEBUG && state != null) {
                Log.v(TAG, state.toString());
            }
            return state;
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Music service reference lost");
        }

        return null;
    }

    public static boolean isPlaying() {
        if (BuildConfig.DEBUG) Log.d(TAG, "isPlaying");

        if (musicService == null) {
            return false;
        }

        try {
            return musicService.isPlaying();
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Music service reference lost");
        }

        return false;
    }


    //=========================================================================
    // ServiceConnection implementation
    //=========================================================================

    /** Defines callbacks for service binding, passed to bindService() */
    private static class MusicServiceConnection implements ServiceConnection {
        private static final String TAG = MusicServiceConnection.class.getSimpleName();

        private MusicServiceConnection() {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
        }


        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (BuildConfig.DEBUG) Log.d(TAG, "onServiceConnected");

            musicService = INyaaNyaaMusicService.Stub.asInterface(service);

            MusicPlaybackService.NyaaNyaaMusicServiceStub binder = (MusicPlaybackService.NyaaNyaaMusicServiceStub)service;
            NyaaUtils.notifyChange(binder.getService(), NyaaUtils.SERVICE_READY);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (BuildConfig.DEBUG) Log.d(TAG, "onServiceDisconnected");

            musicService = null;
        }
    }
}
