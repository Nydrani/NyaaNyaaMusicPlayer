package xyz.velvetmilk.nyaanyaamusicplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import xyz.velvetmilk.nyaanyaamusicplayer.BuildConfig;
import xyz.velvetmilk.nyaanyaamusicplayer.util.JobUtils;

public class MediaButtonIntentReceiver extends BroadcastReceiver {
    private static final String TAG = MediaButtonIntentReceiver.class.getSimpleName();


    @Override
    public void onReceive(Context context, Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onReceive");

        final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

        // return if for some reason there is no EXTRA_KEY_EVENT
        // from android.intent.action.MEDIA_BUTTON
        if (event == null) {
            return;
        }

        JobUtils.scheduleMediaJob(context, event.getKeyCode());
    }
}
