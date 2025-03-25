package com.espressif.ui.activities;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.espressif.AppConstants;
import com.espressif.ui.managers.DeviceSearchManager;
import com.espressif.wifi_provisioning.R;
import com.espressif.ui.navigation.NavigationManager;

public class DeviceRecoveryActivity extends AppCompatActivity {
    
    private static final String TAG = "DeviceRecoveryActivity";
    private DeviceSearchManager deviceSearchManager;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_recovery);
        
        deviceSearchManager = new DeviceSearchManager(this);
        initViews();
    }

    private void initViews() {
        findViewById(R.id.btn_search_device).setOnClickListener(v -> searchExistingDevice());
    }

    private void searchExistingDevice() {
        showProgressDialog();
        
        deviceSearchManager.findDevice(new DeviceSearchManager.SearchCallback() {
            @Override
            public void onDeviceFound(String deviceId) {
                runOnUiThread(() -> {
                    dismissProgressDialog();
                    saveDeviceAndNavigate(deviceId);
                });
            }

            @Override
            public void onSearchFailed(String error) {
                runOnUiThread(() -> {
                    dismissProgressDialog();
                    showNoDeviceFoundDialog();
                });
            }
        });
    }

    private void saveDeviceAndNavigate(String deviceId) {
        SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(AppConstants.KEY_IS_PROVISIONED, true);
        editor.putString(AppConstants.KEY_DEVICE_ID, deviceId);
        editor.apply();

        // Use NavigationManager instead of direct call
        NavigationManager.goToMqttDashboard(this, deviceId);
        finish();
    }

    private void showProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_searching);
        builder.setMessage(R.string.message_searching_device);
        builder.setCancelable(false);
        
        progressDialog = builder.create();
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showNoDeviceFoundDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_no_device);
        builder.setMessage(R.string.message_no_device_found);
        
        builder.setPositiveButton(R.string.btn_setup_new, (dialog, which) -> {
            // Regresar a EspMainActivity para configurar nuevo dispositivo
            finish();
        });
        
        builder.setNeutralButton(R.string.btn_try_again, (dialog, which) -> {
            searchExistingDevice();
        });
        
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> {
            finish();
        });
        
        builder.show();
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        dismissProgressDialog();
        super.onBackPressed();
    }

    // ... otros métodos para diálogos y UI
}