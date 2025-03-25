package com.espressif.ui.utils;

import android.util.Log;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MqttDeviceFinder {
    private static final String TAG = "MqttDeviceFinder";
    private static final String BROKER_URI = "tcp://broker.emqx.io:1883";
    private static final String TOPIC_PUBLISH = "/device/commands";
    private static final String TOPIC_SUBSCRIBE = "/device/status";
    private static final long TIMEOUT_MS = 10000;

    public static boolean findDevice() {
        final boolean[] deviceResponded = {false};
        final CountDownLatch latch = new CountDownLatch(1);
        MqttClient mqttClient = null;
        
        try {
            String clientId = "AndroidFinder_" + System.currentTimeMillis();
            mqttClient = new MqttClient(BROKER_URI, clientId, null);
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            
            mqttClient.connect(options);
            configureCallbacks(mqttClient, deviceResponded, latch);
            mqttClient.subscribe(TOPIC_SUBSCRIBE, 1);
            sendPingMessage(mqttClient);
            
            latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            return deviceResponded[0];
            
        } catch (Exception e) {
            Log.e(TAG, "Error en búsqueda MQTT", e);
            return false;
        } finally {
            disconnectClient(mqttClient);
        }
    }

    private static void configureCallbacks(MqttClient client, boolean[] deviceResponded, CountDownLatch latch) {
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.e(TAG, "Conexión MQTT perdida durante búsqueda", cause);
                latch.countDown();
            }
            
            @Override
            public void messageArrived(String topic, MqttMessage message) {
                handleMessage(message, deviceResponded, latch);
            }
            
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d(TAG, "Mensaje de búsqueda enviado correctamente");
            }
        });
    }

    private static void handleMessage(MqttMessage message, boolean[] deviceResponded, CountDownLatch latch) {
        String payload = new String(message.getPayload());
        Log.d(TAG, "Mensaje recibido: " + payload);
        
        try {
            JSONObject json = new JSONObject(payload);
            if ((json.has("type") && "pong".equals(json.getString("type"))) || 
                (json.has("status") && "online".equals(json.getString("status")))) {
                deviceResponded[0] = true;
                latch.countDown();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error al analizar respuesta JSON", e);
        }
    }

    private static void sendPingMessage(MqttClient client) throws Exception {
        JSONObject pingMessage = new JSONObject()
            .put("type", "ping")
            .put("finder", true)
            .put("timestamp", System.currentTimeMillis());
        
        MqttMessage message = new MqttMessage(pingMessage.toString().getBytes());
        message.setQos(1);
        client.publish(TOPIC_PUBLISH, message);
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