package xyz.lostalishar.nyaanyaamusicplayer.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
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
import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.AlbumListFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.BaseFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.MiniPlayerFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.MusicQueueFragment;

public class AlbumListActivity extends BaseActivity implements OnViewInflatedListener,
        MiniPlayerFragment.OnMiniPlayerTouchedListener, SlidingUpPanelLayout.PanelSlideListener {
    private static final String TAG = AlbumListActivity.class.getSimpleName();

    private SlidingUpPanelLayout slidingUpPanelLayout;

    private Fragment musicQueueFragment;
    private Fragment albumListFragment;
    private Fragment miniPlayerFragment;


    //=========================================================================
    // Activity lifecycle
    //=========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_layout_home);
        slidingUpPanelLayout = (SlidingUpPanelLayout)findViewById(R.id.activity_sliding_up_layout);

        // setup fragments
        long chosenId = MusicPlaybackService.UNKNOWN_ID;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            chosenId = extras.getLong("albumId");
        }
        albumListFragment = AlbumListFragment.newInstance(chosenId);
        musicQueueFragment = MusicQueueFragment.newInstance();
        miniPlayerFragment = MiniPlayerFragment.newInstance();
    }

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
        mi.inflate(R.menu.album_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onOptionsItemSelected");

        int id = item.getItemId();

        switch (id) {
            case R.id.actionbar_settings:
                Toast.makeText(this, R.string.app_name, Toast.LENGTH_LONG).show();
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

        SlidingUpPanelLayout slidingView = (SlidingUpPanelLayout)findViewById(R.id.activity_sliding_up_layout);

        if (view.getId() == R.id.fragment_mini_player_container) {
            updateUI(slidingView.getPanelState());
        } else if (view.getId() == R.id.fragment_queue_container) {
            RecyclerView scrollableView = (RecyclerView) view.findViewById(R.id.list_base_view);

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

        setBaseFragment(albumListFragment);
        setSlidingFragment(musicQueueFragment);
        setMiniPlayerFragment(miniPlayerFragment);
    }

    /*
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

    /*
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

    // Gets the current fragment being shown
    private Fragment getBaseFragment(FragmentManager fm) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getBaseFragment");

        return fm.findFragmentById(R.id.activity_base_content);
    }

    // Gets the current fragment being shown
    private Fragment getSlidingFragment(FragmentManager fm) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getSlidingFragment");

        return fm.findFragmentById(R.id.activity_sliding_content);
    }

    // Gets the mini player fragment
    private Fragment getMiniPlayerFragment(FragmentManager fm) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getMiniFragment");

        return fm.findFragmentById(R.id.activity_mini_player);
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
        BaseFragment slidingFragment = (BaseFragment)getSlidingFragment(fm);
        View miniPlayerView = miniPlayerFragment.getView();
        ActionBar actionBar = getSupportActionBar();


        if (state == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            slidingFragment.setHasOptionsMenu(false);
            if (miniPlayerView != null) {
                miniPlayerView.setVisibility(View.VISIBLE);
            }
            if (actionBar != null) {
                actionBar.setTitle(R.string.app_name);
            }
        } else if (state == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingFragment.setHasOptionsMenu(true);
            if (miniPlayerView != null) {
                miniPlayerView.setVisibility(View.GONE);
            }
            if (actionBar != null) {
                actionBar.setTitle(R.string.fragment_name_queue);
            }
        } else {
            slidingFragment.setHasOptionsMenu(true);
            if (miniPlayerView != null) {
                miniPlayerView.setVisibility(View.VISIBLE);
            }
        }
    }
}
