package com.example.venom.bgwifiservicetest;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    BackgroundWifiService mBGWService;
    Intent mServiceIntent;
    Context ctx;
    public Context getCtx() {
        return ctx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;
        mBGWService = new BackgroundWifiService(getCtx());
        mServiceIntent = new Intent(getCtx(), mBGWService.getClass());
        if (!isMyServiceRunning(mBGWService.getClass())) {
            startService(mServiceIntent);
        }
    }

    public void startService(View view) {
        // start our service here
        Intent intent = new Intent(this, mBGWService.getClass());
        startService(intent);
    }

    public void stopService(View view) {
        Intent intent = new Intent(this, mBGWService.getClass());
        stopService(intent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }


    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();

    }
}
