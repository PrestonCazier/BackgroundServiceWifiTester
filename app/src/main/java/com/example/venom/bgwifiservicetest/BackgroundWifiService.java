package com.example.venom.bgwifiservicetest;

import android.Manifest;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;
import android.os.Process;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Venom on 2/23/2018.
 */

public class BackgroundWifiService extends Service {

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            boolean mobileDataEnabled = false;  // initially assume disabled

            ConnectivityManager cm = (ConnectivityManager) getBaseContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            //showToast("Starting IntentService");

            try {
                Class cmClass = Class.forName(cm.getClass().getName());
                Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");

                method.setAccessible(true);
                mobileDataEnabled = (Boolean) method.invoke(cm);
                String message = "mobile data is " + mobileDataEnabled;
                showToast(message);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            showToast("Finishing IntentService");

            stopSelf(msg.arg1);
        }
    }

    int mStartMode = START_STICKY;
    IBinder mBinder = null;
    boolean mAllowRebind = false;
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    int counter = 0;
    private Timer timer;
    private TimerTask timerTask;
    long oldTime = 0;
    GPSLocationListener mLocationListener;
    LocationManager mLocationManager;
    Context mBaseContext;

    public BackgroundWifiService() {
        super();
    }

    public BackgroundWifiService(Context applicationContext, LocationManager locManager) {
        super();
        mLocationManager = locManager;
        mBaseContext = applicationContext;
        createLocationListener();
        Log.i("HERE", "here I am!");
    }

    private void createLocationListener() {
        // check to see if gps is on
        mLocationListener = new GPSLocationListener(mBaseContext);
        if (!mLocationListener.getGpsStatus()) {
            // enable GPS
        }

        int hasFineLocation = ActivityCompat.checkSelfPermission(mBaseContext, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocation = ActivityCompat.checkSelfPermission(mBaseContext, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocation != PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocation != PackageManager.PERMISSION_GRANTED ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);
    }

    /**
     * The service is starting, due to a call to startService()
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        //startTimer();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        return mStartMode;
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.

        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    /**
     * A client is binding to the service with bindService()
     * @param intent
     * @return We don't provide binding, so return null
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Called when all clients have unbound with unbindService()
     * @param intent
     * @return We don't allow binding so we return false to prevent rebinding
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    /**
     * Called when a client is binding to the service with bindService()
     * @param intent
     */
    @Override
    public void onRebind(Intent intent){
    }

    /**
     * Called when the service is no longer used and is destroyed
     */
    @Override
    public void onDestroy() {
        showToast("background wifi service was destroyed");
        super.onDestroy();

        //Intent broadcastIntent = new Intent("uk.ac.shef.oak.ActivityRecognition.RestartSensor");
        //sendBroadcast(broadcastIntent);
        //stopTimerTask();
    }

    protected void showToast(final String msg){
        //gets the main thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                // run this code in the main thread
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  "+ (counter++));
            }
        };
    }

    /**
     * not needed
     */
    public void stopTimerTask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
