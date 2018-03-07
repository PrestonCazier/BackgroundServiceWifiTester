package com.example.venom.bgwifiservicetest;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * Created by Venom on 3/7/2018.
 */

public class GPSLocationListener implements LocationListener {
    Context baseContext;

    // we need some kind of variable to store GPS location
    public static String myLocation = "";

    // we need a function to retrieve that location
    public static String getMyLocation() {
        return myLocation;
    }

    GPSLocationListener(Context ctx)
    {
        baseContext = ctx;
    }

    /*----Method to Check GPS is enable or disable ----- */
    public Boolean getGpsStatus() {
        ContentResolver contentResolver = baseContext.getContentResolver();

        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);
        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(
                baseContext,
                "Location changed: Lat: " + location.getLatitude() + " Lng: "
                        + location.getLongitude(), Toast.LENGTH_SHORT).show();
        String longitude = "Longitude: " + location.getLongitude();
        Log.v(TAG, longitude);
        String latitude = "Latitude: " + location.getLatitude();
        Log.v(TAG, latitude);

        /*------- To get city name from coordinates -------- */
        String cityName = null;
        Geocoder gcd = new Geocoder(baseContext, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
            if (addresses.size() > 0) {
                System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        myLocation = longitude + "\n" + latitude + "\n\nMy Current City is: "
                + cityName;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void setGPSStatus(Boolean status)
    {
        Intent intent=new Intent("android.location.GPS_ENABLED_CHANGE");
        intent.putExtra("enabled", status);
        //sendBroadcast(intent);
    }
}
