package com.espressif.ui.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.espressif.AppConstants;
import com.espressif.provisioning.ESPProvisionManager;

public class DeviceManagerImpl {
    private static final String TAG = "DeviceManagerImpl";
    
    private final Context context;
    private final ESPProvisionManager provisionManager;
    private final SharedPreferences preferences;

    public DeviceManagerImpl(Context context) {
        this.context = context;
        this.provisionManager = ESPProvisionManager.getInstance(context);
        this.preferences = context.getSharedPreferences(AppConstants.ESP_PREFERENCES, Context.MODE_PRIVATE);
    }

    public String getDeviceType() {
        return preferences.getString(AppConstants.KEY_DEVICE_TYPES, AppConstants.DEVICE_TYPE_DEFAULT);
    }

    public void setDeviceType(String deviceType) {
        preferences.edit()
                .putString(AppConstants.KEY_DEVICE_TYPES, deviceType)
                .apply();
    }

    public boolean isSecurityEnabled() {
        return preferences.getBoolean(AppConstants.KEY_SECURITY_TYPE, true);
    }

    public void createDevice(String deviceType, boolean isSec1) {
        try {
            provisionManager.createESPDevice(deviceType, isSec1);
            Log.d(TAG, "Device created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating device", e);
            throw new RuntimeException("Failed to create device", e);
        }
    }
}