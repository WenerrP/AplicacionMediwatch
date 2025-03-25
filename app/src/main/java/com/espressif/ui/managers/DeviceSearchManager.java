package com.espressif.ui.managers;

import android.content.Context;
import android.util.Log;
import com.espressif.AppConstants;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DeviceSearchManager {
    private static final String TAG = "DeviceSearchManager";
    private final Context context;

    public interface SearchCallback {
        void onDeviceFound(String deviceId);
        void onSearchFailed(String error);
    }

    public DeviceSearchManager(Context context) {
        this.context = context;
    }

    public void findDevice(SearchCallback callback) {
        new Thread(() -> {
            try {
                boolean found = searchDeviceViaMqtt();
                if (found) {
                    String deviceId = AppConstants.DEVICE_ID_PREFIX + System.currentTimeMillis();
                    callback.onDeviceFound(deviceId);
                } else {
                    callback.onSearchFailed(AppConstants.ERROR_NO_DEVICE_FOUND);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error searching device", e);
                callback.onSearchFailed(e.getMessage());
            }
        }).start();
    }

    private boolean searchDeviceViaMqtt() {
        final boolean[] deviceResponded = {false};
        final CountDownLatch latch = new CountDownLatch(1);
        
        MqttClient mqttClient = null;
        
        try {
            mqttClient = new MqttClient(AppConstants.MQTT_BROKER_URI, 
                    AppConstants.MQTT_FINDER_CLIENT_ID_PREFIX + System.currentTimeMillis(), 
                    null);
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(AppConstants.MQTT_CONNECTION_TIMEOUT);
            
            mqttClient.connect(options);
            
            final MqttClient finalMqttClient = mqttClient;
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "MQTT connection lost during search", cause);
                    latch.countDown();
                }
                
                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    handleIncomingMessage(message, deviceResponded, latch);
                }
                
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Search message sent successfully");
                }
            });
            
            mqttClient.subscribe(AppConstants.MQTT_TOPIC_STATUS, AppConstants.MQTT_SEARCH_QOS);
            sendPingMessage(mqttClient);
            
            boolean success = latch.await(AppConstants.DEVICE_SEARCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            return deviceResponded[0] && success;
            
        } catch (Exception e) {
            Log.e(TAG, "Error in MQTT search", e);
            return false;
        } finally {
            disconnectMqttClient(mqttClient);
        }
    }

    private void handleIncomingMessage(MqttMessage message, boolean[] deviceResponded, 
            CountDownLatch latch) {
        try {
            String payload = new String(message.getPayload());
            Log.d(TAG, "Message received: " + payload);
            
            JSONObject json = new JSONObject(payload);
            
            if (isValidResponse(json)) {
                Log.d(TAG, "Device found! Response: " + payload);
                deviceResponded[0] = true;
                latch.countDown();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON response", e);
        }
    }

    private boolean isValidResponse(JSONObject json) throws Exception {
        return (json.has("type") && "pong".equals(json.getString("type"))) || 
               (json.has("status") && "online".equals(json.getString("status")));
    }

    private void sendPingMessage(MqttClient mqttClient) throws Exception {
        JSONObject pingMessage = new JSONObject();
        pingMessage.put("type", "ping");
        pingMessage.put("finder", true);
        pingMessage.put("timestamp", System.currentTimeMillis());
        
        MqttMessage message = new MqttMessage(pingMessage.toString().getBytes());
        message.setQos(AppConstants.MQTT_SEARCH_QOS);
        mqttClient.publish(AppConstants.MQTT_TOPIC_COMMANDS, message);
        
        Log.d(TAG, "Ping sent: " + pingMessage.toString());
    }

    private void disconnectMqttClient(MqttClient mqttClient) {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                mqttClient.close();
            } catch (MqttException e) {
                Log.e(TAG, "Error disconnecting MQTT search client", e);
            }
        }
    }
}