package com.espressif.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;
import com.espressif.AppConstants;
import com.espressif.wifi_provisioning.R;

public class DialogManager {
    
    public interface DialogCallback {
        void onPositiveButton();
        void onNegativeButton();
    }

    public interface DeviceTypeCallback {
        void onBleSelected();
        void onSoftApSelected();
    }

    public static void showLocationDialog(Context context, DialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(R.string.dialog_title_location);
        builder.setMessage(R.string.dialog_msg_gps);
        builder.setPositiveButton(R.string.btn_ok, (dialog, which) -> callback.onPositiveButton());
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> callback.onNegativeButton());
        builder.show();
    }

    public static void showDeviceTypeDialog(Context context, DeviceTypeCallback callback) {
        String[] types = {
            context.getString(R.string.device_type_ble),
            context.getString(R.string.device_type_softap)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_title_select_device);
        builder.setSingleChoiceItems(types, -1, (dialog, which) -> {
            dialog.dismiss();
            if (which == 0) {
                callback.onBleSelected();
            } else {
                callback.onSoftApSelected();
            }
        });
        builder.show();
    }

    public static void showErrorDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.btn_ok, null);
        builder.show();
    }

    public static void showConfirmationDialog(Context context, String title, 
            String message, DialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.btn_ok, (dialog, which) -> callback.onPositiveButton());
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> callback.onNegativeButton());
        builder.show();
    }

    public static void showProgressDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.show();
    }

    public static void showNoDeviceFoundDialog(Context context, DialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_title_no_device);
        builder.setMessage(R.string.dialog_msg_no_device);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.btn_try_again, (dialog, which) -> callback.onPositiveButton());
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> callback.onNegativeButton());
        builder.show();
    }

    public static void showDisconnectionDialog(Context context, DialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_title_disconnect);
        builder.setMessage(R.string.dialog_msg_disconnect);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.btn_ok, (dialog, which) -> callback.onPositiveButton());
        builder.show();
    }
}