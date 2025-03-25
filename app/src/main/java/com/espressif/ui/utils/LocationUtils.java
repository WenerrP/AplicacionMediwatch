package com.espressif.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.espressif.wifi_provisioning.R;

public class LocationUtils {
    private static final String TAG = "LocationUtils";
    private static final int REQUEST_LOCATION = 1;

    public interface LocationCallback {
        void onLocationSettingsOpened();
    }

    public static void askForLocation(Activity activity, LocationCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true);
        builder.setMessage(R.string.dialog_msg_gps);

        builder.setPositiveButton(R.string.btn_ok, (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activity.startActivityForResult(intent, REQUEST_LOCATION);
            if (callback != null) {
                callback.onLocationSettingsOpened();
            }
        });

        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public static boolean isLocationEnabled(Context context) {
        boolean gps_enabled = false;
        boolean network_enabled = false;
        LocationManager lm = (LocationManager) context.getSystemService(Activity.LOCATION_SERVICE);

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            Log.e(TAG, "Error checking GPS provider", ex);
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Log.e(TAG, "Error checking Network provider", ex);
        }

        Log.d(TAG, "GPS Enabled: " + gps_enabled + ", Network Enabled: " + network_enabled);
        return gps_enabled || network_enabled;
    }
}