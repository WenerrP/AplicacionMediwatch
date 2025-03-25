package com.espressif.models;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.location.LocationManager;

public class LocationManager {
    private final Context context;
    private final android.location.LocationManager locationManager;

    public LocationManager(Context context) {
        this.context = context;
        this.locationManager = (android.location.LocationManager) 
            context.getSystemService(Activity.LOCATION_SERVICE);
    }

    public boolean isLocationRequired() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    public boolean isLocationEnabled() {
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {}

        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored) {}

        return gpsEnabled || networkEnabled;
    }
}