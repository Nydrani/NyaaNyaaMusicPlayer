package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.ArtistAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.loader.ArtistLoader;
import xyz.lostalishar.nyaanyaamusicplayer.model.Artist;

/**
 * Fragment containing entire list of music on device
 */

public class ArtistFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<List<Artist>> {
    private static final String TAG = ArtistFragment.class.getSimpleName();

    private TextView emptyView;

    public ArtistAdapter adapter;

    public static ArtistFragment newInstance() {
        if (BuildConfig.DEBUG) Log.d(TAG, "newInstance");

        return new ArtistFragment();
    }


    //=========================================================================
    // Fragment lifecycle
    //=========================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        adapter = new ArtistAdapter(new ArrayList<>(), cabHolder);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView");

        Activity activity = getActivity();
        RecyclerView.LayoutManager layout = new LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(activity,
                DividerItemDecoration.VERTICAL);

        View rootView = inflater.inflate(R.layout.list_base, container, false);
        emptyView = rootView.findViewById(R.id.empty_view);
        RecyclerView recyclerView = rootView.findViewById(R.id.list_base_view);

        emptyView.setText(R.string.no_artists_found);

        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layout);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onResume");
        super.onResume();

        refreshList();
    }


    //=========================================================================
    // Options menu callbacks
    //=========================================================================

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateOptionsMenu");

        inflater.inflate(R.menu.artist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onOptionsItemSelected");

        int id = item.getItemId();

        switch (id) {
            case R.id.actionbar_refresh:
                refreshList();
                return true;
            default:
                if (BuildConfig.DEBUG) Log.w(TAG, "Unknown menu item id: " + id);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    //=========================================================================
    // LoaderManager.LoaderCallbacks
    //=========================================================================

    @Override
    public @NonNull Loader<List<Artist>> onCreateLoader(int id, Bundle args) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateLoader");

        Activity activity = getActivity();

        return new ArtistLoader(activity, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Artist>> loader, List<Artist> data) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onLoadFinished");

        cabHolder.closeCab();
        adapter.swap(data);
        updateEmptyView();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Artist>> loader) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onLoadReset");

        cabHolder.closeCab();
        adapter.swap(new ArrayList<>());
    }


    //=========================================================================
    // Helper functions
    //=========================================================================

    private void updateEmptyView() {
        if (BuildConfig.DEBUG) Log.d(TAG, "updateEmptyView");

        emptyView.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void refreshList() {
        if (BuildConfig.DEBUG) Log.d(TAG, "refreshList");

        getLoaderManager().restartLoader(0, null, this);
    }
}
