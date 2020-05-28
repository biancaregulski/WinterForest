package com.example.winterforest;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;

public class LevelScreen extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_screen);

        int secondsDelayed = 1;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(LevelScreen.this, PlayGameActivity.class));
                changedActivity = true;
                finish();
            }
        }, secondsDelayed * 1000);
    }

    @Override
    public void onBackPressed() {
        // do nothing, must wait for game
    }
}
