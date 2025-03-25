package com.espressif.ui.coordinators;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.espressif.ui.managers.LocationManagerCompat;
import com.espressif.ui.managers.BluetoothManagerCompat;

public class PermissionsCoordinator {
    private static final String TAG = "PermissionsCoordinator";
    
    private final Context context;
    private final LocationManagerCompat locationManager;
    private final BluetoothManagerCompat bluetoothManager;

    public PermissionsCoordinator(Context context) {
        this.context = context;
        this.locationManager = new LocationManagerCompat(context);
        this.bluetoothManager = new BluetoothManagerCompat(context);
    }

    public boolean isLocationRequired() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    public boolean isLocationEnabled() {
        return locationManager.isLocationEnabled();
    }

    public boolean isBluetoothEnabled() {
        return bluetoothManager.isBluetoothEnabled();
    }

    public Intent getEnableBluetoothIntent() {
        return bluetoothManager.getEnableBluetoothIntent();
    }

    public boolean checkAllPermissions() {
        boolean hasLocation = !isLocationRequired() || isLocationEnabled();
        boolean hasBluetooth = !bluetoothManager.isRequired() || isBluetoothEnabled();
        return hasLocation && hasBluetooth;
    }

    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied(String permission);
    }
}