package xyz.lostalishar.nyaanyaamusicplayer.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import xyz.lostalishar.nyaanyaamusicplayer.ui.dialogfragment.AboutDialogFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.BaseFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.LibraryFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.MiniPlayerFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.MusicQueueFragment;

public class HomeActivity extends BaseActivity implements OnViewInflatedListener,
        MiniPlayerFragment.OnMiniPlayerTouchedListener, SlidingUpPanelLayout.PanelSlideListener {
    private static final String TAG = HomeActivity.class.getSimpleName();

    private SlidingUpPanelLayout slidingUpPanelLayout;

    private Fragment libraryFragment;
    private Fragment musicQueueFragment;
    private Fragment miniPlayerFragment;
    private Fragment slidingMiniPlayerFragment;


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

        // setup the fragments
        if (savedInstanceState == null) {
            libraryFragment = LibraryFragment.newInstance();
        } else {
            libraryFragment = getFragmentManager().getFragment(savedInstanceState, "libraryFragment");
        }
        musicQueueFragment = MusicQueueFragment.newInstance();
        miniPlayerFragment = MiniPlayerFragment.newInstance();
        slidingMiniPlayerFragment = MiniPlayerFragment.newInstance();
    }

    @Override
    public void onBackPressed() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onBackPressed");

        if (!handleBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        getFragmentManager().putFragment(outState, "libraryFragment", libraryFragment);
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

        View miniPlayerView = miniPlayerFragment.getView();

        if (miniPlayerView != null) {
            miniPlayerView.setAlpha(1.0f - slideOffset);
        }
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

    // initialisation code
    @Override
    protected void initialise() {
        if (BuildConfig.DEBUG) Log.d(TAG, "initialise");
        super.initialise();

        setBaseFragment(libraryFragment);
        setSlidingFragment(musicQueueFragment);
        setMiniPlayerFragment(miniPlayerFragment);
        setSlidingMiniPlayerFragment(slidingMiniPlayerFragment);
    }

    /**
     * Replaces the fragment in the FrameLayout container
     * If fragment == null : remove "all" from fragment     <---- all is assuming only 1
     * If fragment != null : replace with new fragment
     */
    private void setBaseFragment(Fragment fragment) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setBaseFragment");

        FragmentManager fm = getFragmentManager();
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

        FragmentManager fm = getFragmentManager();
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

        FragmentManager fm = getFragmentManager();
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

        FragmentManager fm = getFragmentManager();
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

        FragmentManager fm = getFragmentManager();
        LibraryFragment baseFragment = (LibraryFragment)getBaseFragment(fm);
        BaseFragment slidingFragment = (BaseFragment)getSlidingFragment(fm);
        View miniPlayerView = miniPlayerFragment.getView();

        if (state == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            slidingFragment.setHasOptionsMenu(false);

            if (miniPlayerView != null) {
                miniPlayerView.setVisibility(View.VISIBLE);
            }
        } else if (state == SlidingUpPanelLayout.PanelState.EXPANDED) {
            baseFragment.setChildrenOptionsMenu(false);

            if (miniPlayerView != null) {
                miniPlayerView.setVisibility(View.GONE);
            }
        } else {
            baseFragment.setChildrenOptionsMenu(true);
            slidingFragment.setHasOptionsMenu(true);

            if (miniPlayerView != null) {
                miniPlayerView.setVisibility(View.VISIBLE);
            }
        }
    }
}
