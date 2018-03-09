package com.example.venom.bgwifiservicetest;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
    private View mLayout;
    private static final int PERMISSION_REQUEST_READ_SMS = 0;
    private static final int PERMISSION_REQUEST_READ_PHONE_STATE = 1;
    private static final int PERMISSION_REQUEST_ACCESS_COARSE_LOCATION = 2;
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 3;

    public Context getCtx() {
        return ctx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.main_layout);
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

        int hasFineLocation = ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocation = ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocation != PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocation != PackageManager.PERMISSION_GRANTED ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.i("createLocListenerInMain", "createLocationListener: permissions not found");
            return;
        }

        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);
    }

    private void scheduleAlarms() {
        long futureInMillis = SystemClock.elapsedRealtime() + 5000;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        // sets a single non repeating alarm
        // alarmManager.set(AlarmManager.ELAPSED_REALTIME, futureInMillis, pendingIntent);

        // set a repeating alarm
        alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME, futureInMillis, 10000, mPendingIntent
        );
    }

    public void setTextInfo() {
        final TextView textView = findViewById(R.id.main_text);
        String buildText = buildBuildText();
        String phoneNumberText = buildPhoneNumberText();
        String gpsText = buildGPSLocationText();
        String textViewString = buildText + phoneNumberText + gpsText;
        textView.setText(textViewString);
    }

    private String buildGPSLocationText() {
        String ret = "GPS Location: " + GPSLocationListener.myLocation;
        return ret;
    }

    private String buildBuildText() {
         return "SERIAL: " + Build.SERIAL + "\n" +
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
                "Version Code: " + Build.VERSION.RELEASE + "\n";
    }

    private String buildPhoneNumberText() {
        TelephonyManager tMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        int hasReadSMS = ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_SMS);
        int hasReadPhoneState = ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE);
        Boolean permissionGrantedReadSMS = hasReadSMS == PackageManager.PERMISSION_GRANTED;
        Boolean permissionGrantedReadPhoneState = hasReadPhoneState == PackageManager.PERMISSION_GRANTED;

        if (permissionGrantedReadPhoneState && permissionGrantedReadSMS) {
            // Permission is already available, start camera preview
            Snackbar.make(mLayout,
                    R.string.phone_info_permission_available,
                    Snackbar.LENGTH_SHORT).show();
            String mPhoneNumber = tMgr.getLine1Number();
            if (mPhoneNumber == null || mPhoneNumber.equals("") || mPhoneNumber.equals("???????")) {
                mPhoneNumber = tMgr.getDeviceId();
            }
            String ret = "Phone Number: " + mPhoneNumber + "\n";
            return ret;
        }
        if (!permissionGrantedReadSMS) {
            requestReadSMSPermission();
        }
        if (!permissionGrantedReadPhoneState) {
            requestReadPhoneStatePermission();
        }
        return "\n";
    }

    /**
     * Requests the {@link android.Manifest.permission#READ_SMS} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private void requestReadSMSPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {
            //
            Snackbar.make(mLayout, R.string.read_sms_access_required,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_SMS},
                            PERMISSION_REQUEST_READ_SMS);
                }
            }).show();
        } else {
            Snackbar.make(mLayout, R.string.read_sms_unavailable, Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS}, PERMISSION_REQUEST_READ_SMS);
        }
    }

    /**
     * Requests the {@link android.Manifest.permission#READ_PHONE_STATE} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private void requestReadPhoneStatePermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
            //
            Snackbar.make(mLayout, R.string.read_phone_state_access_required,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_PHONE_STATE},
                            PERMISSION_REQUEST_READ_PHONE_STATE);
                }
            }).show();
        } else {
            Snackbar.make(mLayout, R.string.read_phone_state_unavailable, Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_READ_PHONE_STATE);
        }
    }

    /**
     * Requests the {@link android.Manifest.permission#ACCESS_COARSE_LOCATION} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private void requestCoarseLocationPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            //
            Snackbar.make(mLayout, R.string.coarse_location_access_required,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_REQUEST_ACCESS_COARSE_LOCATION);
                }
            }).show();
        } else {
            Snackbar.make(mLayout, R.string.coarse_location_unavailable, Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_ACCESS_COARSE_LOCATION);
        }
    }

    /**
     * Requests the {@link android.Manifest.permission#ACCESS_FINE_LOCATION} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private void requestFineLocationPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            //
            Snackbar.make(mLayout, R.string.fine_location_access_required,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
                }
            }).show();
        } else {
            Snackbar.make(mLayout, R.string.fine_location_unavailable, Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_SMS:
                askForReadSMS(requestCode, permissions, grantResults);
                break;
            case PERMISSION_REQUEST_READ_PHONE_STATE:
                askForReadPhoneState(requestCode, permissions, grantResults);
                break;
            case PERMISSION_REQUEST_ACCESS_COARSE_LOCATION:
                askForCoarseLocation(requestCode, permissions, grantResults);
                break;
            case PERMISSION_REQUEST_ACCESS_FINE_LOCATION:
                askForFineLocation(requestCode, permissions, grantResults);
                break;
            default:
                // Permission has been granted. Start camera preview Activity.
                Snackbar.make(mLayout, R.string.bad_permission_request,
                        Snackbar.LENGTH_SHORT)
                        .show();
                break;
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }

    public void askForReadSMS(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Request for camera permission.
        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission has been granted. Start camera preview Activity.
            Snackbar.make(mLayout, R.string.read_sms_permission_granted,
                    Snackbar.LENGTH_SHORT)
                    .show();
            // do something here because permission was granted
            setTextInfo();
        } else {
            // Permission request was denied.
            Snackbar.make(mLayout, R.string.read_sms_permission_denied,
                    Snackbar.LENGTH_SHORT)
                    .show();
            // do something here because permission was denied
        }
    }

    public void askForReadPhoneState(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Request for camera permission.
        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission has been granted. Start camera preview Activity.
            Snackbar.make(mLayout, R.string.read_phone_state_permission_granted,
                    Snackbar.LENGTH_SHORT)
                    .show();
            // do something here because permission was granted
            setTextInfo();
        } else {
            // Permission request was denied.
            Snackbar.make(mLayout, R.string.read_phone_state_permission_denied,
                    Snackbar.LENGTH_SHORT)
                    .show();
            // do something here because permission was denied
        }
    }

    public void askForCoarseLocation(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Request for camera permission.
        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission has been granted. Start camera preview Activity.
            Snackbar.make(mLayout, R.string.coarse_location_permission_granted,
                    Snackbar.LENGTH_SHORT)
                    .show();
            // do something here because permission was granted
            setTextInfo();
        } else {
            // Permission request was denied.
            Snackbar.make(mLayout, R.string.coarse_location_permission_denied,
                    Snackbar.LENGTH_SHORT)
                    .show();
            // do something here because permission was denied
        }
    }

    public void askForFineLocation(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Request for camera permission.
        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission has been granted. Start camera preview Activity.
            Snackbar.make(mLayout, R.string.fine_location_permission_granted,
                    Snackbar.LENGTH_SHORT)
                    .show();
            // do something here because permission was granted
            setTextInfo();
        } else {
            // Permission request was denied.
            Snackbar.make(mLayout, R.string.fine_location_permission_denied,
                    Snackbar.LENGTH_SHORT)
                    .show();
            // do something here because permission was denied
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
