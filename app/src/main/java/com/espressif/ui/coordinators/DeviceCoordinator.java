package com.espressif.ui.coordinators;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.espressif.AppConstants;
import com.espressif.ui.managers.DeviceManagerImpl;  // Updated import path
import com.espressif.ui.models.PreferencesManager;
import com.espressif.ui.coordinators.PermissionsCoordinator;

public class DeviceCoordinator {
    private static final String TAG = "DeviceCoordinator";
    
    private final Context context;
    private final DeviceManagerImpl deviceManager;
    private final PreferencesManager preferencesManager;
    private final PermissionsCoordinator permissionsCoordinator;

    public DeviceCoordinator(Context context) {
        this.context = context;
        this.deviceManager = new DeviceManagerImpl(context);
        this.preferencesManager = new PreferencesManager(context);
        this.permissionsCoordinator = new PermissionsCoordinator(context);
    }

    public boolean isProvisioned() {
        return preferencesManager.isProvisioned();
    }

    public String getDeviceId() {
        return preferencesManager.getDeviceId();
    }

    public String getDeviceType() {
        return deviceManager.getDeviceType();
    }

    public void setDeviceType(String deviceType) {
        deviceManager.setDeviceType(deviceType);
    }

    public boolean isSecurityEnabled() {
        return deviceManager.isSecurityEnabled();
    }

    public boolean isLocationRequired() {
        return permissionsCoordinator.isLocationRequired();
    }

    public boolean isLocationEnabled() {
        return permissionsCoordinator.isLocationEnabled();
    }

    public boolean isBluetoothEnabled() {
        return permissionsCoordinator.isBluetoothEnabled();
    }

    public Intent getEnableBluetoothIntent() {
        return permissionsCoordinator.getEnableBluetoothIntent();
    }

    public void createDevice(String deviceType, boolean isSec1) {
        deviceManager.createDevice(deviceType, isSec1);
    }

    public void saveProvisioningState(String deviceId) {
        preferencesManager.saveProvisioningState(deviceId);
    }

    public void clearProvisioningState() {
        preferencesManager.clearProvisioningState();
    }

    public interface DeviceProvisioningCallback {
        void onProvisioningComplete(String deviceId);
    }
}