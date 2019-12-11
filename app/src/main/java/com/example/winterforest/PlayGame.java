package com.example.winterforest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PlayGame extends Activity {

    private PlayGameLayout mPlayGameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        mPlayGameLayout = new PlayGameLayout(this, display);
        setContentView(mPlayGameLayout);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayGameLayout.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPlayGameLayout.resume();
    }
}