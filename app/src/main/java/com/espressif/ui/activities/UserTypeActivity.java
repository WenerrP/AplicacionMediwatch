package com.espressif.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.espressif.AppConstants;
import com.espressif.wifi_provisioning.R;

public class UserTypeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type);

        findViewById(R.id.btn_patient).setOnClickListener(v -> {
            Intent intent = new Intent(UserTypeActivity.this, PatientActivity.class);
            intent.putExtra(AppConstants.EXTRA_USER_TYPE, AppConstants.USER_TYPE_PATIENT);
            startActivity(intent);
        });

        findViewById(R.id.btn_family).setOnClickListener(v -> {
            Intent intent = new Intent(UserTypeActivity.this, FamilyActivity.class);
            intent.putExtra(AppConstants.EXTRA_USER_TYPE, AppConstants.USER_TYPE_FAMILY);
            startActivity(intent);
        });
    }
}