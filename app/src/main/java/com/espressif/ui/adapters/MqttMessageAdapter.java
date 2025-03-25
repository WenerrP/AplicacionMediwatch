package com.espressif.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import com.espressif.AppConstants;
import com.espressif.models.MqttMessage;

public class MqttMessageAdapter {
    private static final String TAG = "MqttMessageAdapter";
    private final Context context;
    private final MessageCallback callback;

    public interface MessageCallback {
        void onStatusUpdate(boolean isOnline);
        void onMessageReceived(String message);
        void onError(String error);
    }

    public MqttMessageAdapter(Context context, MessageCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void handleIncomingMessage(String topic, String payload) {
        try {
            JSONObject jsonMessage = new JSONObject(payload);
            callback.onMessageReceived(payload);

            if (isStatusTopic(topic)) {
                handleStatusMessage(jsonMessage);
            } else if (jsonMessage.has("type")) {
                handleTypeMessage(jsonMessage);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error processing message", e);
            callback.onError("Error processing message");
        }
    }

    private boolean isStatusTopic(String topic) {
        return topic.equals(AppConstants.MQTT_TOPIC_HEARTBEAT) || 
               topic.equals(AppConstants.MQTT_TOPIC_STATUS);
    }

    private void handleStatusMessage(JSONObject jsonMessage) throws JSONException {
        if (jsonMessage.has("status")) {
            String status = jsonMessage.getString("status");
            callback.onStatusUpdate("online".equals(status));
        } else {
            callback.onStatusUpdate(true);
        }
    }

    private void handleTypeMessage(JSONObject jsonMessage) throws JSONException {
        if ("ping".equals(jsonMessage.getString("type"))) {
            createPongResponse(jsonMessage);
        }
    }

    public JSONObject createLedCommand(String ledType) throws JSONException {
        JSONObject jsonCommand = new JSONObject();
        jsonCommand.put("type", "command");
        
        JSONObject payload = new JSONObject();
        payload.put("cmd", ledType);
        jsonCommand.put("payload", payload);
        
        return jsonCommand;
    }

    private JSONObject createPongResponse(JSONObject pingMessage) throws JSONException {
        JSONObject pongMessage = new JSONObject();
        pongMessage.put("type", "pong");
        if (pingMessage.has("finder") && pingMessage.getBoolean("finder")) {
            pongMessage.put("status", "online");
            pongMessage.put("timestamp", System.currentTimeMillis());
        }
        return pongMessage;
    }
}