package xyz.velvetmilk.nyaanyaamusicplayer.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import xyz.velvetmilk.nyaanyaamusicplayer.R;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }
}
