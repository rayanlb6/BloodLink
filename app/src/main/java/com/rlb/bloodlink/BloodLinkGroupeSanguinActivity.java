package com.rlb.bloodlink;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BloodLinkGroupeSanguinActivity extends AppCompatActivity {

    private TextView tv2, tv3, tv4, tv5, tv6, tv7, tv8, btn1;
    private ImageView fleches;
    private String selectedGroupe = "";
    private String selectedRhesus = "";

    private String groupe = "";

    private long lastClientId = -1;

    private DatabaseHelper db;
    private DatabaseReference firebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blood_link_groupe_sanguin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);
        firebaseRef = FirebaseDatabase.getInstance().getReference("clients");

        // --- Récupération du dernier utilisateur avec ID réel ---
        Cursor cursor = db.getLastIdCursor();
        if (cursor != null && cursor.moveToFirst()) {
            lastClientId = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            cursor.close();
        }

        // --- Liaisons des vues ---
        tv2 = findViewById(R.id.tv2);
        tv3 = findViewById(R.id.tv3);
        tv4 = findViewById(R.id.tv4);
        tv5 = findViewById(R.id.tv5);
        tv6 = findViewById(R.id.tv6);
        tv7 = findViewById(R.id.tv7);
        tv8 = findViewById(R.id.tv8);
        btn1 = findViewById(R.id.btn1);
        fleches = findViewById(R.id.fleche);

        fleches.setOnClickListener(v -> finish());

        // --- Gestion du clic sur groupe sanguin ---
        View.OnClickListener groupeListener = v -> {
            resetGroupeSelection();
            TextView clicked = (TextView) v;
            clicked.setBackgroundResource(R.drawable.btn_red2);
            clicked.setTextColor(getColor(R.color.white));
            selectedGroupe = clicked.getText().toString();
        };
        tv2.setOnClickListener(groupeListener);
        tv3.setOnClickListener(groupeListener);
        tv4.setOnClickListener(groupeListener);
        tv5.setOnClickListener(groupeListener);
        tv6.setOnClickListener(groupeListener);

        // --- Gestion du clic sur rhésus ---
        View.OnClickListener rhesusListener = v -> {
            resetRhesusSelection();
            TextView clicked = (TextView) v;
            clicked.setBackgroundResource(R.drawable.btn_red2);
            clicked.setTextColor(getColor(R.color.white));
            selectedRhesus = clicked.getText().toString();
        };
        tv7.setOnClickListener(rhesusListener);
        tv8.setOnClickListener(rhesusListener);

        // --- Bouton d’enregistrement ---
        btn1.setOnClickListener(v -> {
            if (selectedGroupe.isEmpty() || selectedRhesus.isEmpty()) {
                Toast.makeText(this, "Veuillez sélectionner un groupe et un rhésus", Toast.LENGTH_SHORT).show();
                return;
            }

            if (lastClientId == -1) {
                Toast.makeText(this, "Aucun utilisateur trouvé dans la base", Toast.LENGTH_SHORT).show();
                return;
            }
            groupe=selectedGroupe+selectedRhesus;
            // 🔹 Mise à jour SQLite
            int rows = db.updateGroupeRhesus(lastClientId, groupe);

            if (rows > 0) {
                // 🔹 Mise à jour Firebase
                firebaseRef.child(String.valueOf(lastClientId)).child("groupe").setValue(groupe);

                Toast.makeText(this, "Groupe sanguin enregistré : " + selectedGroupe + selectedRhesus, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(BloodLinkGroupeSanguinActivity.this, BloodLinkDonneurActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            } else {
                Toast.makeText(this, "Erreur lors de l’enregistrement", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetGroupeSelection() {
        TextView[] groupViews = {tv2, tv3, tv4, tv5, tv6};
        for (TextView tv : groupViews) {
            tv.setBackgroundResource(R.drawable.btn_red4);
            tv.setTextColor(getColor(R.color.red_bloodlink));
        }
    }

    private void resetRhesusSelection() {
        TextView[] rhesusViews = {tv7, tv8};
        for (TextView tv : rhesusViews) {
            tv.setBackgroundResource(R.drawable.btn_red4);
            tv.setTextColor(getColor(R.color.red_bloodlink));
        }
    }
}
