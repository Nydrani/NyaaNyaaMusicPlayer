package xyz.lostalishar.nyaanyaamusicplayer.activity;

import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.interfaces.OnViewInflatedListener;
import xyz.lostalishar.nyaanyaamusicplayer.observer.MediaStoreObserver;
import xyz.lostalishar.nyaanyaamusicplayer.ui.dialogfragment.AboutDialogFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.BaseFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.LibraryFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.MiniPlayerFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.MusicQueueFragment;

public class HomeActivity extends BaseActivity implements OnViewInflatedListener,
        MiniPlayerFragment.OnMiniPlayerTouchedListener, SlidingUpPanelLayout.PanelSlideListener {
    private static final String TAG = HomeActivity.class.getSimpleName();

    private SlidingUpPanelLayout slidingUpPanelLayout;

    private MediaStoreObserver mediaStoreObserver;


    //=========================================================================
    // Activity lifecycle
    //=========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_layout_home);
        slidingUpPanelLayout = findViewById(R.id.activity_sliding_up_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // setup a content observer
        mediaStoreObserver = new MediaStoreObserver(new Handler());
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, mediaStoreObserver);

        // load fragments
        loadFragments();
    }

    @Override
    protected void onDestroy() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy");
        super.onDestroy();

        getContentResolver().unregisterContentObserver(mediaStoreObserver);
    }


    //=========================================================================
    // Other activity callbacks
    //=========================================================================

    @Override
    public void onBackPressed() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onBackPressed");

        if (!handleBackPressed()) {
            super.onBackPressed();
        }
    }


    //=========================================================================
    // Options menu callbacks
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
            case R.id.actionbar_about:
                setDialogFragment(AboutDialogFragment.newInstance());
                return true;
            case R.id.actionbar_settings:
                Toast.makeText(this, R.string.app_name, Toast.LENGTH_LONG).show();
                Snackbar.make(findViewById(android.R.id.content), R.string.app_name, Snackbar.LENGTH_SHORT)
                        .show();
                return true;
            default:
                if (BuildConfig.DEBUG) Log.w(TAG, "Unknown menu item id: " + id);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    //=========================================================================
    // Panel slide listener callback
    //=========================================================================

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
                                    SlidingUpPanelLayout.PanelState newState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPanelStateChanged");

        updateUI(newState);
        closeCab();
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPanelSlide");

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = getMiniPlayerFragment(fm);
        MiniPlayerFragment miniPlayerFragment = (MiniPlayerFragment) fragment;

        // update transparency
        miniPlayerFragment.setAlpha(1.0f - slideOffset);
    }


    //=========================================================================
    // Fragment view inflated callback
    //=========================================================================

    @Override
    public void onViewInflated(View view) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onViewInflated");

        SlidingUpPanelLayout slidingView = findViewById(R.id.activity_sliding_up_layout);

        if (view.getId() == R.id.fragment_mini_player_container) {
            updateUI(slidingView.getPanelState());
        } else if (view.getId() == R.id.fragment_queue_container) {
            RecyclerView scrollableView = view.findViewById(R.id.list_base_view);

            slidingView.setScrollableView(scrollableView);
            slidingView.addPanelSlideListener(this);
        }
    }

    @Override
    public void onMiniPlayerTouched(View view) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onMiniPlayerTouched");

        expandPanel();
    }


    //=========================================================================
    // Helper functions
    //=========================================================================

    private void loadFragments() {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadFragments");

        // setup the fragments
        setBaseFragment(LibraryFragment.newInstance());
        setSlidingFragment(MusicQueueFragment.newInstance());
        setMiniPlayerFragment(MiniPlayerFragment.newInstance());
        setSlidingMiniPlayerFragment(MiniPlayerFragment.newInstance());
    }

    /**
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
    private void setSlidingFragment(Fragment fragment) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setSlidingFragment");

        FragmentManager fm = getSupportFragmentManager();
        Fragment element = getSlidingFragment(fm);

        // check for "remove fragment" and null fragment in container
        if (fragment == null && element == null) {
            return;
        }

        FragmentTransaction ft = fm.beginTransaction();
        if (fragment == null) {
            ft.remove(element);
        } else {
            ft.replace(R.id.activity_sliding_content, fragment);
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

    /**
     * Replaces the fragment in the FrameLayout container
     * If fragment == null : remove "all" from fragment     <---- all is assuming only 1
     * If fragment != null : replace with new fragment
     */
    private void setSlidingMiniPlayerFragment(Fragment fragment) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setSlidingMiniPlayerFragment");

        FragmentManager fm = getSupportFragmentManager();
        Fragment element = getSlidingMiniPlayerFragment(fm);

        // check for "remove fragment" and null fragment in container
        if (fragment == null && element == null) {
            return;
        }

        FragmentTransaction ft = fm.beginTransaction();
        if (fragment == null) {
            ft.remove(element);
        } else {
            ft.replace(R.id.fragment_bottom_bar, fragment);
        }
        ft.commit();
    }

    // Gets the current fragment being shown
    private Fragment getBaseFragment(FragmentManager fm) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getBaseFragment");

        return fm.findFragmentById(R.id.activity_base_content);
    }

    // Gets the slider fragment
    private Fragment getSlidingFragment(FragmentManager fm) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getSlidingFragment");

        return fm.findFragmentById(R.id.activity_sliding_content);
    }

    // Gets the mini player fragment
    private Fragment getMiniPlayerFragment(FragmentManager fm) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getMiniFragment");

        return fm.findFragmentById(R.id.activity_mini_player);
    }

    // Gets the other mini player fragment
    private Fragment getSlidingMiniPlayerFragment(FragmentManager fm) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getSlidingMiniFragment");

        return fm.findFragmentById(R.id.fragment_bottom_bar);
    }

    private boolean handleBackPressed() {
        if (BuildConfig.DEBUG) Log.d(TAG, "handleBackPressed");

        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            collapsePanel();
            return true;
        }
        return false;
    }


    private void collapsePanel() {
        if (BuildConfig.DEBUG) Log.d(TAG, "collapsePanel");

        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    private void expandPanel() {
        if (BuildConfig.DEBUG) Log.d(TAG, "expandPanel");

        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    private void updateUI(SlidingUpPanelLayout.PanelState state) {
        if (BuildConfig.DEBUG) Log.d(TAG, "updateUI");

        FragmentManager fm = getSupportFragmentManager();
        LibraryFragment libraryFragment = (LibraryFragment) getBaseFragment(fm);
        BaseFragment slidingFragment = (BaseFragment) getSlidingFragment(fm);
        MiniPlayerFragment miniPlayerFragment = (MiniPlayerFragment) getMiniPlayerFragment(fm);

        switch (state) {
            case COLLAPSED:
                slidingFragment.setHasOptionsMenu(false);
                libraryFragment.setChildrenOptionsMenu(true);
                break;
            case EXPANDED:
                slidingFragment.setHasOptionsMenu(true);
                libraryFragment.setChildrenOptionsMenu(false);

                miniPlayerFragment.setVisibility(View.GONE);
                break;
            default:
                miniPlayerFragment.setVisibility(View.VISIBLE);
        }
    }
}
