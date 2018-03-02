package com.example.venom.bgwifiservicetest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Venom on 2/28/2018.
 */

public class BackgroundWifiServiceRestartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(BackgroundWifiServiceRestartReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
        context.startService(new Intent(context, BackgroundWifiService.class));;
    }
}
