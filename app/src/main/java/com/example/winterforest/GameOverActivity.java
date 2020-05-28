package com.example.winterforest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class GameOverActivity extends BaseActivity {

    int score;
    TextView scoreText;
    MediaPlayer gameOverSound;

    boolean soundsOn;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SWITCH_SOUNDS = "soundsSwitch";

    SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        mPrefs = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_MULTI_PROCESS);
        soundsOn = mPrefs.getBoolean(SWITCH_SOUNDS, true);

        if (soundsOn == true) {
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
                changedActivity = true;
                intent = new Intent(this, PlayGameActivity.class);
                startActivity(intent);
                break;
            case R.id.button_quit:
                changedActivity = true;
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
        }
    }
    @Override
    public void onBackPressed() {
        changedActivity = true;
        super.onBackPressed();
    }
}
