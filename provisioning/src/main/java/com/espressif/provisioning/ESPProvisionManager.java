// Copyright 2020 Espressif Systems (Shanghai) PTE LTD
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.espressif.provisioning;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.espressif.provisioning.device_scanner.BleScanner;
import com.espressif.provisioning.device_scanner.WiFiScanner;
import com.espressif.provisioning.listeners.BleScanListener;
import com.espressif.provisioning.listeners.WiFiScanListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * App can use this class to provision device. It has APIs to scan devices
 * and connect with the device to get object of ESPDevice.
 */
public class ESPProvisionManager {

    private static final String TAG = "ESP:" + ESPProvisionManager.class.getSimpleName();

    private static ESPProvisionManager provision;

    private ESPDevice espDevice;
    private BleScanner bleScanner;
    private WiFiScanner wifiScanner;
    private Context context;

    /**
     * This method is used to get singleton instance of
     *
     * @param context Context
     * @return Returns
     */
    public static ESPProvisionManager getInstance(Context context) {

        if (provision == null) {
            provision = new ESPProvisionManager(context);
        }
        return provision;
    }

    private ESPProvisionManager(Context context) {
        this.context = context;
    }

    /**
     * This method is used to get ESPDevice object with given transport and security.
     *
     * @param transportType Transport type.
     * @param securityType  Security type.
     * @return Returns ESPDevice.
     */
    public ESPDevice createESPDevice(ESPConstants.TransportType transportType, ESPConstants.SecurityType securityType) {

        espDevice = new ESPDevice(context, transportType, securityType);
        return espDevice;
    }

    /**
     * This method is used to get ESPDevice object with given transport and security.
     *
     * @return Returns ESPDevice.
     */
    public ESPDevice getEspDevice() {
        return espDevice;
    }

    /**
     * This method is used to scan BLE devices.
     *
     * @param filters            The scan filters that will be used
     * @param bleScannerListener BleScanListener for scanning callbacks.
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION})
    public void searchBleEspDevices(List<ScanFilter> filters, BleScanListener bleScannerListener) {

        Log.d(TAG, "Search for BLE devices");
        bleScanner = new BleScanner(context, bleScannerListener);
        bleScanner.startScan(filters);
    }

    /**
     * This method is used to scan BLE devices.
     *
     * @param scanSettings       The scan settings that will be used
     * @param bleScannerListener BleScanListener for scanning callbacks.
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION})
    public void searchBleEspDevices(ScanSettings scanSettings, BleScanListener bleScannerListener) {

        Log.d(TAG, "Search for BLE devices");
        bleScanner = new BleScanner(context, bleScannerListener);
        bleScanner.startScan(scanSettings);
    }

    /**
     * This method is used to scan BLE devices.
     *
     * @param filters            The scan filters that will be used
     * @param scanSettings       The scan settings that will be used
     * @param bleScannerListener BleScanListener for scanning callbacks.
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION})
    public void searchBleEspDevices(List<ScanFilter> filters, ScanSettings scanSettings, BleScanListener bleScannerListener) {

        Log.d(TAG, "Search for BLE devices");
        bleScanner = new BleScanner(context, bleScannerListener);
        bleScanner.startScan(filters, scanSettings);
    }

    /**
     * This method is used to scan BLE devices.
     *
     * @param bleScannerListener BleScanListener for scanning callbacks.
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION})
    public void searchBleEspDevices(BleScanListener bleScannerListener) {

        Log.d(TAG, "Search for BLE devices");
        bleScanner = new BleScanner(context, bleScannerListener);
        bleScanner.startScan();
    }

    /**
     * This method is used to scan BLE devices with having given prefix in device name.
     *
     * @param prefix             Prefix to filter devices from device name.
     * @param bleScannerListener BleScanListener for scanning callbacks.
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION})
    public void searchBleEspDevices(String prefix, BleScanListener bleScannerListener) {

        Log.d(TAG, "Search for BLE devices");
        bleScanner = new BleScanner(context, prefix, bleScannerListener);
        bleScanner.startScan();
    }

    /**
     * This method is used to stop BLE scanning.
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION})
    public void stopBleScan() {

        if (bleScanner != null) {
            bleScanner.stopScan();
        }
    }

    /**
     * This method is used to scan Wi-Fi devices.
     *
     * @param wiFiDeviceScanListener WiFiScanListener for scanning callbacks.
     */
    @RequiresPermission(allOf = {Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE})
    public void searchWiFiEspDevices(WiFiScanListener wiFiDeviceScanListener) {

        wifiScanner = new WiFiScanner(context, wiFiDeviceScanListener);
        wifiScanner.startScan();
    }

    /**
     * This method is used to scan Wi-Fi devices with having given prefix in device name.
     *
     * @param prefix                 Prefix to filter devices from device name.
     * @param wiFiDeviceScanListener WiFiScanListener for scanning callbacks.
     */
    @RequiresPermission(allOf = {Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE})
    public void searchWiFiEspDevices(String prefix, WiFiScanListener wiFiDeviceScanListener) {

        wifiScanner = new WiFiScanner(context, prefix, wiFiDeviceScanListener);
        wifiScanner.startScan();
    }

    private ESPConstants.SecurityType setSecurityType(int security) {
        switch (security) {
            case 0:
                return ESPConstants.SecurityType.SECURITY_0;
            case 1:
                return ESPConstants.SecurityType.SECURITY_1;
            case 2:
            default:
                return ESPConstants.SecurityType.SECURITY_2;
        }
    }
}
