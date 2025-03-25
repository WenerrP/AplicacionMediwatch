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

package com.espressif.ui.activities;

// Android imports
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// AndroidX imports
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

// Material Design imports
import com.google.android.material.button.MaterialButton;

// App imports
import com.espressif.AppConstants;
import com.espressif.wifi_provisioning.BuildConfig;
import com.espressif.wifi_provisioning.R;
import com.espressif.ui.animations.ViewAnimator;
import com.espressif.ui.animations.ActivityTransitionAnimator;
import com.espressif.ui.dialogs.DialogManager;
import com.espressif.ui.navigation.NavigationManager;

// Nuevos imports
import com.espressif.coordinators.DeviceCoordinator;

public class EspMainActivity extends AppCompatActivity implements DeviceCoordinator.DeviceProvisioningCallback {

    private static final String TAG = AppConstants.TAG_ESP_MAIN;

    private DeviceCoordinator coordinator;
    private MaterialButton btnAddDevice;
    private ImageView ivEsp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esp_main);

        initManagers();
        initViews();

        // Verificar si venimos del login
        boolean fromLogin = getIntent().getBooleanExtra(AppConstants.EXTRA_FROM_LOGIN, false);
        if (fromLogin) {
            startProvisioningFlow();
        } else {
            checkProvisionedStatus();
        }
    }

    private void initManagers() {
        coordinator = new DeviceCoordinator(this);
    }

    private void checkProvisionedStatus() {
        if (coordinator.isProvisioned()) {
            String deviceId = coordinator.getDeviceId();
            if (!deviceId.isEmpty()) {
                NavigationManager.goToMqttDashboard(this, deviceId);
                return;
            }
        }
        NavigationManager.goToUserType(this);
    }

    private void navigateToUserType() {
        Intent intent = new Intent(this, UserTypeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDeviceTypePreferences();
        updateDeviceIcon();
    }

    private void updateDeviceTypePreferences() {
        String deviceType = coordinator.getDeviceType();
        if (deviceType.equals("wifi")) {
            SharedPreferences.Editor editor = getSharedPreferences(AppConstants.ESP_PREFERENCES, Context.MODE_PRIVATE).edit();
            editor.putString(AppConstants.KEY_DEVICE_TYPES, AppConstants.DEVICE_TYPE_DEFAULT);
            editor.apply();
            coordinator.setDeviceType(AppConstants.DEVICE_TYPE_DEFAULT);
        }
    }

    private void updateDeviceIcon() {
        String deviceType = coordinator.getDeviceType();
        int iconResource;
        switch (deviceType) {
            case AppConstants.DEVICE_TYPE_BLE:
                iconResource = R.drawable.ic_esp_ble;
                break;
            case AppConstants.DEVICE_TYPE_SOFTAP:
                iconResource = R.drawable.ic_esp_softap;
                break;
            default:
                iconResource = R.drawable.ic_esp;
                break;
        }
        ivEsp.setImageResource(iconResource);
    }

    private void initViews() {
        ivEsp = findViewById(R.id.iv_esp);
        btnAddDevice = findViewById(R.id.btn_provision_device);
        
        setupButtonVisibility();
        setupClickListeners();
        setupAppVersion();
        setupAnimations();
    }

    private void setupButtonVisibility() {
        View arrowView = btnAddDevice.findViewById(R.id.iv_arrow);
        if (arrowView != null) {
            arrowView.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        btnAddDevice.setOnClickListener(v -> handleAddDeviceClick());
        
        findViewById(R.id.tv_recover_device).setOnClickListener(v -> {
            Intent intent = new Intent(this, DeviceRecoveryActivity.class);
            startActivity(intent);
        });
    }

    private void setupAppVersion() {
        try {
            TextView tvAppVersion = findViewById(R.id.tv_app_version);
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            tvAppVersion.setText(String.format("%s %s", getString(R.string.app_version), version));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting app version", e);
        }
    }

    private void setupAnimations() {
        ViewAnimator.playLogoAnimation(ivEsp);
        ViewAnimator.playButtonScaleAnimation(btnAddDevice);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (BuildConfig.isSettingsAllowed) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_settings, menu);
            return true;
        } else {
            menu.clear();
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppConstants.REQUEST_LOCATION) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                if (coordinator.isLocationEnabled()) {
                    addDeviceClick();
                }
            }
        }

        if (requestCode == AppConstants.REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            Toast.makeText(this, "Bluetooth is turned ON, you can provision device now.", Toast.LENGTH_LONG).show();
        }
    }

    private void handleAddDeviceClick() {
        if (coordinator.isLocationRequired() && !coordinator.isLocationEnabled()) {
            DialogManager.showLocationDialog(this, new DialogManager.DialogCallback() {
                @Override
                public void onPositiveButton() {
                    startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 
                            AppConstants.REQUEST_LOCATION);
                }
                @Override
                public void onNegativeButton() {
                    // Do nothing
                }
            });
            return;
        }
        startProvisioningFlow();
    }

    private void addDeviceClick() {
        String deviceType = coordinator.getDeviceType();
        
        if (deviceType.equals(AppConstants.DEVICE_TYPE_BLE) || 
            deviceType.equals(AppConstants.DEVICE_TYPE_BOTH)) {
            
            if (!coordinator.isBluetoothEnabled()) {
                startActivityForResult(coordinator.getEnableBluetoothIntent(), 
                    AppConstants.REQUEST_ENABLE_BT);
            } else {
                startProvisioningFlow();
            }
        } else {
            startProvisioningFlow();
        }
    }

    private void startProvisioningFlow() {
        String deviceType = coordinator.getDeviceType();
        boolean isSec1 = coordinator.isSecurityEnabled();
        int securityType = isSec1 ? 1 : 0;

        coordinator.createDevice(deviceType, isSec1);
        NavigationManager.goToProvisioning(this, deviceType, securityType);
    }

    private void askForLocation() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setMessage(R.string.dialog_msg_gps);

        // Set up the buttons
        builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), AppConstants.REQUEST_LOCATION);
            }
        });

        builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void goToBLEProvisionLandingActivity(int securityType) {

        Intent intent = new Intent(EspMainActivity.this, BLEProvisionLanding.class);
        intent.putExtra(AppConstants.KEY_SECURITY_TYPE, securityType);
        startActivity(intent);
    }

    private void goToWiFiProvisionLandingActivity(int securityType) {

        Intent intent = new Intent(EspMainActivity.this, ProvisionLanding.class);
        intent.putExtra(AppConstants.KEY_SECURITY_TYPE, securityType);
        startActivity(intent);
    }

    // Método para ser llamado cuando el aprovisionamiento se completa
    public void onProvisioningComplete(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            Log.e(TAG, AppConstants.ERROR_EMPTY_DEVICE_ID);
            Toast.makeText(this, AppConstants.ERROR_NO_DEVICE_ID, Toast.LENGTH_LONG).show();
            return;
        }
        
        Log.d(TAG, String.format(AppConstants.LOG_PROVISIONING_SUCCESS, deviceId));
        coordinator.saveProvisioningState(deviceId);
        NavigationManager.goToMqttDashboard(this, deviceId);
    }

    private void openMqttDashboard(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            Log.e(TAG, AppConstants.ERROR_INVALID_DEVICE_ID);
            Toast.makeText(this, AppConstants.ERROR_NO_DEVICE_ASSOCIATED, Toast.LENGTH_LONG).show();
            coordinator.clearProvisioningState();
            return;
        }
        
        Intent intent = new Intent(this, MqttActivity.class);
        intent.putExtra(AppConstants.EXTRA_DEVICE_ID, deviceId);
        ActivityTransitionAnimator.startActivityWithSlideAnimation(this, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        boolean fromLogin = getIntent().getBooleanExtra(AppConstants.EXTRA_FROM_LOGIN, false);
        if (fromLogin) {
            NavigationManager.backToLogin(this);
        } else {
            super.onBackPressed();
        }
    }
}
