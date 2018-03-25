package xyz.lostalishar.nyaanyaamusicplayer.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;

/**
 * Job service for media playback service startup
 */

public class MediaJobService extends JobService {
    private static final String TAG = MediaJobService.class.getSimpleName();


    @Override
    public boolean onStartJob(JobParameters params) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStartJob");

        Intent musicPlaybackService = new Intent(this, MusicPlaybackService.class);

        // transfer extra into the intent
        int keyCode = params.getExtras().getInt("KEYCODE");
        musicPlaybackService.putExtra("KEYCODE", keyCode);

        // start service and finish the job
        startService(musicPlaybackService);
        jobFinished(params, false);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStopJob");

        // forcefully kill service if somehow the jobFinished had not been called
        MusicUtils.stopService(this);

        return true;
    }
}
