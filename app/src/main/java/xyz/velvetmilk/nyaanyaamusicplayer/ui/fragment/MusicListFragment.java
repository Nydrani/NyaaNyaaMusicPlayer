package xyz.velvetmilk.nyaanyaamusicplayer.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import xyz.velvetmilk.nyaanyaamusicplayer.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MusicListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MusicListFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String[] elements = {"Element1", "Elements2", "Element3", "Element4"};


    public static MusicListFragment newInstance(String param1, String param2) {
        MusicListFragment fragment = new MusicListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Activity activity = getActivity();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_musiclist, container, false);

        // get listview
        ListView music_listview = (ListView)view.findViewById(R.id.music_list);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, android.R.id.text1, elements);
        music_listview.setAdapter(adapter);

        return view;
    }
}
