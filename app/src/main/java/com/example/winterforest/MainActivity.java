package com.example.winterforest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;

import java.util.Set;

public class MainActivity extends Activity {
    private boolean mIsBound = false;
    private MusicService mServ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent music = new Intent();
        music.setClass(this,MusicService.class);
        startService(music);
    }

    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.button_play:
                intent = new Intent(this, LevelScreen.class);
                startActivity(intent);
                break;
            case R.id.button_how:
                intent = new Intent(this, HowToPlayActivity.class);
                startActivity(intent);
                break;
            case R.id.button_settings:
                intent = new Intent(this, SettingsActivity.class);
                intent.putExtra("gray_background", true);
                startActivity(intent);
                overridePendingTransition( R.anim.slide_up, R.anim.slide_down );
                break;
            // add high scores
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mServ != null) {
            mServ.pauseMusic();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mServ != null) {
            mServ.resumeMusic();
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            mServ = ((MusicService.ServiceBinder)binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    void doBindService(){
        bindService(new Intent(this,MusicService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService()
    {
        if(mIsBound)
        {
            unbindService(mServiceConnection);
            mIsBound = false;
        }
    }
}
