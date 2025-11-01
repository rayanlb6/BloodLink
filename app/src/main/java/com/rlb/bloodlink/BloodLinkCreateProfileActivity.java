package com.rlb.bloodlink;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class BloodLinkCreateProfileActivity extends AppCompatActivity {

    TextInputEditText etName, etEmail, etTelephone, etSexe;
    TextView btnSignIn;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blood_link_create_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DatabaseHelper(this);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etTelephone = findViewById(R.id.etTelephone);
        etSexe = findViewById(R.id.etSexe);
        btnSignIn = findViewById(R.id.btn1);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String telephone = etTelephone.getText().toString().trim();
                String sexe = etSexe.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty() || telephone.isEmpty() || sexe.isEmpty()) {
                    Toast.makeText(BloodLinkCreateProfileActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Récupère le dernier client inséré
                Cursor cursor = dbHelper.getLastProgress();
                if (cursor != null && cursor.moveToFirst()) {
                    long lastId = cursor.getCount(); // ou mieux : récupérer directement l'ID du dernier (voir remarque en bas)
                    int rows = dbHelper.updatePI(lastId, name, email, telephone, sexe);
                    if (rows > 0) {
                        Toast.makeText(BloodLinkCreateProfileActivity.this, "Profil enregistré avec succès !", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(BloodLinkCreateProfileActivity.this, BloodLinkGroupeSanguinActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(BloodLinkCreateProfileActivity.this, "Erreur lors de l’enregistrement", Toast.LENGTH_SHORT).show();
                    }
                    cursor.close();
                } else {
                    Toast.makeText(BloodLinkCreateProfileActivity.this, "Aucun utilisateur trouvé à mettre à jour", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}