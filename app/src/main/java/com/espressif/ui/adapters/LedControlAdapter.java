package com.espressif.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.espressif.AppConstants;
import com.espressif.ui.models.MqttHandler;
import org.json.JSONObject;
import org.eclipse.paho.client.mqttv3.MqttException;

public class LedControlAdapter {
    private static final String TAG = "LedControlAdapter";
    private final Context context;
    private final MqttHandler mqttHandler;

    public LedControlAdapter(Context context, MqttHandler mqttHandler) {
        this.context = context;
        this.mqttHandler = mqttHandler;
    }

    public void setupLedButton(Button button, String ledId) {
        button.setOnClickListener(v -> {
            try {
                sendLedCommand(ledId);
            } catch (MqttException e) {
                Log.e(TAG, "Error sending LED command", e);
                Toast.makeText(context, "Error al enviar comando: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendLedCommand(String ledId) throws MqttException {
        try {
            JSONObject jsonCommand = new JSONObject();
            jsonCommand.put("type", "command");
            
            JSONObject payload = new JSONObject();
            payload.put("cmd", "toggle_" + ledId);
            jsonCommand.put("payload", payload);
            
            mqttHandler.publishMessage(AppConstants.MQTT_TOPIC_COMMANDS, jsonCommand.toString());
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating LED command", e);
            Toast.makeText(context, "Error al crear comando", Toast.LENGTH_SHORT).show();
        }
    }
}