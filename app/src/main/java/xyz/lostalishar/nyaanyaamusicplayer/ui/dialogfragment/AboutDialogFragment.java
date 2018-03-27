package xyz.lostalishar.nyaanyaamusicplayer.ui.dialogfragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Random;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;

/**
 * DialogFragment displaying about information
 */

public class AboutDialogFragment extends DialogFragment {
    private static final String TAG = AboutDialogFragment.class.getSimpleName();

    private Random random;

    public static AboutDialogFragment newInstance() {
        if (BuildConfig.DEBUG) Log.d(TAG, "newInstance");

        return new AboutDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        random = new Random();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.dialog_fragment_about, container, false);

        TextView message = rootView.findViewById(R.id.about_message);
        message.setText("DANK MEMEMEMEMEEMEMEM");
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int num = random.nextInt(3);
                if (num % 3 == 0) {
                    view.setBackgroundColor(getResources().getColor(R.color.green));
                } else if (num % 3 == 1) {
                    view.setBackgroundColor(getResources().getColor(R.color.blue));
                } else if (num % 3 == 2) {
                    view.setBackgroundColor(getResources().getColor(R.color.red));
                }
            }
        });

        return rootView;
    }
}
