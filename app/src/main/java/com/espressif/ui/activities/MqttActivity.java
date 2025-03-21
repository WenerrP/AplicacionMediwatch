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
import org.eclipse.paho.client.mqttv3.MqttMessage;
import android.content.Intent;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.wifi_provisioning.R;

public class MqttActivity extends AppCompatActivity {

    private static final String TAG = "MqttActivity";
    private static final String BROKER_URI = "tcp://broker.emqx.io:1883";
    private static final String CLIENT_ID = "AndroidClient_" + System.currentTimeMillis();
    private String TOPIC_SUBSCRIBE = "/device/status";
    private String TOPIC_PUBLISH = "/device/commands";
    private String TOPIC_HEARTBEAT = "/device/heartbeat";
    
    // Constantes para la detección de heartbeat - REDUCIDO
    private static final int HEARTBEAT_TIMEOUT = 10000; // 10 segundos (más rápido)
    private static final int CONNECTION_CHECK_INTERVAL = 3000; // Verificar cada 3 segundos
    private static final int STABILITY_THRESHOLD = 2; // Reducido a 2 para respuesta más rápida
    private boolean deviceConnected = false;
    private Handler heartbeatHandler = new Handler(Looper.getMainLooper());
    private Runnable heartbeatRunnable;
    
    // Contador de mensajes recibidos para estabilidad
    private long lastMessageTimestamp = 0;
    private int connectionStabilityCounter = 0;

    private MqttClient mqttClient;
    private TextView textViewReceived;
    private TextView textViewConnectionStatus;
    private EditText editTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt);

        // Establecer título personalizado en la barra de acción
        setTitle("MediWatch MQTT");
        
        // Si se está utilizando Toolbar en lugar de ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("MediWatch MQTT");
        }
        
        // Obtener deviceId del intent si está disponible
        if (getIntent().hasExtra("DEVICE_ID")) {
            String deviceId = getIntent().getStringExtra("DEVICE_ID");
            // Personalizar los tópicos con el ID del dispositivo
            TOPIC_SUBSCRIBE = "mediwatch/" + deviceId + "/data";
            TOPIC_PUBLISH = "mediwatch/" + deviceId + "/control";
            TOPIC_HEARTBEAT = "mediwatch/" + deviceId + "/heartbeat";
        }

        // Inicializar vistas
        textViewReceived = findViewById(R.id.textViewReceived);
        textViewConnectionStatus = findViewById(R.id.textViewConnectionStatus);
        editTextMessage = findViewById(R.id.editTextMessage);
        Button buttonSend = findViewById(R.id.buttonSend);
        
        // Agregar botones específicos para LEDs
        Button buttonLedA = findViewById(R.id.buttonLedA);
        Button buttonLedB = findViewById(R.id.buttonLedB);
        Button buttonLedC = findViewById(R.id.buttonLedC);

        // Actualizar información de tópicos en la UI
        TextView textViewTopicInfo = findViewById(R.id.textViewTopicInfo);
        textViewTopicInfo.setText("Tópico de suscripción: " + TOPIC_SUBSCRIBE +
                "\nTópico de publicación: " + TOPIC_PUBLISH +
                "\nTópico de heartbeat: " + TOPIC_HEARTBEAT);

        // Configurar conexión MQTT
        connectToMqttBroker();
        
        // Configurar el detector de heartbeat
        setupHeartbeatDetection();

        // Configurar listeners para LEDs
        if (buttonLedA != null) {
            buttonLedA.setOnClickListener(v -> sendLedCommand("led_a"));
        }
        
        if (buttonLedB != null) {
            buttonLedB.setOnClickListener(v -> sendLedCommand("led_b"));
        }
        
        if (buttonLedC != null) {
            buttonLedC.setOnClickListener(v -> sendLedCommand("led_c"));
        }

        // Configurar listeners
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString();
                if (!message.isEmpty()) {
                    try {
                        // Crear un JSON personalizado según lo que se ingrese
                        JSONObject jsonMessage = new JSONObject();
                        jsonMessage.put("type", "custom");
                        JSONObject payload = new JSONObject();
                        payload.put("message", message);
                        jsonMessage.put("payload", payload);
                        
                        publishMessage(TOPIC_PUBLISH, jsonMessage.toString());
                        editTextMessage.setText(""); // Limpiar el campo después de enviar
                    } catch (JSONException e) {
                        Log.e(TAG, "Error al crear JSON", e);
                        Toast.makeText(MqttActivity.this, "Error al crear mensaje JSON", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MqttActivity.this, "Ingresa un mensaje", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    /**
     * Envía un comando para controlar un LED específico en formato JSON
     */
    private void sendLedCommand(String ledCommand) {
        try {
            JSONObject jsonCommand = new JSONObject();
            jsonCommand.put("type", "command");
            
            JSONObject payload = new JSONObject();
            payload.put("cmd", ledCommand);
            jsonCommand.put("payload", payload);
            
            publishMessage(TOPIC_PUBLISH, jsonCommand.toString());
            Log.d(TAG, "Comando LED enviado: " + jsonCommand.toString());
            
            // Considerar esto como una interacción válida para reiniciar el heartbeat
            resetHeartbeatTimer();
        } catch (JSONException e) {
            Log.e(TAG, "Error al crear comando JSON para LED", e);
            Toast.makeText(this, "Error al enviar comando", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Configura la detección de heartbeat para monitorear la conexión del dispositivo
     */
    private void setupHeartbeatDetection() {
        heartbeatRunnable = new Runnable() {
            @Override
            public void run() {
                long timeSinceLastMessage = System.currentTimeMillis() - lastMessageTimestamp;
                
                // Si estamos conectados pero no hemos recibido mensajes recientemente
                if (deviceConnected && timeSinceLastMessage >= HEARTBEAT_TIMEOUT) {
                    Log.d(TAG, "Verificando estado: Sin actividad por " + timeSinceLastMessage/1000 + " segundos");
                    
                    // Primera verificación: iniciar periodo de gracia
                    if (connectionStabilityCounter > 0) {
                        connectionStabilityCounter--;
                        Log.d(TAG, "Periodo de gracia: quedan " + connectionStabilityCounter + " intentos");
                        
                        // Verificar más frecuentemente durante el periodo de gracia
                        heartbeatHandler.postDelayed(this, CONNECTION_CHECK_INTERVAL);
                        
                        // Opcional: Enviar un ping para provocar respuesta
                        try {
                            if (mqttClient != null && mqttClient.isConnected()) {
                                JSONObject pingMessage = new JSONObject();
                                pingMessage.put("type", "ping");
                                publishMessage(TOPIC_PUBLISH, pingMessage.toString());
                                Log.d(TAG, "Ping enviado para verificar conexión");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al enviar ping", e);
                        }
                    } else {
                        // Después del periodo de gracia, marcamos como desconectado
                        updateConnectionStatus(false);
                        Log.d(TAG, "Dispositivo marcado como desconectado después de " + 
                              (HEARTBEAT_TIMEOUT + (STABILITY_THRESHOLD * CONNECTION_CHECK_INTERVAL))/1000 + 
                              " segundos sin respuesta");
                        
                        // Seguir verificando pero con menor frecuencia
                        heartbeatHandler.postDelayed(this, HEARTBEAT_TIMEOUT);
                    }
                } else {
                    // Programación normal
                    heartbeatHandler.postDelayed(this, HEARTBEAT_TIMEOUT);
                }
            }
        };
    }
    
    /**
     * Reinicia el temporizador de heartbeat cuando hay actividad
     */
    private void resetHeartbeatTimer() {
        heartbeatHandler.removeCallbacks(heartbeatRunnable);
        lastMessageTimestamp = System.currentTimeMillis();
        
        // Si estaba desconectado, asumimos reconexión inmediata
        if (!deviceConnected) {
            Log.d(TAG, "Actividad detectada después de desconexión, verificando reconexión");
            updateConnectionStatus(true);
        }
        
        // Programar la próxima verificación
        heartbeatHandler.postDelayed(heartbeatRunnable, HEARTBEAT_TIMEOUT);
        Log.d(TAG, "Temporizador de heartbeat reiniciado");
    }
    
    /**
     * Actualiza el estado de conexión en la UI
     */
    private void updateConnectionStatus(boolean connected) {
        // Para respuesta más rápida al conectar
        if (connected && !deviceConnected) {
            // Cambio inmediato a conectado cuando recibimos un mensaje
            deviceConnected = true;
            connectionStabilityCounter = STABILITY_THRESHOLD;
            updateConnectionStatusUI(true);
            return;
        }
        
        // Para desconexión, usamos el mecanismo de estabilidad
        if (!connected && deviceConnected) {
            // Si ya pasamos por el periodo de gracia en setupHeartbeatDetection
            deviceConnected = false;
            updateConnectionStatusUI(false);
        } else if (connected) {
            // Si seguimos conectados, mantener el contador al máximo
            connectionStabilityCounter = STABILITY_THRESHOLD;
        }
    }
    
    /**
     * Actualiza solo la UI del estado de conexión
     */
    private void updateConnectionStatusUI(boolean connected) {
        runOnUiThread(() -> {
            View connectionIndicator = findViewById(R.id.connectionIndicator);
            
            if (connected) {
                textViewConnectionStatus.setText("Estado: Dispositivo Conectado");
                textViewConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                if (connectionIndicator != null) {
                    connectionIndicator.setBackground(getResources().getDrawable(R.drawable.circle_indicator_green));
                }
            } else {
                textViewConnectionStatus.setText("Estado: Dispositivo Desconectado");
                textViewConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                if (connectionIndicator != null) {
                    connectionIndicator.setBackground(getResources().getDrawable(R.drawable.circle_indicator_red));
                }
            }
        });
    }

    /**
     * Actualiza el estado de la interfaz de usuario para mostrar el estado de conexión al broker MQTT
     */
    private void updateBrokerConnectionUI(String status, int color) {
        runOnUiThread(() -> {
            View connectionIndicator = findViewById(R.id.connectionIndicator);
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

    private void connectToMqttBroker() {
        try {
            updateBrokerConnectionUI("Conectando al broker...", android.R.color.holo_orange_dark);

            mqttClient = new MqttClient(BROKER_URI, CLIENT_ID, null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(30); // Timeout más largo para conexión
            options.setKeepAliveInterval(60); // Keep-alive más largo

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "Conexión MQTT perdida", cause);
                    runOnUiThread(() -> {
                        textViewConnectionStatus.setText("Estado: Desconectado del broker");
                        textViewConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        Toast.makeText(MqttActivity.this, "Conexión MQTT perdida", Toast.LENGTH_LONG).show();
                    });
                    // Intentar reconectar automáticamente
                    reconnectToMqttBroker();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    final String receivedMsg = new String(message.getPayload());
                    Log.d(TAG, "Mensaje recibido en " + topic + ": " + receivedMsg);
                    
                    // Cualquier mensaje recibido reinicia el temporizador y confirma conexión
                    resetHeartbeatTimer();
                    
                    // Procesar el mensaje recibido
                    try {
                        JSONObject jsonMessage = new JSONObject(receivedMsg);
                        
                        // Mostrar el mensaje en la UI
                        runOnUiThread(() -> textViewReceived.setText(receivedMsg));
                        
                        // Detección explícita de estado
                        if (topic.equals(TOPIC_HEARTBEAT) || topic.equals(TOPIC_SUBSCRIBE)) {
                            if (jsonMessage.has("status")) {
                                String status = jsonMessage.getString("status");
                                if ("online".equals(status)) {
                                    Log.d(TAG, "Estado explícito ONLINE recibido");
                                    updateConnectionStatus(true);
                                } else if ("offline".equals(status)) {
                                    Log.d(TAG, "Estado explícito OFFLINE recibido");
                                    // Desconexión inmediata si el dispositivo reporta offline
                                    connectionStabilityCounter = 0;
                                    updateConnectionStatus(false);
                                }
                            } else {
                                // Cualquier mensaje en estos topics indica que el dispositivo 
                                // está funcionando - interpretar como señal de vida
                                updateConnectionStatus(true);
                            }
                        }
                        
                        // También detectar mensajes de respuesta a ping
                        if (jsonMessage.has("type") && "pong".equals(jsonMessage.getString("type"))) {
                            Log.d(TAG, "Respuesta a ping recibida, dispositivo activo");
                            updateConnectionStatus(true);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error al procesar JSON recibido", e);
                        // A pesar del error, consideramos que el dispositivo está activo
                        updateConnectionStatus(true);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Mensaje entregado correctamente");
                }
            });

            mqttClient.connect(options);
            mqttClient.subscribe(TOPIC_SUBSCRIBE, 1); // QoS 1 para mejor fiabilidad
            mqttClient.subscribe(TOPIC_HEARTBEAT, 1);

            updateBrokerConnectionUI("Conectado al broker", android.R.color.holo_blue_dark);

            Log.d(TAG, "Conectado y suscrito a " + TOPIC_SUBSCRIBE + " y " + TOPIC_HEARTBEAT);
            
            // Iniciar temporizador de heartbeat
            resetHeartbeatTimer();

        } catch (MqttException e) {
            Log.e(TAG, "Error al conectar con MQTT", e);
            updateBrokerConnectionUI("Error de conexión al broker", android.R.color.holo_red_dark);
            runOnUiThread(() -> {
                textViewConnectionStatus.setText("Estado: Error de conexión al broker");
                textViewConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                Toast.makeText(MqttActivity.this, "Error al conectar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }
    
    /**
     * Intenta reconectar al broker MQTT después de una desconexión
     */
    private void reconnectToMqttBroker() {
        new Thread(() -> {
            try {
                Thread.sleep(5000); // Esperar 5 segundos antes de intentar reconectar
                if (mqttClient != null && !mqttClient.isConnected()) {
                    runOnUiThread(() -> {
                        textViewConnectionStatus.setText("Estado: Intentando reconectar...");
                        textViewConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    });
                    
                    mqttClient.connect();
                    mqttClient.subscribe(TOPIC_SUBSCRIBE, 1);
                    mqttClient.subscribe(TOPIC_HEARTBEAT, 1);
                    
                    runOnUiThread(() -> {
                        textViewConnectionStatus.setText("Estado: Reconectado al broker");
                        textViewConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                    });
                    
                    Log.d(TAG, "Reconectado al broker MQTT");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al reconectar con MQTT", e);
                runOnUiThread(() -> {
                    textViewConnectionStatus.setText("Estado: Error al reconectar");
                    textViewConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                });
                // Intentar de nuevo recursivamente
                reconnectToMqttBroker();
            }
        }).start();
    }

    private void publishMessage(String topic, String payload) {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(1); // QoS 1 para confirmación de entrega
                message.setRetained(false);
                mqttClient.publish(topic, message);
                Log.d(TAG, "Mensaje publicado en " + topic + ": " + payload);
                
                // Al enviar un mensaje, consideramos que hay actividad - reset del timer
                resetHeartbeatTimer();
            } else {
                Log.e(TAG, "Intento de publicar mensaje sin conexión MQTT");
                Toast.makeText(this, "No hay conexión MQTT. Reconectando...", Toast.LENGTH_SHORT).show();
                reconnectToMqttBroker();
            }
        } catch (MqttException e) {
            Log.e(TAG, "Error al publicar mensaje", e);
            Toast.makeText(this, "Error al enviar mensaje: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            heartbeatHandler.removeCallbacks(heartbeatRunnable);
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
        } catch (MqttException e) {
            Log.e(TAG, "Error al desconectar MQTT", e);
        }
    }
}