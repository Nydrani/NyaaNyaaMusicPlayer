package xyz.lostalishar.nyaanyaamusicplayer.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;
import xyz.lostalishar.nyaanyaamusicplayer.ui.dialogfragment.AboutDialogFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.AlbumListFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.BaseFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.MusicQueueFragment;

public class AlbumListActivity extends BaseActivity implements MusicQueueFragment.OnViewInflatedListener,
        SlidingUpPanelLayout.PanelSlideListener {
    private static final String TAG = AlbumListActivity.class.getSimpleName();

    private long chosenId = MusicPlaybackService.UNKNOWN_ID;
    private SlidingUpPanelLayout slidingUpPanelLayout;


    //=========================================================================
    // Activity lifecycle
    //=========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_layout_home);
        slidingUpPanelLayout = (SlidingUpPanelLayout)findViewById(R.id.activity_sliding_up_layout);

        chosenId = getIntent().getExtras().getLong("albumId");
    }


    @Override
    protected void onResume() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onResume");
        super.onResume();
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
            case R.id.actionbar_homelink:
                Snackbar.make(findViewById(android.R.id.content), "Replace with your own action", Snackbar.LENGTH_LONG)
                        .show();
                return true;
            case R.id.actionbar_about:
                setDialogFragment(AboutDialogFragment.newInstance());
                return true;
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

        FragmentManager fm = getFragmentManager();
        BaseFragment slidingFragment = (BaseFragment)getSlidingFragment(fm);
        BaseFragment baseFragment = (BaseFragment)getBaseFragment(fm);

        if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            slidingFragment.setHasOptionsMenu(false);
        } else {
            slidingFragment.setHasOptionsMenu(true);
        }

        // @TODO update CAB to be located it the fragment (UI) instead of adapter
        // @TODO pass CAB into the adapter so single cab entry
        baseFragment.closeCAB();
        slidingFragment.closeCAB();
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPanelSlide");
    }


    //=========================================================================
    // Fragment view inflated callback
    //=========================================================================

    @Override
    public void onViewInflated(View view) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onViewInflated");

        SlidingUpPanelLayout rootView = (SlidingUpPanelLayout)findViewById(R.id.activity_sliding_up_layout);
        RecyclerView scrollableView = (RecyclerView)view.findViewById(R.id.list_base_view);

        rootView.setScrollableView(scrollableView);
        rootView.addPanelSlideListener(this);
    }


    //=========================================================================
    // Helper functions
    //=========================================================================

    // initialisation code
    @Override
    protected void initialise() {
        if (BuildConfig.DEBUG) Log.d(TAG, "initialise");
        super.initialise();

        setBaseFragment(AlbumListFragment.newInstance(chosenId));
        setSlidingFragment(MusicQueueFragment.newInstance());
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
}
