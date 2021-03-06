package com.example.winterforest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

public class MainActivity extends BaseActivity {

    private SnowLayout mSnowLayout;
    private FrameLayout mFrameLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        mFrameLayout = new FrameLayout(this);
        mSnowLayout = new SnowLayout(this, display);
        mFrameLayout.addView(mSnowLayout);                      // displays snow animations

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.activity_main, null, false);
        mFrameLayout.addView(v);                // adds rest of layout (text, image views, buttons)

        setContentView(mFrameLayout);
    }


    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.button_play:              // start game
                intent = new Intent(this, LevelScreen.class);
                startActivity(intent);
                changedActivity = true;
                break;
            case R.id.button_how:               // go to how to play screen
                intent = new Intent(this, HowToPlayActivity.class);
                startActivity(intent);
                changedActivity = true;
                break;
            case R.id.button_settings:          // go to settings screen
                intent = new Intent(this, SettingsActivity.class);
                intent.putExtra("gray_background", true);
                startActivity(intent);
                changedActivity = true;
                overridePendingTransition( R.anim.slide_up, R.anim.slide_down );
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSnowLayout.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSnowLayout.resume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
