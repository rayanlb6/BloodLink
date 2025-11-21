package com.rlb.bloodlink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class BloodLinkCreateProfileActivity extends AppCompatActivity {

    TextInputEditText etName, etEmail, etTelephone,etAdresse ;
    AutoCompleteTextView etSexe;
    String[] sexes = new String[]{"Masculin", "Féminin"};
    TextView btnSignIn;
    ImageView fleches;
    DatabaseHelper dbHelper;
    final String DONNEUR="donneur";
    final String MEDECIN="medecin";
    String testRole;
    long lastId;

    // 🔥 Firebase reference
    DatabaseReference userRef;



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

        // Initialisation SQLite
        dbHelper = new DatabaseHelper(this);


        // Initialisation Firebase
        userRef = FirebaseDatabase.getInstance().getReference("users");

        // Liaison avec les vues
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etTelephone = findViewById(R.id.etTelephone);
        etSexe = findViewById(R.id.etSexe);
        etAdresse = findViewById(R.id.etAdresse);
        btnSignIn = findViewById(R.id.btn1);
        fleches = findViewById(R.id.fleche_retour);

        fleches.setOnClickListener(v -> finish());

        // Adapter pour le sexe
        ArrayAdapter<String> adapterSexe = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                sexes
        );
        etSexe.setAdapter(adapterSexe);
        etSexe.setOnClickListener(v -> etSexe.showDropDown());

        // --- Sauvegarde du profil ---
        btnSignIn.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String telephone = etTelephone.getText().toString().trim();
            String sexe = etSexe.getText().toString().trim();
            String adresse = etAdresse.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || telephone.isEmpty() || sexe.isEmpty() ||adresse.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔹 Étape 1 : Mise à jour locale (SQLite)
            Cursor cursor = dbHelper.getLastIdCursor();
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex("id");
                int roleIndex = cursor.getColumnIndex("role");


                if(idIndex != -1 && roleIndex != -1) {
                     lastId = cursor.getLong(idIndex);
                    testRole = cursor.getString(roleIndex);
                } else {
                    Toast.makeText(this, "Erreur : colonnes introuvables", Toast.LENGTH_SHORT).show();
                    return;
                }

                int rows = dbHelper.updateNom(lastId, name);
                int rows2 = dbHelper.updateEmail(lastId, email);
                int rows3 = dbHelper.updateTelephone(lastId,telephone);
                int rows4 = dbHelper.updateAdresse(lastId,adresse);
                int rows5 = dbHelper.updateSexe(lastId, sexe);


                if (rows > 0 && rows2 > 0 && rows3 > 0 && rows4 > 0 && rows5 > 0) {
                    Toast.makeText(this, "Profil enregistré localement !", Toast.LENGTH_SHORT).show();
                    // 🔹 Étape 2 : Enregistrement dans Firebase
                    saveProfileToFirebase(name, email, telephone,adresse, sexe);
                } else {
                    Toast.makeText(this, "Erreur lors de l’enregistrement local", Toast.LENGTH_SHORT).show();
                }
                cursor.close();
            } else {
                Toast.makeText(this, "Aucun utilisateur trouvé à mettre à jour", Toast.LENGTH_SHORT).show();
            }
                if(testRole.equals(DONNEUR)){
            Intent intent=new Intent(BloodLinkCreateProfileActivity.this, BloodLinkGroupeSanguinActivity.class);
            startActivity(intent);
                }
                else if(testRole.equals(MEDECIN)){
                Intent intent=new Intent(BloodLinkCreateProfileActivity.this, BloodLinkMedecinActivity.class);
                startActivity(intent);
                finish();
            }

        });
    }

    private void saveProfileToFirebase(String name, String email, String telephone,String adresse, String sexe) {
        String userId = userRef.push().getKey(); // crée un ID unique Firebase

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("telephone", telephone);
        userData.put("adresse",adresse);
        userData.put("sexe", sexe);
        userData.put("latitude", 0.0); // tu mettras la vraie position plus tard
        userData.put("longitude", 0.0);

        userRef.child(userId).setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profil envoyé sur Firebase !", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, BloodLinkGroupeSanguinActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur Firebase : " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

}
