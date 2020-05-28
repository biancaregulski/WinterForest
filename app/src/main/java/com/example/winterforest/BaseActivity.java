package com.example.winterforest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/*  plays music when app starts or comes into focus
    stops music when app goes out of focus
    continues playing music throughout activities */

public class BaseActivity extends AppCompatActivity {

    protected boolean changedActivity = false;      // to prevent pausing/resuming music when changing activities
    protected boolean musicSettingOn;
    private boolean musicPlaying = false;

    SharedPreferences mPrefs;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SWITCH_MUSIC = "soundsMusic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if music setting is on
        mPrefs = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_MULTI_PROCESS);
        musicSettingOn = mPrefs.getBoolean(SWITCH_MUSIC, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (musicPlaying && !changedActivity) {
            Intent playIntent = new Intent(this, MusicService.class);
            stopService(playIntent);
            musicPlaying = false;
        }
        else {
            changedActivity = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (musicSettingOn && !changedActivity) {
            // start music
            Intent playIntent = new Intent(this, MusicService.class);
            startService(playIntent);
            musicPlaying = true;
        }
        else {
            changedActivity = false;
        }
    }

    protected void updateMusicSetting(boolean setting) {
        musicSettingOn = setting;

        if (musicSettingOn) {
            onResume();
        }
        else {
            onPause();
        }
    }
}
