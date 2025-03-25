package com.espressif.ui.managers;

import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

public class LocationManagerCompat {
    private final Context context;
    private final LocationManager locationManager;

    public LocationManagerCompat(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public boolean isLocationRequired() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    public boolean isLocationEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return locationManager.isLocationEnabled();
        } else {
            try {
                int locationMode = Settings.Secure.getInt(context.getContentResolver(), 
                        Settings.Secure.LOCATION_MODE);
                return locationMode != Settings.Secure.LOCATION_MODE_OFF;
            } catch (Settings.SettingNotFoundException e) {
                return false;
            }
        }
    }
}