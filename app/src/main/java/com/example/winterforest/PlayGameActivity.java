package com.example.winterforest;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PlayGameActivity extends BaseActivity {

    private PlayGameLayout mPlayGameLayout;
    private FrameLayout mFrameLayout;
    ImageButton pauseButton;
    TextView scoreText;
    Dialog dialog;
    int score;
    static boolean settingsOpened = false, pauseMenuOpened = false;

    boolean soundsOn;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SWITCH_SOUNDS = "soundsSwitch";

    MediaPlayer increasePointsSound, failSound, loseLifeSound;

    public Button resumeButton, restartButton, settingsButton, quitButton;

    SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        mFrameLayout = new FrameLayout(this);
        mPlayGameLayout = new PlayGameLayout(this, display);

        score = 0;
        settingsOpened = false;

        mPrefs = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_MULTI_PROCESS);

        // display pause button
        RelativeLayout gameWidgets = new RelativeLayout(this);
        pauseButton = new ImageButton(this);
        pauseButton.setBackgroundResource(R.drawable.pause);
        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT );
        buttonParams.setMargins(0,30,30,0);
        pauseButton.setLayoutParams(buttonParams);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPauseMenu();
            }
        });

        // display score box
        scoreText = new TextView(this);
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        textParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT );
        textParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM );
        textParams.setMargins(0,0,30,30);
        scoreText.setGravity(Gravity.RIGHT|Gravity.BOTTOM);
        scoreText.setLayoutParams(textParams);
        scoreText.setPadding(30, 10, 30, 10);
        scoreText.setBackgroundResource(R.drawable.border_light_blue_background2);
        scoreText.setText("Score: " + String.format("%05d", score));
        scoreText.setTypeface(null, Typeface.BOLD);
        scoreText.setTextSize(20);
        scoreText.setTextColor(Color.BLACK);

        gameWidgets.addView(pauseButton);
        gameWidgets.addView(scoreText);
        mFrameLayout.addView(mPlayGameLayout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View playLayout = inflater.inflate(R.layout.activity_play, null, false);
        mFrameLayout.addView(playLayout);
        mFrameLayout.addView(gameWidgets);

        setContentView(mFrameLayout);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseLayout();
    }

    @Override
    public void onBackPressed() {
        changedActivity = true;
        super.onBackPressed();
    }

    private void pauseLayout() {
        if (!pauseMenuOpened) {
            mPlayGameLayout.pause();
        }
        if (soundsOn) {
            if (increasePointsSound != null) {
                increasePointsSound.release();
                increasePointsSound = null;
            }
            if (failSound != null) {
                failSound.release();
                failSound = null;
            }
            if (loseLifeSound != null) {
                loseLifeSound.release();
                loseLifeSound = null;
            }
        }
    }

    private void openPauseMenu() {
        pauseMenuOpened = true;
        mPlayGameLayout.pause();
        dialog = new Dialog(PlayGameActivity.this);
        dialog.setContentView(R.layout.dialog_pause);
        dialog.setCancelable(false);
        dialog.show();

        resumeButton = dialog.findViewById(R.id.button_resume);
        restartButton = dialog.findViewById(R.id.button_restart);
        settingsButton = dialog.findViewById(R.id.button_settings);
        quitButton = dialog.findViewById(R.id.button_quit);

        // close dialog and resume game
        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                settingsOpened = false;
                pauseMenuOpened = false;
                onResume();
            }
        });

        // restart activity
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                changedActivity = true;
                pauseMenuOpened = false;
                Intent intent = new Intent(PlayGameActivity.this, LevelScreen.class);
                startActivity(intent);
            }
        });

        // open settings activity
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                changedActivity = true;
                Intent intent = new Intent(PlayGameActivity.this, SettingsActivity.class);
                intent.putExtra("gray_background", false);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                settingsOpened = true;
            }
        });


        // return to main activity
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changedActivity = true;
                dialog.dismiss();
                Intent intent = new Intent(PlayGameActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        if (pauseMenuOpened) {
            dialog.dismiss();
        }
        super.onResume();
        soundsOn = mPrefs.getBoolean(SWITCH_SOUNDS, true);
        if (soundsOn == true) {
            increasePointsSound = MediaPlayer.create(PlayGameActivity.this, R.raw.increase_points);
            failSound = MediaPlayer.create(PlayGameActivity.this, R.raw.fail);
            loseLifeSound = MediaPlayer.create(PlayGameActivity.this, R.raw.lose_life);
        }
        mPlayGameLayout.resume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public void gameOver() {
        changedActivity = true;
        Intent intent = new Intent().setClass(PlayGameActivity.this, GameOverActivity.class);
        intent.putExtra("score", score);
        startActivity(intent);
    }

    public void addPoints(int num) {
        score += num;
        scoreText.setText("Score: " + String.format("%05d", score));
    }

    public int getPoints() {
        return score;
    }

    public void playSound(String sound) {
        if (soundsOn == true) {

            if (sound.equals("points")) {
                if (increasePointsSound != null) {
                    increasePointsSound.start();
                }
            }
            else if (sound.equals("fail")) {
                if (increasePointsSound != null) {
                    failSound.start();
                }
            }
            else if (sound.equals("lose")) {
                if (increasePointsSound != null) {
                    loseLifeSound.start();
                }
            }
        }
    }
}