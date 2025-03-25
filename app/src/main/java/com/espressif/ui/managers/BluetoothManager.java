package com.espressif.ui.managers;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

public class BluetoothManager {
    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;

    public BluetoothManager(Context context) {
        this.context = context;
        android.bluetooth.BluetoothManager bluetoothManager = 
            (android.bluetooth.BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public Intent getEnableBluetoothIntent() {
        return new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    }
}