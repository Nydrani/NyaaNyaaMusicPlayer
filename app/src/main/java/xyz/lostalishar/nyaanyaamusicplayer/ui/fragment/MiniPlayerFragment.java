package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.interfaces.OnViewInflatedListener;
import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackState;
import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackTrack;
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

    private OnMiniPlayerTouchedListener miniPlayerTouchedListener;
    private OnViewInflatedListener viewInflatedListener;

    private TextView musicTitleView;
    private TextView musicArtistView;
    private ImageButton playPauseButton;

    private ProgressBar progressBar;

    private Handler handler;
    private Runnable updater;

    public static MiniPlayerFragment newInstance() {
        if (BuildConfig.DEBUG) Log.d(TAG, "newInstance");

        return new MiniPlayerFragment();
    }

    //=========================================================================
    // Fragment lifecycle
    //=========================================================================

    @Override
    public void onAttach(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onAttach");
        super.onAttach(context);

        Activity activity;

        if (context instanceof Activity){
            activity = (Activity) context;

            try {
                miniPlayerTouchedListener = (OnMiniPlayerTouchedListener) activity;
                viewInflatedListener = (OnViewInflatedListener) activity;
            } catch (ClassCastException e) {
                if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage());
                throw new ClassCastException(activity.toString() +
                        " must implement OnMiniPlayerTouchedListener|OnViewInflatedListener");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        filter = new IntentFilter();
        filter.addAction(NyaaUtils.META_CHANGED);
        filter.addAction(NyaaUtils.SERVICE_READY);
        metaChangedListener = new MetaChangedListener(this);

        handler = new Handler();
        updater = () -> {
            updateProgressBar(MusicUtils.getCurrentPosition());
            handler.postDelayed(updater, 250);
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_mini_player, container, false);
        musicTitleView = rootView.findViewById(R.id.mini_player_title);
        musicArtistView = rootView.findViewById(R.id.mini_player_artist);

        View prev = rootView.findViewById(R.id.prev_button);
        View next = rootView.findViewById(R.id.next_button);
        View up = rootView.findViewById(R.id.up_button);

        playPauseButton = rootView.findViewById(R.id.play_pause_button);

        progressBar = rootView.findViewById(R.id.mini_player_progress);


        rootView.setOnClickListener((v) -> miniPlayerTouchedListener.onMiniPlayerTouched(v));

        prev.setOnClickListener((v) -> {
            boolean wasPlaying = MusicUtils.isPlaying();
            MusicUtils.previous();
            if (wasPlaying) {
                MusicUtils.start();
            }
        });

        next.setOnClickListener((v) -> {
            boolean wasPlaying = MusicUtils.isPlaying();
            MusicUtils.next();
            if (wasPlaying) {
                MusicUtils.start();
            }
        });

        up.setOnClickListener((v) -> miniPlayerTouchedListener.onMiniPlayerTouched(v));

        playPauseButton.setOnClickListener((v) -> {
            MusicPlaybackState state = MusicUtils.getState();
            // do nothing on unknown state
            if (state == null) {
                return;
            }

            if (MusicUtils.isPlaying()) {
                MusicUtils.pause();
                handler.removeCallbacks(updater);
            } else if (state.getQueuePos() == MusicPlaybackService.UNKNOWN_POS) {
                Snackbar.make(v, R.string.snackbar_choose_track, Snackbar.LENGTH_SHORT).show();
            } else {
                MusicUtils.resume();
                handler.post(updater);
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        // notify listener
        if (viewInflatedListener != null) {
            viewInflatedListener.onViewInflated(view);
        }
    }

    @Override
    public void onResume() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onResume");
        super.onResume();

        Activity activity = getActivity();
        activity.registerReceiver(metaChangedListener, filter);

        // update ui on resume
        updateMetaUI();

        // updater
        if (MusicUtils.isPlaying()) {
            handler.post(updater);
        }
    }

    @Override
    public void onPause() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPause");
        super.onPause();

        Activity activity = getActivity();
        activity.unregisterReceiver(metaChangedListener);

        // stop updater
        handler.removeCallbacks(updater);
    }


    //=========================================================================
    // Helper functions
    //=========================================================================

    public void setVisibility(int visibility) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setVisibility");

        View rootView = getView();
        if (rootView != null) {
            rootView.setVisibility(visibility);
        }
    }

    private void updateProgressBar(int progress) {
        if (BuildConfig.DEBUG) Log.d(TAG, "updateProgressBar");

        progressBar.setProgress(progress);
    }

    private void updateProgressBarMax(int max) {
        if (BuildConfig.DEBUG) Log.d(TAG, "updateProgressBarMax");

        progressBar.setMax(max);
    }

    private void updateMetaUI() {
        if (BuildConfig.DEBUG) Log.d(TAG, "updateMetaUI");

        updatePlayPauseButton();
        updateCurrentPlayingTitle();
    }

    private void updatePlayPauseButton() {
        if (BuildConfig.DEBUG) Log.d(TAG, "updatePlayPauseButton");

        if (MusicUtils.isPlaying()) {
            playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            playPauseButton.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void updateCurrentPlayingTitle() {
        if (BuildConfig.DEBUG) Log.d(TAG, "updateCurrentPlayingTitle");

        MusicPlaybackTrack track = MusicUtils.getCurrentPlaying();
        if (track == null) {
            musicTitleView.setText(R.string.service_musicplayback_notification_message);
            musicArtistView.setText("");
            return;
        }

        // find the location from the MediaStore
        Cursor cursor = makeMusicNameCursor(track.getId());

        if (cursor == null) {
            musicTitleView.setText(R.string.service_musicplayback_notification_message);
            musicArtistView.setText("");
            return;
        }

        // more than 1 item of this id --> debug me
        if (cursor.getCount() != 1) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Found " + cursor.getCount() + " item(s)");
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                if (BuildConfig.DEBUG) Log.w(TAG, "Item: " + cursor.getString(titleColumn) +
                        " " + cursor.getString(artistColumn));
            }

            cursor.close();
            musicTitleView.setText(R.string.service_musicplayback_notification_message);
            musicArtistView.setText("");
            return;
        }

        // success
        cursor.moveToFirst();
        String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        cursor.close();

        musicTitleView.setText(name);
        musicArtistView.setText(artist);
    }

    private Cursor makeMusicNameCursor(long musicId) {
        if (BuildConfig.DEBUG) Log.d(TAG, "makeMusicNameCursor");

        ContentResolver musicResolver = getActivity().getContentResolver();

        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST };
        String selection = MediaStore.Audio.Media._ID + "=?";
        String[] selectionArgs = { String.valueOf(musicId) };
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        return musicResolver.query(musicUri, projection, selection, selectionArgs, sortOrder);
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

            if (action == null) {
                return;
            }

            switch (action) {
                case NyaaUtils.META_CHANGED:
                case NyaaUtils.SERVICE_READY:
                    reference.get().updateProgressBarMax(MusicUtils.getDuration());
                    reference.get().updateProgressBar(MusicUtils.getCurrentPosition());
                    reference.get().updateMetaUI();
                    break;
                default:
                    if (BuildConfig.DEBUG) Log.e(TAG, "Unknown action: " + action);
            }
        }
    }

    public interface OnMiniPlayerTouchedListener {
        void onMiniPlayerTouched(View view);
    }
}
