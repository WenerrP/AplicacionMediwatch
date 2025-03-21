package com.espressif.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.espressif.wifi_provisioning.R;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final String PREF_NAME = "EspProvisioningPrefs";
    private static final String KEY_IS_PROVISIONED = "isProvisioned";
    private static final String KEY_DEVICE_ID = "deviceId";
    
    // Tiempo de duración del splash en milisegundos (opcional, puedes ponerlo a 0)
    private static final long SPLASH_DURATION = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Establecer un layout básico de splash o dejarlo sin contenido
        setContentView(R.layout.activity_splash);
        
        new Handler().postDelayed(() -> {
            // Verificar si ya tenemos un dispositivo aprovisionado
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            boolean isProvisioned = prefs.getBoolean(KEY_IS_PROVISIONED, false);
            String deviceId = prefs.getString(KEY_DEVICE_ID, "");

            Intent intent;
            
            if (isProvisioned && !deviceId.isEmpty()) {
                // Si ya hay un dispositivo aprovisionado, ir directamente al MQTT Dashboard
                Log.d(TAG, "Dispositivo ya aprovisionado: " + deviceId + ". Abriendo MQTT Dashboard.");
                intent = new Intent(SplashActivity.this, MqttActivity.class);
                intent.putExtra("DEVICE_ID", deviceId);
                
                // Agregar flags para evitar que el usuario vuelva atrás
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            } else {
                // Si no hay dispositivo aprovisionado, mostrar la pantalla de provisioning
                Log.d(TAG, "No hay dispositivo aprovisionado. Abriendo pantalla de provisioning.");
                intent = new Intent(SplashActivity.this, EspMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
}