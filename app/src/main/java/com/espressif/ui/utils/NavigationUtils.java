package com.espressif.ui.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.espressif.AppConstants;
import com.espressif.ui.activities.BLEProvisionLanding;
import com.espressif.ui.activities.ProvisionLanding;

public class NavigationUtils {
    private static final String TAG = "NavigationUtils";

    public static void goToBLEProvisioning(Activity activity, int securityType) {
        Log.d(TAG, "Navegando a BLE Provisioning con security type: " + securityType);
        Intent intent = new Intent(activity, BLEProvisionLanding.class);
        intent.putExtra(AppConstants.KEY_SECURITY_TYPE, securityType);
        activity.startActivity(intent);
    }

    public static void goToWiFiProvisioning(Activity activity, int securityType) {
        Log.d(TAG, "Navegando a WiFi Provisioning con security type: " + securityType);
        Intent intent = new Intent(activity, ProvisionLanding.class);
        intent.putExtra(AppConstants.KEY_SECURITY_TYPE, securityType);
        activity.startActivity(intent);
    }
}