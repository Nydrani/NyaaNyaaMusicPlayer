package xyz.lostalishar.nyaanyaamusicplayer;

import android.app.Application;
import android.preference.PreferenceManager;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.util.PreferenceUtils;

public class App extends Application {
    public static final String TAG = App.class.getSimpleName();


    //=========================================================================
    // Application lifecycle
    //=========================================================================

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate();

        // set default values only once on application start
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);

        // load fabric
        if (PreferenceUtils.loadUsageDataPref(this)) {
            // disable fabric for now
            // Fabric.with(this, new Crashlytics(), new Answers());
        }
    }
}
