package xyz.lostalishar.nyaanyaamusicplayer.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.AlbumFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.ArtistFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.MusicListFragment;

/**
 * FragmentPager adapter used for the swipe tabs in the home screen
 */

public class LibraryPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = LibraryPagerAdapter.class.getSimpleName();

    public final List<PageHolder> holderList;

    public static final int LIST_FRAGMENT = 0;
    public static final int ALBUM_FRAGMENT = 1;
    public static final int ARTIST_FRAGMENT = 2;

    public LibraryPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.holderList = generatePageList(context);
    }


    // ========================================================================
    // FragmentPagerAdapter overrides
    // ========================================================================

    @Override
    public Fragment getItem(int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getItem");

        Fragment fragment = null;

        switch (position) {
            case LIST_FRAGMENT:
                fragment = MusicListFragment.newInstance();
                break;
            case ALBUM_FRAGMENT:
                fragment = AlbumFragment.newInstance();
                break;
            case ARTIST_FRAGMENT:
                fragment = ArtistFragment.newInstance();
                break;
            default:
                if (BuildConfig.DEBUG) Log.e(TAG,
                        "Can't create LibraryPageFragment for position: " + position);
        }

        return fragment;
    }

    @Override
    public @NonNull Object instantiateItem(ViewGroup container, int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "instantiateItem");
        Object object = super.instantiateItem(container, position);

        Fragment fragment = null;
        // save the appropriate reference depending on position
        switch (position) {
            case LIST_FRAGMENT:
            case ALBUM_FRAGMENT:
            case ARTIST_FRAGMENT:
                fragment = (Fragment)object;
        }

        if (fragment != null) {
            holderList.get(position).tag = fragment.getTag();
        }

        return object;
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


    //=========================================================================
    // helper functions
    //=========================================================================

    private List<LibraryPagerAdapter.PageHolder> generatePageList(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "generatePageList");

        List<PageHolder> pageList = new ArrayList<>();

        PageHolder page = new PageHolder();
        page.name = context.getString(R.string.fragment_name_music_list);
        pageList.add(page);

        page = new PageHolder();
        page.name = context.getString(R.string.fragment_name_album);
        pageList.add(page);

        page = new PageHolder();
        page.name = context.getString(R.string.fragment_name_artist);
        pageList.add(page);

        return pageList;
    }


    // ========================================================================
    // Internal classes
    // ========================================================================

    public static class PageHolder {
        private static final String TAG = PageHolder.class.getSimpleName();

        public String name;
        public String tag;

        public PageHolder() {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
            // empty constructor for now
        }
    }
}
