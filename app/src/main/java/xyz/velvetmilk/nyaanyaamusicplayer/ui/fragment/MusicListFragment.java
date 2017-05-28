package xyz.velvetmilk.nyaanyaamusicplayer.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import xyz.velvetmilk.nyaanyaamusicplayer.R;
import xyz.velvetmilk.nyaanyaamusicplayer.loader.MusicListLoader;
import xyz.velvetmilk.nyaanyaamusicplayer.model.MusicPiece;

/**
 * Created by nydrani on 28/05/17.
 */
public class MusicListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<MusicPiece>> {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private View rootView;
    private ListView listView;
    private ArrayAdapter<MusicPiece> adapter;



    public static MusicListFragment newInstance(String param1, String param2) {
        MusicListFragment fragment = new MusicListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity activity = getActivity();
        adapter = new ArrayAdapter<MusicPiece>(activity, 0);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.list_base, container, false);
        TextView textView = (TextView)rootView.findViewById(R.id.list_base_name);
        textView.setText(savedInstanceState.getString(ARG_PARAM1));
        listView = (ListView)rootView.findViewById(R.id.list_base);
        listView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<List<MusicPiece>> onCreateLoader (final int id, final Bundle args) {
        Activity activity = getActivity();
        MusicListLoader loader = new MusicListLoader(activity);
        return loader;
    }

    @Override
    public void onLoadFinished(final Loader<List<MusicPiece>> loader, final List<MusicPiece> data) {
        if (data.isEmpty()) {
            adapter.clear();
            return;
        }

        adapter.addAll(data);
    }

    @Override
    public void onLoaderReset(final Loader<List<MusicPiece>> loader) {
        adapter.clear();
    }
}
