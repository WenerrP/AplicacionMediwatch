package com.espressif.ui.utils;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;

import com.espressif.ui.activities.PatientActivity;
import com.espressif.ui.activities.FamilyActivity;
import com.espressif.wifi_provisioning.R;

public class DialogUtils {

    public interface DialogActionCallback {
        void onPositiveAction();
        void onNeutralAction();
    }

    public interface DeviceSelectionCallback {
        void onBleSelected();
        void onSoftApSelected();
    }

    public interface UserTypeSelectionCallback {
        void onPatientSelected();
        void onFamilySelected();
    }

    public static void showNoDeviceFoundDialog(Context context, DialogActionCallback callback) {
        new AlertDialog.Builder(context)
            .setTitle("Dispositivo no encontrado")
            .setMessage("No se detectó ningún dispositivo MediWatch conectado a la red. ¿Qué deseas hacer?")
            .setPositiveButton("Configurar nuevo dispositivo", (dialog, which) -> {
                if (callback != null) {
                    callback.onPositiveAction();
                }
            })
            .setNeutralButton("Intentar de nuevo", (dialog, which) -> {
                if (callback != null) {
                    callback.onNeutralAction();
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    public static AlertDialog showProgressDialog(Context context, String title, String message) {
        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .create();
        dialog.show();
        return dialog;
    }

    public static void showDeviceSelectionDialog(Context context, DeviceSelectionCallback callback) {
        final String[] deviceTypes = {"BLE", "SoftAP"};
        new AlertDialog.Builder(context)
            .setCancelable(true)
            .setTitle(R.string.dialog_msg_device_selection)
            .setItems(deviceTypes, (dialog, position) -> {
                if (callback != null) {
                    if (position == 0) {
                        callback.onBleSelected();
                    } else if (position == 1) {
                        callback.onSoftApSelected();
                    }
                }
                dialog.dismiss();
            })
            .show();
    }

    /**
     * Muestra un diálogo para seleccionar el tipo de usuario
     */
    public static void showUserTypeDialog(Context context, UserTypeSelectionCallback callback) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.activity_user_type, null);
        
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Seleccione tipo de usuario")
                .setView(dialogView)
                .setCancelable(false)
                .create();
        
        // Configurar botón de paciente
        dialogView.findViewById(R.id.btn_patient).setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) {
                callback.onPatientSelected();
            }
        });
        
        // Configurar botón de familiar
        dialogView.findViewById(R.id.btn_family).setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) {
                callback.onFamilySelected();
            }
        });
        
        dialog.show();
    }
}