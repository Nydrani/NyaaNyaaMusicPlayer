package xyz.lostalishar.nyaanyaamusicplayer.util;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.service.MediaJobService;
import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;

/**
 * Utilities for scheduling jobs
 */

public class JobUtils {
    private static final String TAG = JobUtils.class.getSimpleName();

    public JobUtils() {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
    }


    public static void scheduleMediaJob(Context context, int keyCode) {
        if (BuildConfig.DEBUG) Log.d(TAG, "scheduleMediaJob");

        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        if (jobScheduler == null) {
            return;
        }

        ComponentName mediaJobServiceComponent = new ComponentName(context, MediaJobService.class);
        PersistableBundle infoBundle = new PersistableBundle();
        infoBundle.putInt(MusicPlaybackService.ACTION_EXTRA_KEYCODE, keyCode);
        JobInfo jobInfo = new JobInfo.Builder(0, mediaJobServiceComponent)
                .setMinimumLatency(0)
                .setOverrideDeadline(5000)
                .setExtras(infoBundle)
                .build();

        jobScheduler.schedule(jobInfo);
    }
}
