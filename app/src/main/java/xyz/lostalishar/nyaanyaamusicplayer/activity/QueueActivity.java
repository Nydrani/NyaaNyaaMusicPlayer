package xyz.lostalishar.nyaanyaamusicplayer.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialcab.MaterialCab;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.interfaces.CabHolder;
import xyz.lostalishar.nyaanyaamusicplayer.interfaces.OnViewInflatedListener;
import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.ArtistListFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.MiniPlayerFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.MusicQueueFragment;

public class QueueActivity extends MusicActivity implements OnViewInflatedListener,
        MiniPlayerFragment.OnMiniPlayerTouchedListener, CabHolder {
    private static final String TAG = QueueActivity.class.getSimpleName();

    private MaterialCab cab;


    //=========================================================================
    // Activity lifecycle
    //=========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_layout_base);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set up actionbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // load fragments
        if (savedInstanceState == null) {
            loadFragments();
        }
    }


    //=========================================================================
    // Other activity callbacks
    //=========================================================================

    @Override
    public void onBackPressed() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onBackPressed");

        if (cab != null && cab.isActive()) {
            cab.finish();
        } else {
            super.onBackPressed();
        }
    }


    //=========================================================================
    // CabHolder callback
    //=========================================================================

    @Override
    public MaterialCab openCab(MaterialCab.Callback callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "openCab");

        if (cab != null && cab.isActive()) {
            cab.finish();
        }

        cab = new MaterialCab(this, R.id.cab_stub)
                .start(callback);

        return cab;
    }

    @Override
    public void closeCab() {
        if (BuildConfig.DEBUG) Log.d(TAG, "closeCab");

        if (cab != null && cab.isActive()) {
            cab.finish();
        }
    }


    //=========================================================================
    // Options menu callbacks
    //=========================================================================

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onOptionsItemSelected");

        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                if (BuildConfig.DEBUG) Log.w(TAG, "Unknown menu item id: " + id);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    //=========================================================================
    // MiniPlayer listener overrides
    //=========================================================================


    @Override
    public void onViewInflated(View view) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onViewInflated");

    }

    @Override
    public void onMiniPlayerTouched(View view) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onMiniPlayerTouched");

    }


    //=========================================================================
    // Helper functions
    //=========================================================================

    private void loadFragments() {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadFragments");

        setBaseFragment(MusicQueueFragment.newInstance());
        setMiniPlayerFragment(MiniPlayerFragment.newInstance());
    }

    /*
     * Replaces the fragment in the FrameLayout container
     * If fragment == null : remove "all" from fragment     <---- all is assuming only 1
     * If fragment != null : replace with new fragment
     */
    private void setBaseFragment(Fragment fragment) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setBaseFragment");

        FragmentManager fm = getSupportFragmentManager();
        Fragment element = getBaseFragment(fm);

        // check for "remove fragment" and null fragment in container
        if (fragment == null && element == null) {
            return;
        }

        FragmentTransaction ft = fm.beginTransaction();
        if (fragment == null) {
            ft.remove(element);
        } else {
            ft.replace(R.id.activity_base_content, fragment);
        }
        ft.commit();
    }

    /**
     * Replaces the fragment in the FrameLayout container
     * If fragment == null : remove "all" from fragment     <---- all is assuming only 1
     * If fragment != null : replace with new fragment
     */
    private void setMiniPlayerFragment(Fragment fragment) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setMiniPlayerFragment");

        FragmentManager fm = getSupportFragmentManager();
        Fragment element = getMiniPlayerFragment(fm);

        // check for "remove fragment" and null fragment in container
        if (fragment == null && element == null) {
            return;
        }

        FragmentTransaction ft = fm.beginTransaction();
        if (fragment == null) {
            ft.remove(element);
        } else {
            ft.replace(R.id.activity_mini_player, fragment);
        }
        ft.commit();
    }

    // Gets the current fragment being shown
    private Fragment getBaseFragment(FragmentManager fm) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getBaseFragment");

        return fm.findFragmentById(R.id.activity_base_content);
    }

    // Gets the mini player fragment
    private Fragment getMiniPlayerFragment(FragmentManager fm) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getMiniFragment");

        return fm.findFragmentById(R.id.activity_mini_player);
    }
}
