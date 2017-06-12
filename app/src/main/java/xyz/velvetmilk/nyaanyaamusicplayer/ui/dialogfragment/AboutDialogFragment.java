package xyz.velvetmilk.nyaanyaamusicplayer.ui.dialogfragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import xyz.velvetmilk.nyaanyaamusicplayer.BuildConfig;
import xyz.velvetmilk.nyaanyaamusicplayer.R;

/**
 * Created by nydrani on 23/05/17.
 *
 * Dialog that show the about fragment ezpz
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

        // this should be called after onAttach but die if it doesnt
        final Context context = getActivity();
        assert context != null;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.dialog_about_message);

        return builder.create();
    }
}
