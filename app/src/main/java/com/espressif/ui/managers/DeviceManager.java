package com.espressif.ui.managers;

import android.content.Context;
import android.content.SharedPreferences;
import com.espressif.AppConstants;
import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPProvisionManager;

public class DeviceManager {
    private final Context context;
    private final ESPProvisionManager provisionManager;
    private final SharedPreferences preferences;

    public DeviceManager(Context context) {
        this.context = context;
        this.provisionManager = ESPProvisionManager.getInstance(context);
        this.preferences = context.getSharedPreferences(AppConstants.ESP_PREFERENCES, Context.MODE_PRIVATE);
    }

    public void createDevice(String deviceType, boolean isSec1) {
        ESPConstants.SecurityType securityType = isSec1 ? 
            ESPConstants.SecurityType.SECURITY_1 : 
            ESPConstants.SecurityType.SECURITY_0;

        switch (deviceType) {
            case AppConstants.DEVICE_TYPE_BLE:
                provisionManager.createESPDevice(ESPConstants.TransportType.TRANSPORT_BLE, securityType);
                break;
            case AppConstants.DEVICE_TYPE_SOFTAP:
                provisionManager.createESPDevice(ESPConstants.TransportType.TRANSPORT_SOFTAP, securityType);
                break;
        }
    }

    public String getDeviceType() {
        return preferences.getString(AppConstants.KEY_DEVICE_TYPES, AppConstants.DEVICE_TYPE_DEFAULT);
    }

    public boolean isSecurityEnabled() {
        return preferences.getBoolean(AppConstants.KEY_SECURITY_TYPE, true);
    }

    public void updateDeviceType(String newType) {
        preferences.edit()
            .putString(AppConstants.KEY_DEVICE_TYPES, newType)
            .apply();
    }
}