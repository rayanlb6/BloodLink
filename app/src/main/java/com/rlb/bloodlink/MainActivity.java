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

            if (isUserFullyRegistered()) {
                // L'utilisateur a tout rempli → aller à la page médecin
                intent = new Intent(MainActivity.this, BloodLinkMedecinActivity.class);
            } else {
                // L'utilisateur n’a pas encore terminé son inscription → accueil
                intent = new Intent(MainActivity.this, BloodLinkAcceuilActivity.class);
            }

            startActivity(intent);
            finish();
        }, 3000);
    }

    private boolean isUserFullyRegistered() {
        Cursor cursor = db.getLastProgress();
        if (cursor != null && cursor.moveToFirst()) {
            String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String telephone = cursor.getString(cursor.getColumnIndexOrThrow("telephone"));
            String sexe = cursor.getString(cursor.getColumnIndexOrThrow("sexe"));
            String groupe = cursor.getString(cursor.getColumnIndexOrThrow("groupe"));
            String rhesus = cursor.getString(cursor.getColumnIndexOrThrow("rhesus"));

            cursor.close();

            // Vérifie si toutes les infos sont présentes (non nulles et non vides)
            return isNotEmpty(role)
                    && isNotEmpty(name)
                    && isNotEmpty(email)
                    && isNotEmpty(telephone)
                    && isNotEmpty(sexe)
                    && isNotEmpty(groupe)
                    && isNotEmpty(rhesus);
        }

        return false;
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}