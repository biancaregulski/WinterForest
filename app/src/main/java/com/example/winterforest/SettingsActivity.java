package com.example.winterforest;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends BaseActivity {

    private Switch musicSwitch;
    private Switch soundsSwitch;
    private Button submitButton, yesButton, noButton;


    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SWITCH_MUSIC = "soundsMusic";
    public static final String SWITCH_SOUNDS = "soundsSwitch";

    private boolean musicOn;
    private boolean soundsOn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // show gray background if coming from MainActivity
        if (getIntent().getExtras().getBoolean("gray_background")) {
            getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.gray));
        }

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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        // if changes have been made by user, ask if they should be saved
        loadData();
        if (musicOn != musicSwitch.isChecked() || soundsOn != soundsSwitch.isChecked()) {
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
        }
        else {
            finish();
        }
        changedActivity = true;
    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SWITCH_MUSIC, musicSwitch.isChecked());
        editor.putBoolean(SWITCH_SOUNDS, soundsSwitch.isChecked());

        editor.apply();
        editor.commit();

        updateMusicSetting(musicSwitch.isChecked());

        Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_MULTI_PROCESS);
        musicOn = sharedPreferences.getBoolean(SWITCH_MUSIC, true);
        soundsOn = sharedPreferences.getBoolean(SWITCH_SOUNDS, true);
    }

    public void updateViews() {
        soundsSwitch.setChecked(soundsOn);
        musicSwitch.setChecked(musicOn);
    }
}