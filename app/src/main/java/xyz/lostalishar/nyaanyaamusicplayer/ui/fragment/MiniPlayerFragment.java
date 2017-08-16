package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackState;
import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;
import xyz.lostalishar.nyaanyaamusicplayer.util.NyaaUtils;

/**
 * MiniPlayer fragment for quick next, prev and play/pause
 */

public class MiniPlayerFragment extends Fragment {
    private static final String TAG = MiniPlayerFragment.class.getSimpleName();

    private IntentFilter filter;
    private MetaChangedListener metaChangedListener;

    private TextView musicTitleView;
    private Button playPauseButton;

    public static MiniPlayerFragment newInstance() {
        if (BuildConfig.DEBUG) Log.d(TAG, "newInstance");

        return new MiniPlayerFragment();
    }

    //=========================================================================
    // Fragment lifecycle
    //=========================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        filter = new IntentFilter();
        filter.addAction(NyaaUtils.META_CHANGED);
        metaChangedListener = new MetaChangedListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_mini_player, container, false);
        Button prev = (Button)rootView.findViewById(R.id.prev_button);
        Button next = (Button)rootView.findViewById(R.id.next_button);
        View playerDetailsContainer = rootView.findViewById(R.id.player_details_container);

        musicTitleView = (TextView)playerDetailsContainer.findViewById(R.id.mini_player_title);
        playPauseButton = (Button)playerDetailsContainer.findViewById(R.id.play_pause_button);


        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicUtils.previous();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicUtils.next();
            }
        });

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlaybackState state = MusicUtils.getState();
                // do nothing on unknown state
                if (state == null) {
                    return;
                }

                if (MusicUtils.isPlaying()) {
                    MusicUtils.pause();
                } else if (state.getQueuePos() == MusicPlaybackService.UNKNOWN_POS) {
                    Toast.makeText(v.getContext(), R.string.toast_choose_track, Toast.LENGTH_SHORT).show();
                } else {
                    MusicUtils.resume();
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

        // update ui on resume
        updateMetaUI();
    }

    @Override
    public void onPause() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPause");
        super.onPause();

        Activity activity = getActivity();
        activity.unregisterReceiver(metaChangedListener);
    }


    //=========================================================================
    // Helper functions
    //=========================================================================

    private void updateMetaUI() {
        if (BuildConfig.DEBUG) Log.d(TAG, "updateMetaUI");

        updatePlayPauseButton();
    }

    private void updatePlayPauseButton() {
        if (BuildConfig.DEBUG) Log.d(TAG, "updatePlayPauseButton");

        if (MusicUtils.isPlaying()) {
            musicTitleView.setText("ayyylemao");
            playPauseButton.setText(R.string.fragment_player_bar_pause);
        } else {
            musicTitleView.setText("weeeooooo");
            playPauseButton.setText(R.string.fragment_player_bar_play);
        }
    }


    //=========================================================================
    // Internal classes
    //=========================================================================

    private static final class MetaChangedListener extends BroadcastReceiver {
        private static final String TAG = MiniPlayerFragment.MetaChangedListener.class.getSimpleName();

        private WeakReference<MiniPlayerFragment> reference;

        public MetaChangedListener(MiniPlayerFragment miniPlayerFragment) {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

            reference = new WeakReference<>(miniPlayerFragment);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BuildConfig.DEBUG) Log.d(TAG, "onReceive");

            final String action = intent.getAction();

            switch (action) {
                case NyaaUtils.META_CHANGED:
                    reference.get().updateMetaUI();
                    break;
                default:
                    if (BuildConfig.DEBUG) Log.e(TAG, "Unknown action: " + action);
            }
        }
    }
}
