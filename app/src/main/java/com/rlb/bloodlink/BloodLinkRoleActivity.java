package com.rlb.bloodlink;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BloodLinkRoleActivity extends AppCompatActivity {
     Client client;
     RelativeLayout donneurLayout, medecinLayout;
     DatabaseHelper dbHelper;
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

        // Lorsqu'on clique sur "Je suis donneur"
        donneurLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long id = dbHelper.insertClientRole("donneur");
                if (id != -1) {
                    Toast.makeText(BloodLinkRoleActivity.this, "Rôle 'donneur' enregistré !", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(BloodLinkRoleActivity.this, BloodLinkGroupeSanguinActivity.class);
                    startActivity(intent);
                    finish();
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
                if (id != -1) {
                    Toast.makeText(BloodLinkRoleActivity.this, "Rôle 'médecin' enregistré !", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(BloodLinkRoleActivity.this, BloodLinkGroupeSanguinActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(BloodLinkRoleActivity.this, "Erreur lors de l’enregistrement", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}