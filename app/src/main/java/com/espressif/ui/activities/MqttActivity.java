package com.espressif.ui.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import android.content.Intent;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;

import com.espressif.ui.adapters.LedControlAdapter;
import com.espressif.ui.adapters.MqttMessageAdapter;
import com.espressif.wifi_provisioning.R;
import com.espressif.AppConstants;
import com.espressif.ui.models.MqttHandler;
import com.espressif.ui.models.MqttMessage;
import com.espressif.ui.models.HeartbeatHandler;

public class MqttActivity extends AppCompatActivity implements HeartbeatHandler.HeartbeatCallback, 
        MqttMessageAdapter.MessageCallback {

    private static final String TAG = AppConstants.TAG_MQTT_ACTIVITY;
    
    private MqttHandler mqttHandler;
    private HeartbeatHandler heartbeatHandler;
    private String deviceId;
    private TextView textViewReceived;
    private TextView textViewConnectionStatus;
    private EditText editTextMessage;
    private View connectionIndicator;
    private MqttMessageAdapter messageAdapter;
    private LedControlAdapter ledControlAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt);

        if (!initializeDeviceId()) {
            return;
        }

        initializeViews();
        setupMqttConnection();
        setupHeartbeatHandler();
        setupAdapters();
        setupButtonListeners();
    }

    private boolean initializeDeviceId() {
        deviceId = getIntent().getStringExtra(AppConstants.EXTRA_DEVICE_ID);
        
        if (deviceId == null) {
            SharedPreferences prefs = getSharedPreferences(AppConstants.PREFS_NAME, MODE_PRIVATE);
            boolean isProvisioned = prefs.getBoolean(AppConstants.KEY_IS_PROVISIONED, false);
            deviceId = prefs.getString(AppConstants.KEY_DEVICE_ID, "");
            
            if (!isProvisioned || deviceId.isEmpty()) {
                handleNoProvisionedDevice();
                return false;
            }
        }
        return true;
    }

    private void initializeViews() {
        // Establecer título personalizado en la barra de acción
        setTitle("MediWatch MQTT");
        
        // Si se está utilizando Toolbar en lugar de ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("MediWatch MQTT");
        }

        // Inicializar vistas
        textViewReceived = findViewById(R.id.textViewReceived);
        textViewConnectionStatus = findViewById(R.id.textViewConnectionStatus);
        editTextMessage = findViewById(R.id.editTextMessage);
        connectionIndicator = findViewById(R.id.connectionIndicator);
        Button buttonSend = findViewById(R.id.buttonSend);
        
        // Agregar botones específicos para LEDs
        Button buttonLedA = findViewById(R.id.buttonLedA);
        Button buttonLedB = findViewById(R.id.buttonLedB);
        Button buttonLedC = findViewById(R.id.buttonLedC);

        // Actualizar información de tópicos en la UI
        TextView textViewTopicInfo = findViewById(R.id.textViewTopicInfo);
        textViewTopicInfo.setText("Tópico de suscripción: " + AppConstants.MQTT_TOPIC_STATUS +
                "\nTópico de publicación: " + AppConstants.MQTT_TOPIC_COMMANDS +
                "\nTópico de heartbeat: " + AppConstants.MQTT_TOPIC_HEARTBEAT);
    }

    private void setupMqttConnection() {
        try {
            mqttHandler = new MqttHandler(getApplicationContext(), new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    updateBrokerConnectionUI("Desconectado", android.R.color.holo_red_dark);
                    reconnectToMqttBroker();
                }

                @Override
                public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) 
                        throws Exception {
                    handleIncomingMessage(topic, new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Mensaje entregado correctamente");
                }
            });

            mqttHandler.connect();
            mqttHandler.subscribe(AppConstants.MQTT_TOPIC_STATUS, 1);
            mqttHandler.subscribe(AppConstants.MQTT_TOPIC_HEARTBEAT, 1);
            
        } catch (MqttException e) {
            handleMqttError("Error al conectar con MQTT", e);
        }
    }

    private void setupHeartbeatHandler() {
        heartbeatHandler = new HeartbeatHandler(this);
        heartbeatHandler.start();
    }

    private void setupAdapters() {
        try {
            messageAdapter = new MqttMessageAdapter(this, this);
            ledControlAdapter = new LedControlAdapter(getApplicationContext(), mqttHandler);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up adapters", e);
            Toast.makeText(this, "Error al inicializar adaptadores", Toast.LENGTH_LONG).show();
        }
    }

    private void setupButtonListeners() {
        try {
            Button buttonSend = findViewById(R.id.buttonSend);
            Button buttonLedA = findViewById(R.id.buttonLedA);
            Button buttonLedB = findViewById(R.id.buttonLedB);
            Button buttonLedC = findViewById(R.id.buttonLedC);

            // Configurar LEDs usando el adaptador
            ledControlAdapter.setupLedButton(buttonLedA, "led_a");
            ledControlAdapter.setupLedButton(buttonLedB, "led_b");
            ledControlAdapter.setupLedButton(buttonLedC, "led_c");

            buttonSend.setOnClickListener(v -> {
                String message = editTextMessage.getText().toString();
                if (!message.isEmpty()) {
                    try {
                        JSONObject jsonMessage = new JSONObject();
                        jsonMessage.put("type", "custom");
                        JSONObject payload = new JSONObject();
                        payload.put("message", message);
                        jsonMessage.put("payload", payload);
                        
                        mqttHandler.publishMessage(AppConstants.MQTT_TOPIC_COMMANDS, jsonMessage.toString());
                        editTextMessage.setText("");
                    } catch (MqttException e) {
                        handleMqttError("Error al publicar mensaje", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error al crear JSON", e);
                        Toast.makeText(this, "Error al crear mensaje JSON", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Ingresa un mensaje", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up button listeners", e);
            Toast.makeText(this, "Error al configurar botones", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleNoProvisionedDevice() {
        Log.e(TAG, "Error: MqttActivity iniciada sin aprovisionamiento previo");
        Toast.makeText(this, "Error: No hay dispositivo aprovisionado", Toast.LENGTH_LONG).show();
        
        // Forzar limpieza de estado para asegurar aprovisionamiento
        SharedPreferences prefs = getSharedPreferences(AppConstants.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(AppConstants.KEY_IS_PROVISIONED, false);
        editor.putString(AppConstants.KEY_DEVICE_ID, "");
        editor.apply();
        
        // Volver a la actividad de aprovisionamiento
        Intent mainIntent = new Intent(this, EspMainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void handleIncomingMessage(String topic, String payload) {
        messageAdapter.handleIncomingMessage(topic, payload);
    }

    // Implementar callbacks de MqttMessageAdapter.MessageCallback
    @Override
    public void onStatusUpdate(boolean isOnline) {
        heartbeatHandler.updateConnectionStatus(isOnline);
    }

    @Override
    public void onMessageReceived(String message) {
        runOnUiThread(() -> textViewReceived.setText(message));
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> Toast.makeText(this, error, Toast.LENGTH_SHORT).show());
    }

    private void sendLedCommand(String ledCommand) {
        try {
            JSONObject jsonCommand = new JSONObject();
            jsonCommand.put("type", "command");
            
            JSONObject payload = new JSONObject();
            payload.put("cmd", ledCommand);
            jsonCommand.put("payload", payload);
            
            mqttHandler.publishMessage(AppConstants.MQTT_TOPIC_COMMANDS, jsonCommand.toString());
            Log.d(TAG, "Comando LED enviado: " + jsonCommand.toString());
            
        } catch (JSONException e) {
            Log.e(TAG, "Error al crear comando JSON para LED", e);
            Toast.makeText(this, "Error al enviar comando", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBrokerConnectionUI(String status, int color) {
        runOnUiThread(() -> {
            textViewConnectionStatus.setText("Estado: " + status);
            textViewConnectionStatus.setTextColor(getResources().getColor(color));
            
            if (connectionIndicator != null) {
                int drawableId;
                if (color == android.R.color.holo_green_dark) {
                    drawableId = R.drawable.circle_indicator_green;
                } else if (color == android.R.color.holo_orange_dark) {
                    drawableId = R.drawable.circle_indicator_orange;
                } else {
                    drawableId = R.drawable.circle_indicator_red;
                }
                connectionIndicator.setBackground(getResources().getDrawable(drawableId));
            }
        });
    }

    private void reconnectToMqttBroker() {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                if (!mqttHandler.isConnected()) {
                    updateBrokerConnectionUI("Intentando reconectar...", android.R.color.holo_orange_dark);
                    
                    mqttHandler.connect();
                    mqttHandler.subscribe(AppConstants.MQTT_TOPIC_STATUS, 1);
                    mqttHandler.subscribe(AppConstants.MQTT_TOPIC_HEARTBEAT, 1);
                    
                    updateBrokerConnectionUI("Reconectado al broker", android.R.color.holo_blue_dark);
                    Log.d(TAG, "Reconectado al broker MQTT");
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupción durante la reconexión", e);
                Thread.currentThread().interrupt();
            } catch (MqttException e) {
                Log.e(TAG, "Error al reconectar con MQTT", e);
                updateBrokerConnectionUI("Error al reconectar", android.R.color.holo_red_dark);
                // Programar próximo intento de reconexión
                new Handler(Looper.getMainLooper()).postDelayed(this::reconnectToMqttBroker, 5000);
            }
        }).start();
    }

    private void handleMqttError(String message, MqttException e) {
        Log.e(TAG, message, e);
        String errorDetail;
        switch (e.getReasonCode()) {
            case MqttException.REASON_CODE_CLIENT_DISCONNECTING:
            case MqttException.REASON_CODE_SERVER_CONNECT_ERROR:
                errorDetail = "Error de conexión al broker";
                break;
            case MqttException.REASON_CODE_CONNECTION_LOST:
                errorDetail = "Conexión perdida";
                break;
            case MqttException.REASON_CODE_CLIENT_TIMEOUT:
                errorDetail = "Tiempo de espera agotado";
                break;
            default:
                errorDetail = e.getMessage();
        }
        
        updateBrokerConnectionUI(errorDetail, android.R.color.holo_red_dark);
        runOnUiThread(() -> 
            Toast.makeText(this, message + ": " + errorDetail, Toast.LENGTH_LONG).show()
        );
    }

    @Override
    public void onConnectionStatusChanged(boolean connected) {
        updateConnectionStatusUI(connected);
    }

    @Override
    public void onStabilityCounterChanged(int counter) {
        Log.d(TAG, "Estabilidad de conexión: " + counter);
    }

    private void updateConnectionStatusUI(boolean connected) {
        runOnUiThread(() -> {
            try {
                if (connected) {
                    textViewConnectionStatus.setText("Conectado");
                    textViewConnectionStatus.setTextColor(getResources().getColor(R.color.colorGreen));
                    connectionIndicator.setBackground(getResources().getDrawable(R.drawable.circle_indicator_green));
                } else {
                    textViewConnectionStatus.setText("Desconectado");
                    textViewConnectionStatus.setTextColor(getResources().getColor(R.color.colorRed));
                    connectionIndicator.setBackground(getResources().getDrawable(R.drawable.circle_indicator_red));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar UI de estado", e);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            heartbeatHandler.stop();
            mqttHandler.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning up", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mqtt, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_reset_provisioning) {
            // Mostrar diálogo de confirmación
            new AlertDialog.Builder(this)
                .setTitle("Resetear provisioning")
                .setMessage("¿Estás seguro que deseas resetear el provisioning? Esto te permitirá aprovisionar un nuevo dispositivo.")
                .setPositiveButton("Sí", (dialog, which) -> {
                    try {
                        // Limpiar el estado de provisioning
                        SharedPreferences prefs = getSharedPreferences(AppConstants.PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(AppConstants.KEY_IS_PROVISIONED, false);
                        editor.putString(AppConstants.KEY_DEVICE_ID, "");
                        editor.apply();
                        
                        Intent intent = new Intent(this, EspMainActivity.class);
                        intent.putExtra("FROM_RESET", true);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, "Error al resetear", e);
                        Toast.makeText(this, "Error al resetear: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
            return true;
        } else if (id == R.id.action_refresh) {
            // Refrescar la conexión MQTT
            Toast.makeText(this, "Refrescando conexión...", Toast.LENGTH_SHORT).show();
            reconnectToMqttBroker();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    // Solo para Android 13+ (API 33+)
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this, "Use el menú para salir", Toast.LENGTH_SHORT).show();
    }
}