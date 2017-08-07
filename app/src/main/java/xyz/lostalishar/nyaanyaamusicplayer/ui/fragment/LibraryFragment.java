package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.LibraryPagerAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.util.NyaaUtils;

/**
 * Fragment containing entire list of music on device
 */

public class LibraryFragment extends Fragment {
    private static final String TAG = LibraryFragment.class.getSimpleName();

    public List<LibraryPagerAdapter.PageHolder> pageList;
    private LibraryPagerAdapter adapter;
    private ViewPager viewPager;
    private TabLayout.OnTabSelectedListener tabSelectedListener;

    private IntentFilter filter;
    private MetaChangedListener metaChangedListener;

    public static final int LIST_FRAGMENT = 0;
    public static final int QUEUE_FRAGMENT = 1;

    public static LibraryFragment newInstance() {
        if (BuildConfig.DEBUG) Log.d(TAG, "newInstance");

        return new LibraryFragment();
    }


    //=========================================================================
    // Fragment lifecycle
    //=========================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        Activity activity = getActivity();

        pageList = generatePageList();
        adapter = new LibraryPagerAdapter(activity, getChildFragmentManager(), pageList);

        tabSelectedListener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onTabSelected");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onTabUnselected");

                BaseFragment frag = (BaseFragment)adapter.getItem(tab.getPosition());

                // need to check for null in case of a possible orientation change
                if (frag.adapter == null) {
                    return;
                }

                if (frag.adapter.isCABOpen()) {
                    frag.adapter.finishCAB();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onTabReselected");
            }
        };

        filter = new IntentFilter(NyaaUtils.META_CHANGED);
        metaChangedListener = new MetaChangedListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_library, container, false);
        viewPager = (ViewPager)rootView.findViewById(R.id.fragment_library_view_pager);
        final TabLayout tabLayout = (TabLayout)viewPager.findViewById(R.id.fragment_library_tab_layout);

        viewPager.setAdapter(adapter);
        tabLayout.addOnTabSelectedListener(tabSelectedListener);
        tabLayout.setupWithViewPager(viewPager);

        return rootView;
    }

    @Override
    public void onResume() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onResume");
        super.onResume();

        Activity activity = getActivity();
        activity.registerReceiver(metaChangedListener, filter);
    }

    @Override
    public void onPause() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPause");
        super.onPause();

        Activity activity = getActivity();
        activity.unregisterReceiver(metaChangedListener);
    }


    //=========================================================================
    // helper functions
    //=========================================================================

    private List<LibraryPagerAdapter.PageHolder> generatePageList() {
        if (BuildConfig.DEBUG) Log.d(TAG, "generatePageList");

        List<LibraryPagerAdapter.PageHolder> pageList = new ArrayList<>();
        Activity activity = getActivity();

        LibraryPagerAdapter.PageHolder page = new LibraryPagerAdapter.PageHolder();
        page.fragment = Fragment.instantiate(activity, MusicListFragment.class.getName());
        page.sname = getString(R.string.fragment_name_music_list);
        pageList.add(page);

        page = new LibraryPagerAdapter.PageHolder();
        page.fragment = Fragment.instantiate(activity, MusicListFragment.class.getName());
        page.sname = getString(R.string.fragment_name_queue);
        pageList.add(page);

        return pageList;
    }


    //=========================================================================
    // Internal classes
    //=========================================================================

    private static final class MetaChangedListener extends BroadcastReceiver {
        private static final String TAG = MetaChangedListener.class.getSimpleName();

        private WeakReference<LibraryFragment> reference;

        public MetaChangedListener(LibraryFragment libraryFragment) {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

            reference = new WeakReference<>(libraryFragment);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BuildConfig.DEBUG) Log.d(TAG, "onReceive");

            final String action = intent.getAction();

            if (action.equals(NyaaUtils.META_CHANGED)) {
                // reference.get().updateMetaUI();
            }
        }
    }
}
