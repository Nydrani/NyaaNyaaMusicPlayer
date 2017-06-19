package xyz.lostalishar.nyaanyaamusicplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.util.JobUtils;

public class MediaButtonIntentReceiver extends BroadcastReceiver {
    private static final String TAG = MediaButtonIntentReceiver.class.getSimpleName();


    @Override
    public void onReceive(Context context, Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onReceive");

        final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

        // only schedule if there is a EXTRA_KEY_EVENT extra as well as it being ACTION_DOWN
        // e.g. from android.intent.action.MEDIA_BUTTON
        if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
            JobUtils.scheduleMediaJob(context, event.getKeyCode());
        }
    }
}
