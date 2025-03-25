package com.espressif.ui.models;

import org.json.JSONException;
import org.json.JSONObject;

public class MqttMessage {
    private final JSONObject json;

    public MqttMessage(String type) {
        json = new JSONObject();
        try {
            json.put("type", type);
            json.put("timestamp", System.currentTimeMillis());
            json.put("payload", new JSONObject());
        } catch (JSONException e) {
            throw new RuntimeException("Error creating MqttMessage", e);
        }
    }

    public void addPayload(String key, Object value) throws JSONException {
        json.getJSONObject("payload").put(key, value);
    }

    public static MqttMessage createPing(boolean isFinder) throws JSONException {
        MqttMessage message = new MqttMessage("ping");
        message.json.put("finder", isFinder);
        return message;
    }

    public static MqttMessage createPong(JSONObject pingMessage) throws JSONException {
        MqttMessage message = new MqttMessage("pong");
        if (pingMessage.has("finder") && pingMessage.getBoolean("finder")) {
            message.json.put("status", "online");
        }
        return message;
    }

    public static MqttMessage createCommand(String cmd) throws JSONException {
        MqttMessage message = new MqttMessage("command");
        message.addPayload("cmd", cmd);
        return message;
    }

    @Override
    public String toString() {
        return json.toString();
    }
}