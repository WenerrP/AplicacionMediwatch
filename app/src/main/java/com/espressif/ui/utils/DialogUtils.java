package com.espressif.ui.utils;

import android.content.Context;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;

public class DialogUtils {

    public interface DialogActionCallback {
        void onPositiveAction();
        void onNeutralAction();
    }

    public static void showNoDeviceFoundDialog(Context context, DialogActionCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Dispositivo no encontrado");
        builder.setMessage("No se detectó ningún dispositivo MediWatch conectado a la red. ¿Qué deseas hacer?");
        
        builder.setPositiveButton("Configurar nuevo dispositivo", (dialog, which) -> {
            if (callback != null) {
                callback.onPositiveAction();
            }
        });
        
        builder.setNeutralButton("Intentar de nuevo", (dialog, which) -> {
            if (callback != null) {
                callback.onNeutralAction();
            }
        });
        
        builder.setNegativeButton("Cancelar", null);
        
        builder.show();
    }
}