package com.espressif.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.espressif.AppConstants;

public class PreferencesManager {
    private final SharedPreferences preferences;

    public PreferencesManager(Context context) {
        this.preferences = context.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveProvisioningState(String deviceId) {
        preferences.edit()
            .putBoolean(AppConstants.KEY_IS_PROVISIONED, true)
            .putString(AppConstants.KEY_DEVICE_ID, deviceId)
            .apply();
    }

    public void clearProvisioningState() {
        preferences.edit()
            .putBoolean(AppConstants.KEY_IS_PROVISIONED, false)
            .putString(AppConstants.KEY_DEVICE_ID, "")
            .apply();
    }

    public boolean isProvisioned() {
        return preferences.getBoolean(AppConstants.KEY_IS_PROVISIONED, false);
    }

    public String getDeviceId() {
        return preferences.getString(AppConstants.KEY_DEVICE_ID, "");
    }
}