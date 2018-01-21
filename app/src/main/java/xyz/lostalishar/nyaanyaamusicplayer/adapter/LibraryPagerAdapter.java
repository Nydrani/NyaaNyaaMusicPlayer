package xyz.lostalishar.nyaanyaamusicplayer.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;

/**
 * FragmentPager adapter used for the swipe tabs in the home screen
 */

public class LibraryPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = LibraryPagerAdapter.class.getSimpleName();

    private final List<PageHolder> holderList;

    public LibraryPagerAdapter(FragmentManager fm, List<PageHolder> holderList) {
        super(fm);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.holderList = holderList;
    }


    // ========================================================================
    // FragmentPagerAdapter overrides
    // ========================================================================

    @Override
    public Fragment getItem(int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getItem");

        PageHolder page = holderList.get(position);

        return page.fragment;
    }

    @Override
    public int getCount() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getCount");

        return holderList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getPageTitle");

        return holderList.get(position).name;
    }


    // ========================================================================
    // Internal classes
    // ========================================================================

    public static class PageHolder {
        private static final String TAG = PageHolder.class.getSimpleName();

        public Fragment fragment;
        public String name;

        public PageHolder() {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
            // empty constructor for now
        }
    }
}
