package com.example.winterforest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class GameOverActivity extends AppCompatActivity {

    int score;
    TextView scoreText;
    public static MusicService mBoundService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

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
