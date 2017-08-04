package xyz.lostalishar.nyaanyaamusicplayer.adapter;

import android.content.Context;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.BaseFragment;

/**
 * FragmentPager adapter used for the swipe tabs in the home screen
 */

public class LibraryPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = LibraryPagerAdapter.class.getSimpleName();

    private final List<PageHolder> holderList;
    private Context context;

    public LibraryPagerAdapter(Context context, FragmentManager fm, List<PageHolder> holderList) {
        super(fm);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.context = context;
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

        return holderList.get(position).sname;
    }


    // ========================================================================
    // Internal classes
    // ========================================================================

    public static class PageHolder {
        private static final String TAG = PageHolder.class.getSimpleName();

        public Fragment fragment;
        public String sname;

        public PageHolder() {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
            // empty constructor for now
        }
    }
}
