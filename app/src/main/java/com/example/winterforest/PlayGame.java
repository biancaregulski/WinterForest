package com.example.winterforest;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PlayGame extends Activity {

    private PlayGameLayout mPlayGameLayout;
    private FrameLayout mFrameLayout;
    ImageButton pauseButton;
    TextView scoreText;
    int score;
    static boolean settingsOpened = false;

    public static MusicService mBoundService;

    public Button resumeButton, restartButton, settingsButton, quitButton;

    // TODO: add multiple lives
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

        Button testButton = new Button(this);
        RelativeLayout.LayoutParams testButtonParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        testButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT );
        testButtonParams.setMargins(0,30,220,0);
        testButton.setLayoutParams(testButtonParams);
        testButton.setText("Game Over");
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlayGame.this, GameOverActivity.class);
                intent.putExtra("score", 0);
                startActivity(intent);
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
        gameWidgets.addView(testButton);
        gameWidgets.addView(scoreText);
        mFrameLayout.addView(mPlayGameLayout);
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

        final Dialog dialog = new Dialog(PlayGame.this);
        // Include dialog.xml file
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
        if (!settingsOpened)
            mPlayGameLayout.resume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public void addPoints(int num) {
        score += num;
        scoreText.setText("Score: " + String.format("%05d", score));
    }
}