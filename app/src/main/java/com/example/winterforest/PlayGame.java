package com.example.winterforest;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PlayGame extends AppCompatActivity {

    private PlayGameLayout mPlayGameLayout;
    private FrameLayout mFrameLayout;
    ImageButton pauseButton;
    TextView scoreText;
    int score;
    static boolean settingsOpened = false;

    public static MusicService mBoundService;

    MediaPlayer increasePointsSound, increaseLevelSound, failSound, loseLifeSound;

    public Button resumeButton, restartButton, settingsButton, quitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        mFrameLayout = new FrameLayout(this);
        mPlayGameLayout = new PlayGameLayout(this, display);

        score = 0;
        settingsOpened = false;

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
                onPause();
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
        scoreText.setBackgroundResource(R.drawable.border_light_blue_background);
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

    private void pauseLayout() {
        mPlayGameLayout.pause();
        if (increasePointsSound != null) {
            increasePointsSound.release();
            increasePointsSound = null;
        }
        if (increaseLevelSound != null) {
            increaseLevelSound.release();
            increaseLevelSound = null;
        }
        if (failSound != null) {
            failSound.release();
            failSound = null;
        }
        if (loseLifeSound != null) {
            loseLifeSound.release();
            loseLifeSound = null;
        }

        final Dialog dialog = new Dialog(PlayGame.this);
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
                onResume();
            }
        });

        // restart activity
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(PlayGame.this, LevelScreen.class);
                startActivity(intent);
            }
        });

        // open settings activity
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(PlayGame.this, SettingsActivity.class);
                intent.putExtra("gray_background", false);
                startActivity(intent);
                overridePendingTransition( R.anim.slide_up, R.anim.slide_down );
                settingsOpened = true;
            }
        });


        // return to main activity
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(PlayGame.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        increasePointsSound = MediaPlayer.create(PlayGame.this, R.raw.increase_points);
        increaseLevelSound = MediaPlayer.create(PlayGame.this, R.raw.increase_level);
        failSound = MediaPlayer.create(PlayGame.this, R.raw.fail);
        loseLifeSound = MediaPlayer.create(PlayGame.this, R.raw.lose_life);

        if (!settingsOpened) {
            mPlayGameLayout.resume();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public void addPoints(int num) {
        score += num;
        scoreText.setText("Score: " + String.format("%05d", score));
    }

    public int getPoints() {
        return score;
    }

    public void playSound(String sound) {
        if (sound.equals("level")) {
            increaseLevelSound.start();
        }
        else if (sound.equals("points")) {
            increasePointsSound.start();
        }
        else if (sound.equals("fail")) {
            failSound.start();
        }
        else if (sound.equals("lose")) {
            loseLifeSound.start();
        }
    }
}