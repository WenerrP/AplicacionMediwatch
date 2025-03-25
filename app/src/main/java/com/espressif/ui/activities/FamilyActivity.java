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
                etPatientId.setError("Por favor ingrese un ID válido");
            }
        });
    }

    private void checkPatientId(String patientId) {
        mDatabase.child("users").child(patientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // ID válido, redirigir a la pantalla de alarmas y resúmenes
                    Intent intent = new Intent(FamilyActivity.this, FamilyDashboardActivity.class);
                    intent.putExtra("PATIENT_ID", patientId);
                    startActivity(intent);
                    finish();
                } else {
                    Log.w("FamilyActivity", "ID de paciente no encontrado");
                    EditText etPatientId = findViewById(R.id.et_patient_id);
                    etPatientId.setError("ID de paciente no encontrado");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("FamilyActivity", "checkPatientId:onCancelled", databaseError.toException());
            }
        });
    }
}