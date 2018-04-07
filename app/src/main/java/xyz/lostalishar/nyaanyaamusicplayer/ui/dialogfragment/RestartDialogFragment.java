package xyz.lostalishar.nyaanyaamusicplayer.ui.dialogfragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;
import xyz.lostalishar.nyaanyaamusicplayer.util.NyaaUtils;

/**
 * DialogFragment displaying about information
 */

public class RestartDialogFragment extends DialogFragment {
    private static final String TAG = RestartDialogFragment.class.getSimpleName();

    public static RestartDialogFragment newInstance() {
        if (BuildConfig.DEBUG) Log.d(TAG, "newInstance");

        return new RestartDialogFragment();
    }


    @Override
    public @NonNull
    Dialog onCreateDialog(Bundle onSavedInstance) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateDialog");

        // this should be called after onAttach but die if it doesn't
        final Activity activity = getActivity();
        assert activity != null;


        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        // @TODO make a cool icon
        // builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(R.string.dialog_restart_title);
        builder.setMessage(R.string.dialog_restart_message);
        builder.setPositiveButton(R.string.dialog_restart_positive, (dialogInterface, i) -> {
            MusicUtils.pause();
            NyaaUtils.triggerRebirth(getActivity());
        });
        builder.setNegativeButton(R.string.dialog_restart_negative, null);

        return builder.create();
    }
}
