package com.rlb.bloodlink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;

public class BloodLinkRoleActivity extends AppCompatActivity {
     Client client;
     RelativeLayout donneurLayout, medecinLayout;
     DatabaseHelper dbHelper;
    DatabaseReference userRef;
    SharedPreferences prefs;
    // 🔹 Stockage dans SharedPreferences
    SharedPreferences.Editor editor ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blood_link_role);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation
        donneurLayout = findViewById(R.id.donneur_layout);
        medecinLayout = findViewById(R.id.medecin_layout);
        dbHelper = new DatabaseHelper(this);
        prefs = getSharedPreferences("BloodLinkPrefs", MODE_PRIVATE);
        editor = prefs.edit();
        // Lorsqu'on clique sur "Je suis donneur"
        donneurLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long id = dbHelper.insertClientRole("donneur");
                editor.putLong("userId", id);
                editor.putString("role","donneur");

                editor.apply(); // asynchrone
                if (id != -1) {
                    Toast.makeText(BloodLinkRoleActivity.this, "Rôle 'donneur' enregistré !", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(BloodLinkRoleActivity.this, BloodLinkCreateProfileActivity.class);
                    startActivity(intent);

                } else {
                    Toast.makeText(BloodLinkRoleActivity.this, "Erreur lors de l’enregistrement", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Lorsqu'on clique sur "Je suis medecin"
        medecinLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long id = dbHelper.insertClientRole("medecin");
                editor.putLong("userId", id);
                editor.putString("role","medecin");
                editor.apply(); // asynchrone
                if (id != -1) {
                    Toast.makeText(BloodLinkRoleActivity.this, "Rôle 'médecin' enregistré !", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(BloodLinkRoleActivity.this, BloodLinkCreateProfileActivity.class);
                    startActivity(intent);

                } else {
                    Toast.makeText(BloodLinkRoleActivity.this, "Erreur lors de l’enregistrement", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }





}