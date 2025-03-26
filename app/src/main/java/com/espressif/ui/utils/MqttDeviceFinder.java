package com.espressif.ui.utils;

import android.util.Log;
import org.eclipse.paho.client.mqttv3.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MqttDeviceFinder {
    private static final String TAG = "MqttDeviceFinder";
    private static final String BROKER_URI = "tcp://broker.emqx.io:1883";
    private static final String STATUS_TOPIC = "mediwatch/status";
    private static final String COMMAND_TOPIC = "/device/commands";
    private static final long TIMEOUT_MS = 5000; // 5 segundos de espera

    public interface DeviceFinderCallback {
        void onDeviceFound(String deviceId);
        void onDeviceNotFound();
        void onError(String error);
    }

    /**
     * Busca un dispositivo ESP32 conectado a la red
     */
    public static void findDevice(DeviceFinderCallback callback) {
        MqttClient mqttClient = null;
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] deviceFound = {false};

        try {
            String clientId = "AndroidFinder_" + System.currentTimeMillis();
            mqttClient = new MqttClient(BROKER_URI, clientId, null);
            
            // Configurar opciones de conexión
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setAutomaticReconnect(false);
            
            // Conectar al broker
            mqttClient.connect(options);
            
            // Configurar callback para mensajes
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "Conexión perdida", cause);
                    latch.countDown();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    Log.d(TAG, "Mensaje recibido en tema: " + topic + ", contenido: " + payload);

                    // Verificar si es una respuesta del ESP32
                    if (topic.equals(COMMAND_TOPIC)) {
                        // Si recibimos cualquier mensaje en este tópico, significa que el ESP32 está conectado
                        deviceFound[0] = true;
                        
                        // Extraer deviceId del mensaje si está disponible
                        // Por ahora usamos un ID genérico ya que estamos en pruebas
                        latch.countDown();
                    }
                    // También verificar el tópico de estado
                    else if (topic.equals(STATUS_TOPIC) && "online".equals(payload)) {
                        deviceFound[0] = true;
                        latch.countDown();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            // Suscribirse a ambos tópicos
            mqttClient.subscribe(STATUS_TOPIC, 1);
            mqttClient.subscribe(COMMAND_TOPIC, 1);
            
            // Enviar un mensaje de ping al ESP32
            String pingMessage = "ping_" + System.currentTimeMillis();
            MqttMessage mqttMessage = new MqttMessage(pingMessage.getBytes());
            mqttMessage.setQos(1);
            mqttClient.publish(COMMAND_TOPIC, mqttMessage);

            // Esperar respuesta o timeout
            boolean received = latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            
            if (deviceFound[0]) {
                // Para las pruebas, podemos usar un ID genérico o uno basado en timestamp
                String deviceId = "esp32_" + System.currentTimeMillis();
                callback.onDeviceFound(deviceId);
            } else {
                callback.onDeviceNotFound();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error en búsqueda MQTT", e);
            callback.onError(e.getMessage());
        } finally {
            disconnectClient(mqttClient);
        }
    }

    /**
     * Verificar si un ESP32 específico está conectado
     */
    public static void checkDeviceConnection(DeviceFinderCallback callback) {
        // Para la fase de pruebas, esta función es similar a findDevice
        // pero puede ser especializada cuando tengamos IDs específicos
        findDevice(callback);
    }

    private static void disconnectClient(MqttClient client) {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
                client.close();
            } catch (MqttException e) {
                Log.e(TAG, "Error al desconectar cliente MQTT", e);
            }
        }
    }
}