package xyz.lostalishar.nyaanyaamusicplayer.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialcab.MaterialCab;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.LibraryPagerAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.interfaces.CabHolder;
import xyz.lostalishar.nyaanyaamusicplayer.interfaces.OnViewInflatedListener;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.MiniPlayerFragment;
import xyz.lostalishar.nyaanyaamusicplayer.util.NyaaUtils;
import xyz.lostalishar.nyaanyaamusicplayer.util.PreferenceUtils;

public class HomeActivity extends MusicActivity implements OnViewInflatedListener,
        MiniPlayerFragment.OnMiniPlayerTouchedListener, CabHolder {
    private static final String TAG = HomeActivity.class.getSimpleName();

    private MaterialCab cab;

    private ViewPager viewPager;

    //=========================================================================
    // Activity lifecycle
    //=========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_layout_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // stuff with views
        viewPager = findViewById(R.id.activity_view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        viewPager.setCurrentItem(PreferenceUtils.loadCurViewPagerPosition(this));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    PreferenceUtils.saveCurViewPagerPosition(HomeActivity.this, viewPager.getCurrentItem());
                }
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onTabSelected");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onTabUnselected");

                closeCab();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onTabReselected");
            }
        });
        tabLayout.setupWithViewPager(viewPager);

        // load fragments
        if (savedInstanceState == null) {
            loadFragments();
        // otherwise reloading old data
        } else {
            viewPager.setCurrentItem(savedInstanceState.getInt("currentPos"));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putInt("currentPos", viewPager.getCurrentItem());
    }

    @Override
    protected void initialise() {
        super.initialise();

        viewPager.setAdapter(new LibraryPagerAdapter(getSupportFragmentManager(), this));
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
            case R.id.actionbar_settings:
                NyaaUtils.openSettings(this);
                return true;
            default:
                if (BuildConfig.DEBUG) Log.w(TAG, "Unknown menu item id: " + id);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    //=========================================================================
    // Fragment callbacks
    //=========================================================================

    @Override
    public void onViewInflated(View view) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onViewInflated");

    }

    @Override
    public void onMiniPlayerTouched(View view) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onMiniPlayerTouched");

        NyaaUtils.openQueue(this);
    }


    //=========================================================================
    // Helper functions
    //=========================================================================

    private void loadFragments() {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadFragments");

        // setup the fragments
        setMiniPlayerFragment(MiniPlayerFragment.newInstance());
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

    // Gets the mini player fragment
    private Fragment getMiniPlayerFragment(FragmentManager fm) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getMiniFragment");

        return fm.findFragmentById(R.id.activity_mini_player);
    }
}
