package com.espressif.ui.utils;

import android.util.Log;
import org.eclipse.paho.client.mqttv3.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MqttDeviceFinder {
    private static final String TAG = "MqttDeviceFinder";
    private static final String BROKER_URI = "tcp://broker.emqx.io:1883";
    private static final String STATUS_TOPIC = "mediwatch/status";
    private static final long TIMEOUT_MS = 5000; // Reducido a 5 segundos

    public interface DeviceFinderCallback {
        void onDeviceFound(String deviceId);
        void onDeviceNotFound();
        void onError(String error);
    }

    public static void findDevice(DeviceFinderCallback callback) {
        MqttClient mqttClient = null;
        final CountDownLatch latch = new CountDownLatch(1);
        final String[] foundDeviceId = {null};

        try {
            String clientId = "AndroidFinder_" + System.currentTimeMillis();
            mqttClient = new MqttClient(BROKER_URI, clientId, null);
            
            // Configurar opciones de conexión
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setAutomaticReconnect(false);
            
            // Conectar y suscribirse
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
                    Log.d(TAG, "Mensaje recibido: " + payload);

                    if ("online".equals(payload)) {
                        // Extraer deviceId del topic (ejemplo: mediwatch/status/device123)
                        String[] topicParts = topic.split("/");
                        if (topicParts.length >= 3) {
                            foundDeviceId[0] = topicParts[2];
                            latch.countDown();
                        }
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            // Suscribirse al topic de estado
            mqttClient.subscribe(STATUS_TOPIC + "/#", 1);

            // Esperar respuesta o timeout
            boolean received = latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            
            if (foundDeviceId[0] != null) {
                callback.onDeviceFound(foundDeviceId[0]);
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