package com.rlb.bloodlink;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper db;
    final String DONNEUR="donneur";
    final String MEDECIN="medecin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = new DatabaseHelper(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;

            String role = getUserRole();
            if ("medecin".equals(role)) {
                intent=new Intent(this, BloodLinkMedecinActivity.class);
            } else if ("donneur".equals(role)) {
                intent=new Intent(this, BloodLinkDonneurActivity.class);
            } else {
                intent=new Intent(this, BloodLinkAcceuilActivity.class);
            }


            startActivity(intent);
            finish();
        }, 3000);
    }
    private boolean isUserFullyRegisteredForMedecin() {
        Cursor cursor = db.getLastIdCursor();
        if (cursor != null && cursor.moveToFirst()) {
            String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String telephone = cursor.getString(cursor.getColumnIndexOrThrow("telephone"));
            String adresse = cursor.getString(cursor.getColumnIndexOrThrow("adresse"));
            String sexe = cursor.getString(cursor.getColumnIndexOrThrow("sexe"));

            android.util.Log.d("DB_CHECK", "role=" + role + ", name=" + name + ", email=" + email + ", tel=" + telephone + ", adresse=" + adresse +
                    ", sexe=" + sexe);
            cursor.close();

            // Vérifie si toutes les infos sont présentes (non nulles et non vides)
            return isNotEmpty(role)
                    && isNotEmpty(name)
                    && isNotEmpty(email)
                    && isNotEmpty(telephone)
                    && isNotEmpty(adresse)
                    && isNotEmpty(sexe)
                    && role.equals(MEDECIN);

        }

        return false;
    }

    private boolean isUserFullyRegisteredForDonneur() {
        Cursor cursor = db.getLastIdCursor();
        if (cursor != null && cursor.moveToFirst()) {
            String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String telephone = cursor.getString(cursor.getColumnIndexOrThrow("telephone"));
            String adresse = cursor.getString(cursor.getColumnIndexOrThrow("adresse"));
            String sexe = cursor.getString(cursor.getColumnIndexOrThrow("sexe"));
            String groupe = cursor.getString(cursor.getColumnIndexOrThrow("groupe"));

            android.util.Log.d("DB_CHECK", "role=" + role + ", name=" + name + ", email=" + email + ", tel=" + telephone + ", adresse=" + adresse +
                    ", sexe=" + sexe + ", groupe=" + groupe);
            cursor.close();

            // Vérifie si toutes les infos sont présentes (non nulles et non vides)
            return isNotEmpty(role)
                    && isNotEmpty(name)
                    && isNotEmpty(email)
                    && isNotEmpty(telephone)
                    && isNotEmpty(adresse)
                    && isNotEmpty(sexe)
                    && isNotEmpty(groupe)
                    && role.equals(DONNEUR);
        }

        return false;
    }
    private String getUserRole() {
        Cursor cursor = db.getLastIdCursor();
        if (cursor != null && cursor.moveToFirst()) {
            String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
            cursor.close();
            return role;
        }
        return null;
    }
    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}