package com.espressif.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import com.espressif.wifi_provisioning.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.espressif.AppConstants;

public class FamilyActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        findViewById(R.id.btn_submit_patient_id).setOnClickListener(v -> {
            EditText etPatientId = findViewById(R.id.et_patient_id);
            String patientId = etPatientId.getText().toString().trim();
            if (!patientId.isEmpty()) {
                checkPatientId(patientId);
            } else {
                etPatientId.setError(AppConstants.ERROR_EMPTY_PATIENT_ID);
            }
        });
    }

    private void checkPatientId(String patientId) {
        mDatabase.child(AppConstants.FB_USERS_PATH).child(patientId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Intent intent = new Intent(FamilyActivity.this, FamilyDashboardActivity.class);
                    intent.putExtra(AppConstants.EXTRA_PATIENT_ID, patientId);
                    startActivity(intent);
                    finish();
                } else {
                    Log.w(AppConstants.TAG_FAMILY_ACTIVITY, AppConstants.ERROR_PATIENT_NOT_FOUND);
                    EditText etPatientId = findViewById(R.id.et_patient_id);
                    etPatientId.setError(AppConstants.ERROR_PATIENT_NOT_FOUND);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(AppConstants.TAG_FAMILY_ACTIVITY, "checkPatientId:onCancelled", 
                      databaseError.toException());
            }
        });
    }
}