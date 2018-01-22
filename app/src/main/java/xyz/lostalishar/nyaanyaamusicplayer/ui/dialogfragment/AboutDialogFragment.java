package xyz.lostalishar.nyaanyaamusicplayer.ui.dialogfragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;

/**
 * DialogFragment displaying about information
 */

public class AboutDialogFragment extends DialogFragment {
    private static final String TAG = AboutDialogFragment.class.getSimpleName();

    public static AboutDialogFragment newInstance() {
        if (BuildConfig.DEBUG) Log.d(TAG, "newInstance");

        return new AboutDialogFragment();
    }


    @Override
    public Dialog onCreateDialog(Bundle onSavedInstance) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateDialog");

        // this should be called after onAttach but die if it doesn't
        final Context context = getActivity();
        assert context != null;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.dialog_about_message);

        return builder.create();
    }
}
