package xyz.velvetmilk.nyaanyaamusicplayer.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

import xyz.velvetmilk.nyaanyaamusicplayer.BuildConfig;

/**
 * Created by nydrani on 14/06/17.
 */

public class MediaJobService extends JobService {
    private static final String TAG = MediaJobService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters params) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStartJob");

        int keyCode = params.getExtras().getInt("KEYCODE");

        Intent musicPlaybackService = new Intent(getApplicationContext(), MusicPlaybackService.class);
        musicPlaybackService.putExtra("KEYCODE", keyCode);

        getApplicationContext().startService(musicPlaybackService);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStopJob");

        return true;
    }
}
