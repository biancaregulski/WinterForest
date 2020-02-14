package com.example.winterforest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class GameOverActivity extends AppCompatActivity {

    int score;
    TextView scoreText;
    MediaPlayer gameOverSound;
    // public static MusicService mBoundService;

    boolean soundsOnOff;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SWITCH_SOUNDS = "soundsSwitch";

    SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        mPrefs = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_MULTI_PROCESS);
        soundsOnOff = mPrefs.getBoolean(SWITCH_SOUNDS, true);

        if (soundsOnOff == true) {
            gameOverSound = MediaPlayer.create(GameOverActivity.this, R.raw.game_over);
        }
        gameOverSound.start();

        scoreText = findViewById(R.id.text_score);
        score = getIntent().getIntExtra("score", 0);
        scoreText.setText("Final Score: " + String.format("%05d", score));
    }

    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.button_restart:
                intent = new Intent(this, PlayGame.class);
                startActivity(intent);
                break;
            case R.id.button_quit:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
        }
    }
}
