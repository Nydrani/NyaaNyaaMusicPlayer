package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.LibraryPagerAdapter;

/**
 * Fragment containing entire list of music on device
 */

public class LibraryFragment extends Fragment {
    private static final String TAG = LibraryFragment.class.getSimpleName();

    private List<LibraryPagerAdapter.PageHolder> pageList;
    private LibraryPagerAdapter adapter;
    private ViewPager viewPager;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_library, container, false);
        viewPager = (ViewPager)rootView.findViewById(R.id.fragment_library_view_pager);
        TabLayout tabLayout = (TabLayout)viewPager.findViewById(R.id.fragment_library_tab_layout);

        tabLayout.setupWithViewPager(viewPager);
        viewPager.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onViewCreated");

        super.onViewCreated(view, savedInstanceState);
    }


    //=========================================================================
    // helper functions
    //=========================================================================

    private List<LibraryPagerAdapter.PageHolder> generatePageList() {
        if (BuildConfig.DEBUG) Log.d(TAG, "generatePageList");

        List<LibraryPagerAdapter.PageHolder> pageList = new ArrayList<>();

        LibraryPagerAdapter.PageHolder page = new LibraryPagerAdapter.PageHolder();
        page.fname = MusicListFragment.class.getName();
        page.sname = getString(R.string.fragment_name_music_list);

        pageList.add(page);

        page = new LibraryPagerAdapter.PageHolder();
        page.fname = MusicQueueFragment.class.getName();
        page.sname = getString(R.string.fragment_name_queue);

        pageList.add(page);

        return pageList;
    }
}
