package com.espressif.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.espressif.wifi_provisioning.R;

public class FamilyDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_dashboard);

        // Aquí puedes agregar la lógica para mostrar las alarmas y resúmenes del paciente
    }
}