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

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.espressif.AppConstants;
import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.ui.utils.AnimationUtils;
import com.espressif.ui.utils.LocationUtils;
import com.espressif.ui.utils.MqttDeviceFinder;
import com.espressif.wifi_provisioning.BuildConfig;
import com.espressif.wifi_provisioning.R;
import com.google.android.material.button.MaterialButton;
import com.espressif.ui.utils.NavigationUtils;
import com.espressif.ui.utils.DialogUtils;

public class EspMainActivity extends AppCompatActivity {

    private static final String TAG = EspMainActivity.class.getSimpleName();
    private static final String PREF_NAME = "EspProvisioningPrefs";
    private static final String KEY_IS_PROVISIONED = "isProvisioned";
    private static final String KEY_DEVICE_ID = "deviceId";

    // Request codes
    private static final int REQUEST_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private ESPProvisionManager provisionManager;
    private MaterialButton btnAddDevice;
    private ImageView ivEsp;
    private SharedPreferences sharedPreferences;
    private String deviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esp_main);

        // Inicializar sharedPreferences
        sharedPreferences = getSharedPreferences(AppConstants.ESP_PREFERENCES, Context.MODE_PRIVATE);
        
        // Inicializar provisionManager
        provisionManager = ESPProvisionManager.getInstance(getApplicationContext());
        
        // Inicializar deviceType con valor predeterminado
        deviceType = sharedPreferences.getString(AppConstants.KEY_DEVICE_TYPES, AppConstants.DEVICE_TYPE_DEFAULT);
        
        // Inicializar vistas de la UI
        initViews();
        
        // Verificar si el usuario ya está aprovisionado
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isProvisioned = prefs.getBoolean(KEY_IS_PROVISIONED, false);
        String deviceId = prefs.getString(KEY_DEVICE_ID, "");

        if (isProvisioned && deviceId != null && !deviceId.isEmpty()) {
            // Redirigir al dashboard MQTT
            openMqttDashboard(deviceId);
        } else {
            // Redirigir a la pantalla de selección de tipo de usuario
            Intent intent = new Intent(EspMainActivity.this, UserTypeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        deviceType = sharedPreferences.getString(AppConstants.KEY_DEVICE_TYPES, AppConstants.DEVICE_TYPE_DEFAULT);
        if (deviceType.equals("wifi")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(AppConstants.KEY_DEVICE_TYPES, AppConstants.DEVICE_TYPE_DEFAULT);
            editor.apply();
        }

        deviceType = sharedPreferences.getString(AppConstants.KEY_DEVICE_TYPES, AppConstants.DEVICE_TYPE_DEFAULT);
        if (deviceType.equals(AppConstants.DEVICE_TYPE_BLE)) {
            ivEsp.setImageResource(R.drawable.ic_esp_ble);
        } else if (deviceType.equals(AppConstants.DEVICE_TYPE_SOFTAP)) {
            ivEsp.setImageResource(R.drawable.ic_esp_softap);
        } else {
            ivEsp.setImageResource(R.drawable.ic_esp);
        }
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

        if (requestCode == REQUEST_LOCATION) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                if (isLocationEnabled()) {
                    addDeviceClick();
                }
            }
        }

        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            Toast.makeText(this, "Bluetooth is turned ON, you can provision device now.", Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {

        ivEsp = findViewById(R.id.iv_esp);
        btnAddDevice = findViewById(R.id.btn_provision_device);
        
        // Verificar que el elemento exista antes de acceder
        View arrowView = btnAddDevice.findViewById(R.id.iv_arrow);
        if (arrowView != null) {
            arrowView.setVisibility(View.GONE);
        }
        
        btnAddDevice.setOnClickListener(addDeviceBtnClickListener);

        TextView tvAppVersion = findViewById(R.id.tv_app_version);

        String version = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String appVersion = getString(R.string.app_version) + " - v" + version;
        tvAppVersion.setText(appVersion);

        // Usar la clase utilitaria para las animaciones
        AnimationUtils.animateLogo(ivEsp);
        AnimationUtils.animateButton(btnAddDevice);
        
        // Botón para recuperar dispositivo ya conectado
        TextView tvRecoverDevice = findViewById(R.id.tv_recover_device);
        tvRecoverDevice.setOnClickListener(v -> {
            // Mostrar diálogo de progreso
            AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setTitle("Buscando dispositivos")
                .setMessage("Buscando dispositivos MediWatch ya conectados a la red...")
                .setCancelable(false)
                .create();
            progressDialog.show();
            
            // Ejecutar búsqueda en segundo plano
            new Thread(() -> {
                try {
                    // Usar la nueva clase utilitaria
                    boolean deviceFound = MqttDeviceFinder.findDevice();
                    
                    // El resto del código permanece igual
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        
                        if (deviceFound) {
                            Toast.makeText(this, "¡Dispositivo encontrado! Conectando...", Toast.LENGTH_SHORT).show();
                            
                            // Redireccionar a MqttActivity
                            String deviceId = "mediwatch_" + System.currentTimeMillis(); // ID genérico, la autenticación es por tópico
                            
                            // Guardar en SharedPreferences
                            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean(KEY_IS_PROVISIONED, true);
                            editor.putString(KEY_DEVICE_ID, deviceId);
                            editor.apply();
                            
                            // Abrir dashboard MQTT - Esto inicia MqttActivity
                            openMqttDashboard(deviceId);
                        } else {
                            showNoDeviceFoundDialog();
                        }
                    });
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error buscando dispositivos: " + e.getMessage(), e);
                    
                    // Actualizar UI en hilo principal
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });
    }

    // Reemplazar con este método unificado
    private void openMqttDashboard(String deviceId) {
        // Verificar una vez más que tengamos un deviceId válido
        if (deviceId == null || deviceId.isEmpty()) {
            Log.e(TAG, "Error: Intento de abrir MQTT Dashboard sin ID de dispositivo");
            Toast.makeText(this, "Error: No hay dispositivo asociado", Toast.LENGTH_LONG).show();
            
            // Limpiar estado para forzar el aprovisionamiento
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_IS_PROVISIONED, false);
            editor.putString(KEY_DEVICE_ID, "");
            editor.apply();
            return;
        }
        
        // Ahora sí, iniciar MqttActivity con el deviceId
        Intent intent = new Intent(EspMainActivity.this, MqttActivity.class);
        intent.putExtra("DEVICE_ID", deviceId);
        startActivity(intent);
        
        // Añadir animación de transición
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    
        finish();
    }

    View.OnClickListener addDeviceBtnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                if (!isLocationEnabled()) {
                    askForLocation();
                    return;
                }
            }
            addDeviceClick();
        }
    };

    private void addDeviceClick() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            if (!isLocationEnabled()) {
                askForLocation();
                return;
            }
        }

        if (deviceType.equals(AppConstants.DEVICE_TYPE_BLE) || deviceType.equals(AppConstants.DEVICE_TYPE_BOTH)) {

            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bleAdapter = bluetoothManager.getAdapter();

            if (!bleAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                startProvisioningFlow();
            }
        } else {
            startProvisioningFlow();
        }
    }

    private void startProvisioningFlow() {

        deviceType = sharedPreferences.getString(AppConstants.KEY_DEVICE_TYPES, AppConstants.DEVICE_TYPE_DEFAULT);
        final boolean isSec1 = sharedPreferences.getBoolean(AppConstants.KEY_SECURITY_TYPE, true);
        Log.d(TAG, "Device Types : " + deviceType);
        Log.d(TAG, "isSec1 : " + isSec1);
        int securityType = 0;
        if (isSec1) {
            securityType = 1;
        }

        if (deviceType.equals(AppConstants.DEVICE_TYPE_BLE)) {

            if (isSec1) {
                provisionManager.createESPDevice(ESPConstants.TransportType.TRANSPORT_BLE, ESPConstants.SecurityType.SECURITY_1);
            } else {
                provisionManager.createESPDevice(ESPConstants.TransportType.TRANSPORT_BLE, ESPConstants.SecurityType.SECURITY_0);
            }
            goToBLEProvisionLandingActivity(securityType);

        } else if (deviceType.equals(AppConstants.DEVICE_TYPE_SOFTAP)) {

            if (isSec1) {
                provisionManager.createESPDevice(ESPConstants.TransportType.TRANSPORT_SOFTAP, ESPConstants.SecurityType.SECURITY_1);
            } else {
                provisionManager.createESPDevice(ESPConstants.TransportType.TRANSPORT_SOFTAP, ESPConstants.SecurityType.SECURITY_0);
            }
            goToWiFiProvisionLandingActivity(securityType);

        } else {

            final String[] deviceTypes = {"BLE", "SoftAP"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle(R.string.dialog_msg_device_selection);
            final int finalSecurityType = securityType;
            builder.setItems(deviceTypes, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int position) {

                    switch (position) {
                        case 0:

                            if (isSec1) {
                                provisionManager.createESPDevice(ESPConstants.TransportType.TRANSPORT_BLE, ESPConstants.SecurityType.SECURITY_1);
                            } else {
                                provisionManager.createESPDevice(ESPConstants.TransportType.TRANSPORT_BLE, ESPConstants.SecurityType.SECURITY_0);
                            }
                            goToBLEProvisionLandingActivity(finalSecurityType);
                            break;

                        case 1:

                            if (isSec1) {
                                provisionManager.createESPDevice(ESPConstants.TransportType.TRANSPORT_SOFTAP, ESPConstants.SecurityType.SECURITY_1);
                            } else {
                                provisionManager.createESPDevice(ESPConstants.TransportType.TRANSPORT_SOFTAP, ESPConstants.SecurityType.SECURITY_0);
                            }
                            goToWiFiProvisionLandingActivity(finalSecurityType);
                            break;
                    }
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    // Reemplazar el método askForLocation() original con:
    private void askForLocation() {
        LocationUtils.askForLocation(this, () -> {
            // Este callback se ejecuta cuando el usuario abre la configuración de ubicación
            Log.d(TAG, "Usuario abrió configuración de ubicación");
        });
    }

    // Reemplazar el método isLocationEnabled() original con:
    private boolean isLocationEnabled() {
        return LocationUtils.isLocationEnabled(getApplicationContext());
    }

    private void goToBLEProvisionLandingActivity(int securityType) {
        NavigationUtils.goToBLEProvisioning(this, securityType);
    }

    private void goToWiFiProvisionLandingActivity(int securityType) {
        NavigationUtils.goToWiFiProvisioning(this, securityType);
    }

    // Método para ser llamado cuando el aprovisionamiento se completa
    public void onProvisioningComplete(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            Log.e(TAG, "Error: ID de dispositivo vacío después del aprovisionamiento");
            Toast.makeText(this, "Error: No se recibió ID de dispositivo", Toast.LENGTH_LONG).show();
            return;
        }
        
        Log.d(TAG, "Aprovisionamiento completado exitosamente. ID: " + deviceId);
        
        // Guardar el estado de aprovisionamiento
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_PROVISIONED, true);
        editor.putString(KEY_DEVICE_ID, deviceId);
        editor.apply();
        
        // Abrir el MQTT Dashboard
        openMqttDashboard(deviceId);
    }

    /**
     * Muestra diálogo cuando no se encuentra ningún dispositivo
     */
    private void showNoDeviceFoundDialog() {
        DialogUtils.showNoDeviceFoundDialog(this, new DialogUtils.DialogActionCallback() {
            @Override
            public void onPositiveAction() {
                addDeviceClick();
            }

            @Override
            public void onNeutralAction() {
                TextView tvRecoverDevice = findViewById(R.id.tv_recover_device);
                if (tvRecoverDevice != null) {
                    tvRecoverDevice.performClick();
                }
            }
        });
    }
}
