package com.example.venom.bgwifiservicetest;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    BackgroundWifiService mBGWService;
    Intent mServiceIntent;
    Context ctx;
    LocationManager mLocationManager;
    GPSLocationListener mLocationListener;
    PendingIntent mPendingIntent;

    public Context getCtx() {
        return ctx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;

        LocationManager mLocationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        createBackgroundService();
        scheduleAlarms();
        createLocationListener();
        setTextInfo();
    }

    private void createBackgroundService() {
        mBGWService = new BackgroundWifiService(getCtx(), mLocationManager);
        mServiceIntent = new Intent(getCtx(), mBGWService.getClass());

        //if (!isMyServiceRunning(mBGWService.getClass())) {
        //startService(mServiceIntent);
        //}

        // these are from example at https://gist.github.com/BrandonSmith/6679223
        // there purpose is to pass data to the intent
        //notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        //notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        mPendingIntent = PendingIntent.getBroadcast(
                ctx, 0, mServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private void createLocationListener() {
        // check to see if gps is on
        mLocationListener = new GPSLocationListener(ctx);
        if (!mLocationListener.getGpsStatus()) {
            // enable GPS
        }
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);
    }

    private void scheduleAlarms() {
        long futureInMillis = SystemClock.elapsedRealtime() + 30000;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        // sets a single non repeating alarm
        // alarmManager.set(AlarmManager.ELAPSED_REALTIME, futureInMillis, pendingIntent);

        // set a repeating alarm
        alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME, futureInMillis, 60000, mPendingIntent
        );
    }

    public void setTextInfo() {
        final TextView textView = (TextView) findViewById(R.id.main_text);
        textView.setText(
                "SERIAL: " + Build.SERIAL + "\n" +
                        "MODEL: " + Build.MODEL + "\n" +
                        "ID: " + Build.ID + "\n" +
                        "Manufacture: " + Build.MANUFACTURER + "\n" +
                        "Brand: " + Build.BRAND + "\n" +
                        "Type: " + Build.TYPE + "\n" +
                        "User: " + Build.USER + "\n" +
                        "BASE: " + Build.VERSION_CODES.BASE + "\n" +
                        "INCREMENTAL: " + Build.VERSION.INCREMENTAL + "\n" +
                        "SDK:  " + Build.VERSION.SDK + "\n" +
                        "BOARD: " + Build.BOARD + "\n" +
                        "BRAND: " + Build.BRAND + "\n" +
                        "HOST: " + Build.HOST + "\n" +
                        "FINGERPRINT: "+Build.FINGERPRINT + "\n" +
                        "Version Code: " + Build.VERSION.RELEASE);

        TelephonyManager tMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        String mPhoneNumber = tMgr.getLine1Number();
        textView.setText(textView.getText() + "\n" + mPhoneNumber);

        if (mPhoneNumber == null || mPhoneNumber.equals("") || mPhoneNumber.equals("???????")) {
            mPhoneNumber = tMgr.getDeviceId();
            textView.setText(textView.getText() + "\n" + mPhoneNumber);
        }

        // add gps location data
        textView.setText(textView.getText() + "\n" + GPSLocationListener.myLocation);
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
