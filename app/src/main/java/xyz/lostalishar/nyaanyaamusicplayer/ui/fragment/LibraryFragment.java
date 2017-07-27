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
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.LibraryPagerAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackState;
import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;
import xyz.lostalishar.nyaanyaamusicplayer.util.NyaaUtils;

/**
 * Fragment containing entire list of music on device
 */

public class LibraryFragment extends Fragment {
    private static final String TAG = LibraryFragment.class.getSimpleName();

    private List<LibraryPagerAdapter.PageHolder> pageList;
    private LibraryPagerAdapter adapter;
    private ViewPager viewPager;
    private TabLayout.OnTabSelectedListener tabSelectedListener;
    private TextView pauseBox;

    private IntentFilter filter;
    private MetaChangedListener metaChangedListener;

    private static final int LIST_FRAGMENT = 0;
    private static final int QUEUE_FRAGMENT = 1;

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
        pauseBox = (TextView) rootView.findViewById(R.id.fragment_library_bottom_bar);
        final TabLayout tabLayout = (TabLayout)viewPager.findViewById(R.id.fragment_library_tab_layout);

        viewPager.setAdapter(adapter);
        tabLayout.addOnTabSelectedListener(tabSelectedListener);
        tabLayout.setupWithViewPager(viewPager);

        // update all meta ui components
        updateMetaUI();

        pauseBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onClick");

                MusicPlaybackState state = MusicUtils.getState();
                // do nothing on unknown state
                if (state == null) {
                    return;
                }

                if (MusicUtils.isPlaying()) {
                    MusicUtils.pause();
                } else if (state.getQueuePos() == MusicPlaybackService.UNKNOWN_POS) {
                    Toast.makeText(v.getContext(), R.string.toast_choose_track, Toast.LENGTH_SHORT).show();
                    viewPager.setCurrentItem(QUEUE_FRAGMENT);
                } else {
                    MusicUtils.resume();
                    // @TODO resume when already playing. start when not loaded
                }
            }
        });

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
        page.fragment = Fragment.instantiate(activity, MusicQueueFragment.class.getName());
        page.sname = getString(R.string.fragment_name_queue);
        pageList.add(page);

        return pageList;
    }

    private void updatePauseBox() {
        if (BuildConfig.DEBUG) Log.d(TAG, "updatePauseBox");

        if (MusicUtils.isPlaying()) {
            pauseBox.setText(getString(R.string.fragment_library_bottom_bar_pause));
        } else {
            pauseBox.setText(getString(R.string.fragment_library_bottom_bar_play));
        }
    }

    private void updateMetaUI() {
        if (BuildConfig.DEBUG) Log.d(TAG, "updateMetaUI");

        updatePauseBox();
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
                reference.get().updateMetaUI();
            }
        }
    }
}
