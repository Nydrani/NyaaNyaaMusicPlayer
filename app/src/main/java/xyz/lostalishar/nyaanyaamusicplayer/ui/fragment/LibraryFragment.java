package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.LibraryPagerAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.interfaces.CabHolder;
import xyz.lostalishar.nyaanyaamusicplayer.util.NyaaUtils;

/**
 * Fragment containing entire list of music on device
 */

public class LibraryFragment extends Fragment {
    private static final String TAG = LibraryFragment.class.getSimpleName();

    private LibraryPagerAdapter adapter;
    private ViewPager viewPager;
    private TabLayout.OnTabSelectedListener tabSelectedListener;

    private CabHolder cabHolder;

    private IntentFilter filter;
    private MetaChangedListener metaChangedListener;

    public static LibraryFragment newInstance() {
        if (BuildConfig.DEBUG) Log.d(TAG, "newInstance");

        return new LibraryFragment();
    }


    //=========================================================================
    // Fragment lifecycle
    //=========================================================================

    @Override
    public void onAttach(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onAttach");
        super.onAttach(context);

        if (context instanceof Activity){
            Activity activity = (Activity)context;

            try {
                cabHolder = (CabHolder)activity;
            } catch (ClassCastException e) {
                if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage());
                throw new ClassCastException(activity.toString() +
                        " must implement CabHolder");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        adapter = new LibraryPagerAdapter(getChildFragmentManager(), getActivity());

        tabSelectedListener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onTabSelected");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onTabUnselected");

                cabHolder.closeCab();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_library, container, false);
        viewPager = rootView.findViewById(R.id.fragment_library_view_pager);
        TabLayout tabLayout = viewPager.findViewById(R.id.fragment_library_tab_layout);

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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putInt("currentPos", viewPager.getCurrentItem());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            return;
        }
        viewPager.setCurrentItem(savedInstanceState.getInt("currentPos"));
    }


    //=========================================================================
    // helper functions
    //=========================================================================

    // @TODO fix this later
    /*
    public void setChildrenOptionsMenu(boolean hasMenu) {
        for (LibraryPagerAdapter.PageHolder pageHolder : pageList) {
            pageHolder.fragment.setHasOptionsMenu(hasMenu);
        }
    }
    */


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

            if (action == null) {
                return;
            }

            switch (action) {
                case NyaaUtils.SERVICE_READY:
                    //reference.get().updateMetaUI();
                    break;
                default:
                    if (BuildConfig.DEBUG) Log.e(TAG, "Unknown action: " + action);
            }
        }
    }
}
