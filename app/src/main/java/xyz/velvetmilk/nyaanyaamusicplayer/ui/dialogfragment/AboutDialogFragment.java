package xyz.velvetmilk.nyaanyaamusicplayer.ui.dialogfragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import xyz.velvetmilk.nyaanyaamusicplayer.R;

/**
 * Created by nydrani on 23/05/17.
 */

public class AboutDialogFragment extends DialogFragment {
    public static AboutDialogFragment newInstance() {
        return new AboutDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle onSavedInstance) {
        final Context context = getActivity();
        assert context != null;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.dialog_about_message);

        return builder.create();
    }
}
