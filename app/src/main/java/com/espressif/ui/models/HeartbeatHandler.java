package com.espressif.ui.models;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class HeartbeatHandler {
    private static final String TAG = "HeartbeatHandler";
    private static final long HEARTBEAT_TIMEOUT = 10000; // 10 seconds

    private final Handler handler;
    private final HeartbeatCallback callback;
    private boolean isRunning;
    private int stabilityCounter;

    public interface HeartbeatCallback {
        void onConnectionStatusChanged(boolean connected);
        void onStabilityCounterChanged(int counter);
    }

    public HeartbeatHandler(HeartbeatCallback callback) {
        this.callback = callback;
        this.handler = new Handler(Looper.getMainLooper());
        this.isRunning = false;
        this.stabilityCounter = 0;
    }

    public void start() {
        isRunning = true;
        scheduleHeartbeatCheck();
    }

    public void stop() {
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
    }

    public void resetHeartbeat() {
        if (isRunning) {
            handler.removeCallbacksAndMessages(null);
            stabilityCounter++;
            callback.onStabilityCounterChanged(stabilityCounter);
            scheduleHeartbeatCheck();
        }
    }

    public void updateConnectionStatus(boolean isConnected) {
        if (isConnected) {
            resetHeartbeat();
        }
        callback.onConnectionStatusChanged(isConnected);
    }

    private void scheduleHeartbeatCheck() {
        handler.postDelayed(() -> {
            if (isRunning) {
                stabilityCounter = 0;
                callback.onStabilityCounterChanged(stabilityCounter);
                callback.onConnectionStatusChanged(false);
            }
        }, HEARTBEAT_TIMEOUT);
    }
}