package com.example.winterforest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private Switch musicSwitch, soundsSwitch;
    private Button submitButton, yesButton, noButton;
    private String statusMusicSwitch, statusSoundsSwitch;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SWITCH_MUSIC = "musicSwitch";
    public static final String SWITCH_SOUNDS = "soundsSwitch";

    private boolean musicOnOff, soundsOnOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // show gray background if coming from MainActivity
        if (getIntent().getExtras().getBoolean("gray_background")) {
            getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.gray));
        }

        // initiate view's
        musicSwitch = findViewById(R.id.switch_music);
        soundsSwitch = findViewById(R.id.switch_sounds);
        submitButton = findViewById(R.id.button_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
        loadData();
        updateViews();
    }

    @Override
    public void onBackPressed() {
        // if changes have been made by user, ask if they should be saved
        loadData();
        if (musicOnOff != musicSwitch.isChecked() || soundsOnOff != soundsSwitch.isChecked()) {
            /*new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
                    .setTitle("Save settings?")
                    .setMessage("Otherwise the changes will be lost.")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveData();
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();*/
            final Dialog dialog = new Dialog(SettingsActivity.this);
            // Include dialog.xml file
            dialog.setContentView(R.layout.dialog_save);
            dialog.setCancelable(false);
            dialog.show();

            yesButton = dialog.findViewById(R.id.button_yes);
            noButton = dialog.findViewById(R.id.button_no);
            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    saveData();
                    finish();
                }
            });

            // restart activity
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    finish();
                }
            });
        } else {
            finish();
        }
    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SWITCH_MUSIC, musicSwitch.isChecked());
        editor.putBoolean(SWITCH_SOUNDS, soundsSwitch.isChecked());

        editor.apply();

        Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        musicOnOff = sharedPreferences.getBoolean(SWITCH_MUSIC, false);
        soundsOnOff = sharedPreferences.getBoolean(SWITCH_SOUNDS, false);
    }

    public void updateViews() {
        musicSwitch.setChecked(musicOnOff);
        soundsSwitch.setChecked(soundsOnOff);
    }
}