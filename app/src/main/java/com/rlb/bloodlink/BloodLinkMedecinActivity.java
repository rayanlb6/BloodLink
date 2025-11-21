package com.rlb.bloodlink;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BloodLinkMedecinActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private LinearLayout lSelectionGroupe, lGroupe1, lGroupe2, lSelectionZone, lCarte, lMenu, lLancerAlerte, layoutDonneurs;
    private String groupeSelectionneZone = "A+";
    private ImageView iBack;
    private ScrollView sListeAttente, sListeDisponible;
    private int idMedecinConnecte;
    private TextView tvDonneur, tvMessage, tvLancerAlerte;
    private TextView lAp, lBm, lABm, lOm, lAm, lBp, lOp, lABp;
    private TextView lAp2, lBm2, lABm2, lOm2, lAm2, lBp2, lOp2, lABp2;
    private TextView tvValide, tvtired, longlat;
    private TextInputEditText zone;
    private boolean isConnectedToServer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("Medecin", "========== DÉMARRAGE MÉDECIN ==========");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blood_link_medecin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("BloodLinkPrefs", MODE_PRIVATE);
        idMedecinConnecte = (int) prefs.getLong("userId", 0);

        Log.d("Medecin", "ID Médecin : " + idMedecinConnecte);

        initializeViews();
        setupClickListeners();
        setupAlertForm();

        // ✅ CONNEXION AU SERVEUR SOCKET.IO
        connectToServer();

        Cursor cursor = dbHelper.getLastIdCursor();
        String nom = "Utilisateur";
        if (cursor != null && cursor.moveToFirst()) {
            nom = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            cursor.close();
        }

        TextView tv1 = findViewById(R.id.tv1);
        tv1.setText("Bonjour Dr " + nom);
    }

    private void initializeViews() {
        lSelectionGroupe = findViewById(R.id.ll5);
        lGroupe1 = findViewById(R.id.ll3);
        lGroupe2 = findViewById(R.id.ll6);
        lSelectionZone = findViewById(R.id.ll7);
        lMenu = findViewById(R.id.ll8);
        lLancerAlerte = findViewById(R.id.ll4);
        sListeAttente = findViewById(R.id.scrollv2);
        sListeDisponible = findViewById(R.id.scrollv1);
        tvLancerAlerte = findViewById(R.id.tvla);
        iBack = findViewById(R.id.back);
        lCarte = findViewById(R.id.ll9);

        lAp = findViewById(R.id.Ap);
        lAm = findViewById(R.id.Am);
        lOp = findViewById(R.id.Op);
        lOm = findViewById(R.id.Om);
        lABp = findViewById(R.id.ABp);
        lABm = findViewById(R.id.ABm);
        lBp = findViewById(R.id.Bp);
        lBm = findViewById(R.id.Bm);

        lAp2 = findViewById(R.id.Ap2);
        lAm2 = findViewById(R.id.Am2);
        lOp2 = findViewById(R.id.Op2);
        lOm2 = findViewById(R.id.Om2);
        lABp2 = findViewById(R.id.ABp2);
        lABm2 = findViewById(R.id.ABm2);
        lBp2 = findViewById(R.id.Bp2);
        lBm2 = findViewById(R.id.Bm2);

        tvDonneur = findViewById(R.id.tvDonneur);
        tvMessage = findViewById(R.id.tvMessage);
        zone = findViewById(R.id.zone);
        tvtired = findViewById(R.id.tvTired);
        longlat = findViewById(R.id.longlat);
        tvValide = findViewById(R.id.tvValide);

        layoutDonneurs = findViewById(R.id.scrollv1_linear_layout);
    }

    private void setupClickListeners() {
        // Listeners pour les groupes sanguins (première sélection)
        View.OnClickListener gListener = v -> {
            resetGSelection();
            TextView clicked = (TextView) v;
            clicked.setBackgroundResource(R.drawable.btn_red2);
        };

        lOm.setOnClickListener(gListener);
        lBm.setOnClickListener(gListener);
        lAm.setOnClickListener(gListener);
        lABm.setOnClickListener(gListener);
        lOp.setOnClickListener(gListener);
        lBp.setOnClickListener(gListener);
        lAp.setOnClickListener(gListener);
        lABp.setOnClickListener(gListener);

        // Listeners pour les groupes sanguins (zone)
        View.OnClickListener g2Listener = v -> {
            resetG2Selection();
            TextView clicked = (TextView) v;
            clicked.setBackgroundResource(R.drawable.btn_red2);
            groupeSelectionneZone = clicked.getText().toString();
            Log.d("Medecin", "Groupe sélectionné : " + groupeSelectionneZone);
        };

        lOm2.setOnClickListener(g2Listener);
        lBm2.setOnClickListener(g2Listener);
        lAm2.setOnClickListener(g2Listener);
        lABm2.setOnClickListener(g2Listener);
        lOp2.setOnClickListener(g2Listener);
        lBp2.setOnClickListener(g2Listener);
        lAp2.setOnClickListener(g2Listener);
        lABp2.setOnClickListener(g2Listener);

        // Menu principal
        View.OnClickListener groupeListener = v -> {
            resetGroupeSelection();
            TextView clicked = (TextView) v;
            clicked.setBackgroundResource(R.drawable.btn_red2);
            clicked.setTextColor(getColor(R.color.white));

            if (clicked.getText().equals("Donneurs")) {
                resetLayoutSelection();
                lGroupe1.setVisibility(View.VISIBLE);
                sListeDisponible.setVisibility(View.VISIBLE);
            } else if (clicked.getText().equals("Alertes")) {
                resetLayoutSelection();
                lLancerAlerte.setVisibility(View.VISIBLE);
                sListeAttente.setVisibility(View.VISIBLE);
            }
        };

        tvDonneur.setOnClickListener(groupeListener);
        tvMessage.setOnClickListener(groupeListener);

        // Bouton "Lancer Alerte"
        tvLancerAlerte.setOnClickListener(v -> {
            resetLayoutSelection();
            lGroupe2.setVisibility(View.VISIBLE);
            lSelectionGroupe.setVisibility(View.VISIBLE);
            lSelectionZone.setVisibility(View.VISIBLE);
            lCarte.setVisibility(View.VISIBLE);
        });

        // Bouton retour
        iBack.setOnClickListener(v -> {
            resetLayoutSelection();
            lLancerAlerte.setVisibility(View.VISIBLE);
            sListeAttente.setVisibility(View.VISIBLE);
        });

        // Bouton test (passer à l'écran donneur)
        TextView test = findViewById(R.id.test);
        test.setOnClickListener(v -> {
            startActivity(new Intent(this, BloodLinkDonneurActivity.class));
        });
    }

    private void setupAlertForm() {
        TextInputEditText rayon = findViewById(R.id.rayon);

        // Obtenir les coordonnées
        tvtired.setOnClickListener(v -> {
            String locationName = zone.getText().toString().trim();

            if (!locationName.isEmpty()) {
                List<Double> coordinates = getCoordinates(locationName);

                if (coordinates != null && coordinates.size() == 2) {
                    double latitude = coordinates.get(0);
                    double longitude = coordinates.get(1);
                    longlat.setText(latitude + "," + longitude);
                    Log.d("Medecin", "Coordonnées obtenues : " + latitude + "," + longitude);
                } else {
                    longlat.setText("Coordonnées introuvables");
                    Toast.makeText(this, "Impossible de trouver cette zone", Toast.LENGTH_SHORT).show();
                }
            } else {
                longlat.setText("Veuillez entrer une zone");
            }
        });

        // Valider et envoyer l'alerte
        tvValide.setOnClickListener(v -> {
            String rayonStr = rayon.getText().toString().trim();
            String zoneStr = zone.getText().toString().trim();
            String coordsStr = longlat.getText().toString().trim();

            // Validations
            if (zoneStr.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer une zone", Toast.LENGTH_SHORT).show();
                return;
            }

            if (rayonStr.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer un rayon valide", Toast.LENGTH_SHORT).show();
                return;
            }

            if (groupeSelectionneZone.isEmpty()) {
                Toast.makeText(this, "Veuillez sélectionner un groupe sanguin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (coordsStr.isEmpty() || !coordsStr.contains(",")) {
                Toast.makeText(this, "Veuillez d'abord obtenir les coordonnées", Toast.LENGTH_SHORT).show();
                return;
            }

            // Vérifier la connexion au serveur
            if (!isConnectedToServer) {
                Toast.makeText(this, "Connexion au serveur en cours...", Toast.LENGTH_SHORT).show();
                // Réessayer de se connecter
                connectToServer();
                // Réessayer après 2 secondes
                new Handler().postDelayed(() -> tvValide.performClick(), 2000);
                return;
            }

            // Parsing des coordonnées
            String[] parts = coordsStr.split(",");
            if (parts.length == 2) {
                try {
                    double lat = Double.parseDouble(parts[0].trim());
                    double lon = Double.parseDouble(parts[1].trim());
                    double rayonKm = Double.parseDouble(rayonStr);

                    // ✅ ENVOYER L'ALERTE VIA SOCKET.IO
                    sendAlerte(idMedecinConnecte, zoneStr, lat, lon, rayonKm, groupeSelectionneZone);

                    // Réinitialiser les champs
                    zone.setText("");
                    rayon.setText("");
                    longlat.setText("");
                    resetG2Selection();

                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Format de coordonnées invalide", Toast.LENGTH_SHORT).show();
                    Log.e("Medecin", "Erreur parsing : " + e.getMessage());
                }
            } else {
                Toast.makeText(this, "Format invalide. Exemple attendu: 3.874,11.513", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ CONNEXION AU SERVEUR SOCKET.IO
    private void connectToServer() {
        Log.d("Medecin", "Connexion au serveur Socket.IO...");

        SocketManager socketManager = SocketManager.getInstance();
        socketManager.connect();

        // Écouter la confirmation d'enregistrement
        socketManager.onRegistered(args -> {
            runOnUiThread(() -> {
                isConnectedToServer = true;
                Log.d("Medecin", "✅ Enregistré sur le serveur");
                Toast.makeText(this, "✅ Connecté au serveur", Toast.LENGTH_SHORT).show();
            });
        });

        // Écouter les réponses des donneurs
        socketManager.onAlertResponse(args -> {
            try {
                JSONObject response = (JSONObject) args[0];

                String alerteId = response.getString("alerteId");
                int donneurId = response.getInt("donneurId");
                boolean accepted = response.getBoolean("accepted");

                runOnUiThread(() -> {
                    String message = "🩸 Donneur #" + donneurId + " a " +
                            (accepted ? "✅ ACCEPTÉ" : "❌ REFUSÉ") + " l'alerte";

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    Log.d("Medecin", message);

                    // TODO: Afficher dans l'interface (liste des réponses)
                });

            } catch (JSONException e) {
                Log.e("Medecin", "Erreur parsing réponse : " + e.getMessage(), e);
            }
        });

        // Confirmation d'envoi d'alerte
        socketManager.onAlertSent(args -> {
            try {
                JSONObject result = (JSONObject) args[0];
                int notificationsSent = result.getInt("notificationsSent");
                String alerteId = result.getString("alerteId");

                runOnUiThread(() -> {
                    String message = "📢 " + notificationsSent + " donneurs notifiés\nID: " + alerteId;
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    Log.d("Medecin", "Alerte envoyée : " + notificationsSent + " notifications");
                });

            } catch (JSONException e) {
                Log.e("Medecin", "Erreur parsing confirmation : " + e.getMessage(), e);
            }
        });

        // S'enregistrer sur le serveur
        new Handler().postDelayed(() -> {
            if (socketManager.isConnected()) {
                socketManager.register(idMedecinConnecte, "medecin", "", 0, 0);
                Log.d("Medecin", "Enregistrement en tant que médecin #" + idMedecinConnecte);
            } else {
                Log.w("Medecin", "Socket non connecté, nouvelle tentative...");
                connectToServer();
            }
        }, 1000);
    }

    // ✅ ENVOYER UNE ALERTE VIA SOCKET.IO
    private void sendAlerte(int idMedecin, String zone, double latitude, double longitude,
                            double rayon, String groupe) {

        Log.d("Medecin", "========== ENVOI ALERTE ==========");
        Log.d("Medecin", "Médecin ID: " + idMedecin);
        Log.d("Medecin", "Zone: " + zone);
        Log.d("Medecin", "Groupe: " + groupe);
        Log.d("Medecin", "Rayon: " + rayon + " km");
        Log.d("Medecin", "Position: " + latitude + "," + longitude);
        Log.d("Medecin", "===================================");

        SocketManager.getInstance().sendAlert(
                idMedecin,
                zone,
                groupe,
                rayon,
                latitude,
                longitude
        );

        Toast.makeText(this, "🚨 Alerte envoyée aux donneurs " + groupe, Toast.LENGTH_LONG).show();
    }

    private List<Double> getCoordinates(String locationName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Double> location = new ArrayList<>();

        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName + ", Cameroun", 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();

                location.add(latitude);
                location.add(longitude);

                Log.d("Medecin", "Géocodage réussi : " + locationName + " -> " +
                        latitude + "," + longitude);
            } else {
                Log.w("Medecin", "Aucun résultat pour : " + locationName);
            }
        } catch (IOException e) {
            Log.e("Medecin", "Erreur géocodage : " + e.getMessage(), e);
            Toast.makeText(this, "Erreur de connexion au service de géocodage", Toast.LENGTH_SHORT).show();
        }

        return location;
    }

    private void resetGroupeSelection() {
        TextView[] groupViews = {tvDonneur, tvMessage};
        for (TextView tv : groupViews) {
            tv.setBackgroundResource(R.drawable.btn_gray2);
            tv.setTextColor(getColor(R.color.gray4));
        }
    }

    private void resetLayoutSelection() {
        LinearLayout[] groupLayouts = {lSelectionGroupe, lGroupe1, lGroupe2, lSelectionZone,
                lMenu, lLancerAlerte, lCarte};
        ScrollView[] groupViews = {sListeDisponible, sListeAttente};

        for (LinearLayout ll : groupLayouts) {
            ll.setVisibility(View.GONE);
        }
        for (ScrollView sv : groupViews) {
            sv.setVisibility(View.GONE);
        }
    }

    private void resetGSelection() {
        TextView[] groupViews = {lOp, lAp, lBm, lABm, lABp, lAm, lBp, lOm};
        for (TextView tv : groupViews) {
            tv.setBackgroundResource(R.drawable.btn_red5);
        }
    }

    private void resetG2Selection() {
        TextView[] groupViews = {lOp2, lAp2, lBm2, lABm2, lABp2, lAm2, lBp2, lOm2};
        for (TextView tv : groupViews) {
            tv.setBackgroundResource(R.drawable.btn_red5);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reconnecter si déconnecté
        SocketManager socketManager = SocketManager.getInstance();
        if (!socketManager.isConnected()) {
            Log.d("Medecin", "Reconnexion au serveur...");
            connectToServer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("Medecin", "Destruction de l'activité");

        // On ne déconnecte pas le socket pour que les réponses continuent d'arriver
        // SocketManager.getInstance().disconnect();
    }
}