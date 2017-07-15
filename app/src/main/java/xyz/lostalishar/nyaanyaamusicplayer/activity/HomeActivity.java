package xyz.lostalishar.nyaanyaamusicplayer.activity;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.ui.dialogfragment.AboutDialogFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.LibraryFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.MusicListFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.MusicQueueFragment;

public class HomeActivity extends BaseActivity {
    private static final String TAG = HomeActivity.class.getSimpleName();


    //=========================================================================
    // Activity lifecycle
    //=========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_base);
    }


    //=========================================================================
    // Activity menu callbacks
    //=========================================================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateOptionsMenu");

        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onOptionsItemSelected");

        int id = item.getItemId();

        switch (id) {
            case R.id.actionbar_homelink:
                Snackbar.make(findViewById(android.R.id.content), "Replace with your own action", Snackbar.LENGTH_LONG)
                        .show();
                return true;
            case R.id.actionbar_settings:
                Toast.makeText(this, R.string.app_name, Toast.LENGTH_LONG).show();
                return true;
            case R.id.actionbar_about:
                setDialogFragment(AboutDialogFragment.newInstance());
                return true;
            case R.id.actionbar_library:
                setFragment(LibraryFragment.newInstance());
                return true;
            case R.id.actionbar_refresh:
                refreshFragments();
                return true;
            default:
                if (BuildConfig.DEBUG) Log.w(TAG, "Unknown menu item");
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    //=========================================================================
    // Helper functions
    //=========================================================================

    // initialisation code
    @Override
    protected void initialise() {
        if (BuildConfig.DEBUG) Log.d(TAG, "initialise");
        super.initialise();

        setFragment(LibraryFragment.newInstance());
    }

    protected void refreshFragments() {
        if (BuildConfig.DEBUG) Log.d(TAG, "refreshFragments");

        // @TODO stub
    }
}
