package com.espressif.ui.activities; // Asegúrate de usar tu paquete correcto

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import android.content.Intent;

import com.espressif.wifi_provisioning.R;

public class MqttActivity extends AppCompatActivity {

    private static final String TAG = "MqttActivity";
    private static final String BROKER_URI = "tcp://broker.emqx.io:1883"; // Cambia esto por tu broker MQTT
    private static final String CLIENT_ID = "AndroidClient_" + System.currentTimeMillis();
    private String TOPIC_SUBSCRIBE = "/device/status";  // Tópico donde recibes mensajes
    private String TOPIC_PUBLISH = "/led/command"; // Tópico donde envías mensajes

    private MqttClient mqttClient;
    private TextView textViewReceived;
    private TextView textViewConnectionStatus;
    private EditText editTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt);

        // Obtener deviceId del intent si está disponible
        if (getIntent().hasExtra("DEVICE_ID")) {
            String deviceId = getIntent().getStringExtra("DEVICE_ID");
            // Se podría personalizar los tópicos con el ID del dispositivo
            TOPIC_SUBSCRIBE = "mediwatch/" + deviceId + "/data";
            TOPIC_PUBLISH = "mediwatch/" + deviceId + "/control";
        }

        // Inicializar vistas
        textViewReceived = findViewById(R.id.textViewReceived);
        textViewConnectionStatus = findViewById(R.id.textViewConnectionStatus);
        editTextMessage = findViewById(R.id.editTextMessage);
        Button buttonSend = findViewById(R.id.buttonSend);
        Button buttonBackToProvisioning = findViewById(R.id.buttonBackToProvisioning);

        // Actualizar información de tópicos en la UI
        TextView textViewTopicInfo = findViewById(R.id.textViewTopicInfo);
        textViewTopicInfo.setText("Tópico de suscripción: " + TOPIC_SUBSCRIBE +
                "\nTópico de publicación: " + TOPIC_PUBLISH);

        // Configurar conexión MQTT
        connectToMqttBroker();

        // Configurar listeners
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString();
                if (!message.isEmpty()) {
                    publishMessage(TOPIC_PUBLISH, message);
                    editTextMessage.setText(""); // Limpiar el campo después de enviar
                } else {
                    Toast.makeText(MqttActivity.this, "Ingresa un mensaje", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonBackToProvisioning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Volver a la pantalla de provisioning
                Intent intent = new Intent(MqttActivity.this, ProvisionActivity.class);
                startActivity(intent);
                // Opcional: cerrar esta actividad
                // finish();
            }
        });
    }

    private void connectToMqttBroker() {
        try {
            textViewConnectionStatus.setText("Estado: Conectando...");
            textViewConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));

            mqttClient = new MqttClient(BROKER_URI, CLIENT_ID, null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "Conexión perdida", cause);
                    runOnUiThread(() -> {
                        textViewConnectionStatus.setText("Estado: Desconectado");
                        textViewConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        Toast.makeText(MqttActivity.this, "Conexión MQTT perdida", Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    final String receivedMsg = new String(message.getPayload());
                    Log.d(TAG, "Mensaje recibido: " + receivedMsg);
                    runOnUiThread(() -> textViewReceived.setText(receivedMsg));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Mensaje entregado correctamente");
                }
            });

            mqttClient.connect(options);
            mqttClient.subscribe(TOPIC_SUBSCRIBE);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewConnectionStatus.setText("Estado: Conectado");
                    textViewConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                }
            });

            Log.d(TAG, "Conectado y suscrito a " + TOPIC_SUBSCRIBE);

        } catch (MqttException e) {
            Log.e(TAG, "Error al conectar con MQTT", e);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewConnectionStatus.setText("Estado: Error de conexión");
                    textViewConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    Toast.makeText(MqttActivity.this, "Error al conectar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void publishMessage(String topic, String payload) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            mqttClient.publish(topic, message);
            Log.d(TAG, "Mensaje publicado en " + topic + ": " + payload);
        } catch (MqttException e) {
            Log.e(TAG, "Error al publicar mensaje", e);
            Toast.makeText(this, "Error al enviar mensaje: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
        } catch (MqttException e) {
            Log.e(TAG, "Error al desconectar MQTT", e);
        }
    }
}