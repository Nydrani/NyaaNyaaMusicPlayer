package xyz.lostalishar.nyaanyaamusicplayer.ui.dialogfragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.TextView;

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
    public @NonNull
    Dialog onCreateDialog(Bundle onSavedInstance) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateDialog");

        // this should be called after onAttach but die if it doesn't
        final Activity activity = getActivity();
        assert activity != null;

        SpannableString string = new SpannableString(getText(R.string.dialog_about_message));

        TextView view = new TextView(activity);
        view.setText(string);
        view.setMovementMethod(LinkMovementMethod.getInstance());
        Linkify.addLinks(view, Linkify.WEB_URLS);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(getResources().getDrawable(R.mipmap.ic_launcher, null));
        builder.setTitle(R.string.app_name);
        builder.setView(view);

        return builder.create();
    }
}
