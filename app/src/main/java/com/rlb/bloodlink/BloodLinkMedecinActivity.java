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
import android.view.LayoutInflater;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BloodLinkMedecinActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private LinearLayout lSelectionGroupe, lGroupe1, lGroupe2, lSelectionZone, lCarte, lMenu, lLancerAlerte, layoutDonneurs;
    private String groupeSelectionneZone = "A+";
    private String groupeSelectionneAffichage = ""; // ✅ Pour filtrer les donneurs
    private ImageView iBack;
    private ScrollView sListeAttente, sListeDisponible;
    private int idMedecinConnecte;
    private TextView tvDonneur, tvMessage, tvLancerAlerte;
    private TextView lAp, lBm, lABm, lOm, lAm, lBp, lOp, lABp;
    private TextView lAp2, lBm2, lABm2, lOm2, lAm2, lBp2, lOp2, lABp2;
    private TextView tvValide, tvtired, longlat;
    private TextInputEditText zone;
    private boolean isConnectedToServer = false;

    // ✅ Conteneurs pour les listes
    private LinearLayout containerDonneurs; // Pour template.xml (donneurs disponibles)
    private LinearLayout containerAlertes;  // Pour template2.xml (alertes envoyées)

    // ✅ Suivi des alertes actives
    private Map<String, AlerteInfo> alertesActives = new HashMap<>();
    private DatabaseReference alertesRef;

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
        alertesRef = FirebaseDatabase.getInstance().getReference("alertes_history");

        SharedPreferences prefs = getSharedPreferences("BloodLinkPrefs", MODE_PRIVATE);
        idMedecinConnecte = (int) prefs.getLong("userId", 0);

        Log.d("Medecin", "ID Médecin : " + idMedecinConnecte);

        initializeViews();
        setupClickListeners();
        setupAlertForm();
        connectToServer();

        Cursor cursor = dbHelper.getLastIdCursor();
        String nom = "Utilisateur";
        if (cursor != null && cursor.moveToFirst()) {
            nom = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            cursor.close();
        }

        TextView tv1 = findViewById(R.id.tv1);
        tv1.setText("Bonjour Dr " + nom);

        // ✅ Écouter les réponses aux alertes en temps réel
        listenToAlertResponses();
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

        // ✅ Conteneurs pour les templates
        containerDonneurs = findViewById(R.id.scrollv1_linear_layout);
        containerAlertes = findViewById(R.id.scrollv2_linear_layout); // ⚠️ Ajouter cet ID dans votre XML
    }

    private void setupClickListeners() {
        // ✅ Listeners pour les groupes sanguins (affichage donneurs)
        View.OnClickListener gListener = v -> {
            resetGSelection();
            TextView clicked = (TextView) v;
            clicked.setBackgroundResource(R.drawable.btn_red2);
            groupeSelectionneAffichage = clicked.getText().toString();

            // ✅ Charger les donneurs du groupe sélectionné
            chargerDonneursPourGroupe(groupeSelectionneAffichage);
            Log.d("Medecin", "Affichage groupe : " + groupeSelectionneAffichage);
        };

        lOm.setOnClickListener(gListener);
        lBm.setOnClickListener(gListener);
        lAm.setOnClickListener(gListener);
        lABm.setOnClickListener(gListener);
        lOp.setOnClickListener(gListener);
        lBp.setOnClickListener(gListener);
        lAp.setOnClickListener(gListener);
        lABp.setOnClickListener(gListener);

        // ✅ Listeners pour les groupes sanguins (zone alerte)
        View.OnClickListener g2Listener = v -> {
            resetG2Selection();
            TextView clicked = (TextView) v;
            clicked.setBackgroundResource(R.drawable.btn_red2);
            groupeSelectionneZone = clicked.getText().toString();
            Log.d("Medecin", "Groupe sélectionné pour alerte : " + groupeSelectionneZone);
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
                // ✅ Charger le premier groupe par défaut
                if (groupeSelectionneAffichage.isEmpty()) {
                    groupeSelectionneAffichage = "A+";
                    lAp.setBackgroundResource(R.drawable.btn_red2);
                    chargerDonneursPourGroupe("A+");
                }
            } else if (clicked.getText().equals("Alertes")) {
                resetLayoutSelection();
                lLancerAlerte.setVisibility(View.VISIBLE);
                sListeAttente.setVisibility(View.VISIBLE);
                // ✅ Charger les alertes actives
                chargerAlertesEnvoyees();
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
            chargerAlertesEnvoyees();
        });

        // Bouton test (passer à l'écran donneur)
        TextView test = findViewById(R.id.test);
        test.setOnClickListener(v -> {
            startActivity(new Intent(this, BloodLinkDonneurActivity.class));
        });
    }

    // ✅ CHARGER LES DONNEURS PAR GROUPE SANGUIN (Template 1)
    private void chargerDonneursPourGroupe(String groupe) {
        containerDonneurs.removeAllViews();

        Log.d("Medecin", "🔍 Chargement donneurs groupe " + groupe);

        DatabaseReference donneursRef = FirebaseDatabase.getInstance()
                .getReference("clients");

        donneursRef.orderByChild("role").equalTo("donneur")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        int count = 0;
                        for (DataSnapshot donneurSnapshot : snapshot.getChildren()) {
                            Client donneur = donneurSnapshot.getValue(Client.class);

                            if (donneur != null && groupe.equals(donneur.getGroupe())) {
                                afficherDonneur(donneur);
                                count++;
                            }
                        }

                        if (count == 0) {
                            TextView tvEmpty = new TextView(BloodLinkMedecinActivity.this);
                            tvEmpty.setText("Aucun donneur " + groupe + " disponible");
                            tvEmpty.setTextSize(18);
                            tvEmpty.setPadding(20, 40, 20, 40);
                            tvEmpty.setGravity(android.view.Gravity.CENTER);
                            containerDonneurs.addView(tvEmpty);
                        }

                        Log.d("Medecin", "✅ " + count + " donneurs " + groupe + " chargés");
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("Medecin", "❌ Erreur chargement donneurs : " + error.getMessage());
                        Toast.makeText(BloodLinkMedecinActivity.this,
                                "Erreur de chargement", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ✅ AFFICHER UN DONNEUR (Template 1)
    private void afficherDonneur(Client donneur) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View donneurView = inflater.inflate(R.layout.template, containerDonneurs, false);

        TextView tvNom = donneurView.findViewById(R.id.tvNom);
        TextView tvGroupe = donneurView.findViewById(R.id.tvGroupe);
        TextView tvLieu = donneurView.findViewById(R.id.tvLieu);
        TextView tvDisponibilite = donneurView.findViewById(R.id.tvDisponibilite);
        TextView btnAlerter = donneurView.findViewById(R.id.btnAlerter);

        tvNom.setText(donneur.getName() != null ? donneur.getName() : "Donneur #" + donneur.getId());
        tvGroupe.setText(donneur.getGroupe());
        tvLieu.setText(donneur.getAdresse() != null ? donneur.getAdresse() : "Localisation non définie");

        // ✅ Statut de disponibilité
        if (donneur.isConnecte()) {
            tvDisponibilite.setText("Disponible");
            tvDisponibilite.setBackgroundResource(R.drawable.btn_green);
        } else {
            tvDisponibilite.setText("Hors ligne");
            tvDisponibilite.setBackgroundResource(R.drawable.btn_gray2);
        }

        // ✅ Bouton Alerter (envoyer alerte à ce donneur spécifique)
        btnAlerter.setOnClickListener(v -> {
            // Passer à l'écran d'envoi d'alerte avec pré-sélection du groupe
            resetLayoutSelection();
            lGroupe2.setVisibility(View.VISIBLE);
            lSelectionGroupe.setVisibility(View.VISIBLE);
            lSelectionZone.setVisibility(View.VISIBLE);
            lCarte.setVisibility(View.VISIBLE);

            // Pré-sélectionner le groupe du donneur
            groupeSelectionneZone = donneur.getGroupe();
            preselectGroupe(donneur.getGroupe());

            Toast.makeText(this, "Envoyez une alerte pour " + donneur.getName(),
                    Toast.LENGTH_SHORT).show();
        });

        containerDonneurs.addView(donneurView);
    }

    // ✅ Pré-sélectionner un groupe sanguin
    private void preselectGroupe(String groupe) {
        resetG2Selection();
        TextView groupeView = null;

        switch (groupe) {
            case "A+": groupeView = lAp2; break;
            case "A-": groupeView = lAm2; break;
            case "B+": groupeView = lBp2; break;
            case "B-": groupeView = lBm2; break;
            case "O+": groupeView = lOp2; break;
            case "O-": groupeView = lOm2; break;
            case "AB+": groupeView = lABp2; break;
            case "AB-": groupeView = lABm2; break;
        }

        if (groupeView != null) {
            groupeView.setBackgroundResource(R.drawable.btn_red2);
        }
    }

    // ✅ CHARGER LES ALERTES ENVOYÉES (Template 2)
    private void chargerAlertesEnvoyees() {
        containerAlertes.removeAllViews();

        Log.d("Medecin", "🔍 Chargement alertes du médecin #" + idMedecinConnecte);

        DatabaseReference alertesRef = FirebaseDatabase.getInstance()
                .getReference("alertes");

        alertesRef.orderByChild("id_medecin").equalTo(idMedecinConnecte)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        int count = 0;

                        for (DataSnapshot alerteSnapshot : snapshot.getChildren()) {
                            String alerteId = alerteSnapshot.getKey();
                            Alerte alerte = alerteSnapshot.getValue(Alerte.class);

                            if (alerte != null && "actif".equals(alerte.getStatut())) {
                                // Charger les réponses pour cette alerte
                                chargerReponsesAlerte(alerteId, alerte);
                                count++;
                            }
                        }

                        if (count == 0) {
                            TextView tvEmpty = new TextView(BloodLinkMedecinActivity.this);
                            tvEmpty.setText("Aucune alerte active");
                            tvEmpty.setTextSize(18);
                            tvEmpty.setPadding(20, 40, 20, 40);
                            tvEmpty.setGravity(android.view.Gravity.CENTER);
                            containerAlertes.addView(tvEmpty);
                        }

                        Log.d("Medecin", "✅ " + count + " alertes actives");
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("Medecin", "❌ Erreur chargement alertes : " + error.getMessage());
                    }
                });
    }

    // ✅ CHARGER LES RÉPONSES D'UNE ALERTE
    private void chargerReponsesAlerte(String alerteId, Alerte alerte) {
        alertesRef.orderByChild("alerte").equalTo(alerteId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<AlerteData> reponses = new ArrayList<>();

                        for (DataSnapshot reponseSnapshot : snapshot.getChildren()) {
                            AlerteData reponse = reponseSnapshot.getValue(AlerteData.class);
                            if (reponse != null) {
                                reponses.add(reponse);
                            }
                        }

                        // Si aucune réponse, afficher "En attente"
                        if (reponses.isEmpty()) {
                            afficherAlerteEnAttente(alerteId, alerte);
                        } else {
                            // Afficher chaque réponse
                            for (AlerteData reponse : reponses) {
                                afficherReponseAlerte(alerteId, alerte, reponse);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("Medecin", "Erreur chargement réponses : " + error.getMessage());
                    }
                });
    }

    // ✅ AFFICHER UNE ALERTE EN ATTENTE (Template 2)
    private void afficherAlerteEnAttente(String alerteId, Alerte alerte) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View alerteView = inflater.inflate(R.layout.template2, containerAlertes, false);

        TextView tvNom = alerteView.findViewById(R.id.tvNom2);
        TextView tvLieu = alerteView.findViewById(R.id.tvLieu2);
        TextView tvGroupe = alerteView.findViewById(R.id.tvGroupe2);
        TextView tvStatu = alerteView.findViewById(R.id.tvStatu);
        TextView tvAction = alerteView.findViewById(R.id.tvAction);

        tvNom.setText("Alerte envoyée");
        tvLieu.setText(alerte.getZone());
        tvGroupe.setText(alerte.getGroupe());
        tvStatu.setText("En attente");
        tvStatu.setBackgroundResource(R.drawable.btn_gray2);

        tvAction.setText("Annuler");
        tvAction.setOnClickListener(v -> {
            annulerAlerte(alerteId);
            containerAlertes.removeView(alerteView);
        });

        containerAlertes.addView(alerteView);
    }

    // ✅ AFFICHER UNE RÉPONSE À UNE ALERTE (Template 2)
    private void afficherReponseAlerte(String alerteId, Alerte alerte, AlerteData reponse) {
        // Charger les infos du donneur
        DatabaseReference donneurRef = FirebaseDatabase.getInstance()
                .getReference("clients")
                .child(String.valueOf(reponse.getIdDonneur()));

        donneurRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Client donneur = snapshot.getValue(Client.class);

                LayoutInflater inflater = LayoutInflater.from(BloodLinkMedecinActivity.this);
                View alerteView = inflater.inflate(R.layout.template2, containerAlertes, false);

                TextView tvNom = alerteView.findViewById(R.id.tvNom2);
                TextView tvLieu = alerteView.findViewById(R.id.tvLieu2);
                TextView tvGroupe = alerteView.findViewById(R.id.tvGroupe2);
                TextView tvStatu = alerteView.findViewById(R.id.tvStatu);
                TextView tvAction = alerteView.findViewById(R.id.tvAction);

                if (donneur != null) {
                    tvNom.setText(donneur.getName());
                    tvLieu.setText(donneur.getAdresse() != null ? donneur.getAdresse() : alerte.getZone());
                    tvGroupe.setText(donneur.getGroupe());
                } else {
                    tvNom.setText("Donneur #" + reponse.getIdDonneur());
                    tvLieu.setText(alerte.getZone());
                    tvGroupe.setText(alerte.getGroupe());
                }

                // ✅ Statut selon la réponse
                if (reponse.isAccept()) {
                    tvStatu.setText("✅ Accepté");
                    tvStatu.setBackgroundResource(R.drawable.btn_green);
                } else {
                    tvStatu.setText("❌ Refusé");
                    tvStatu.setBackgroundResource(R.drawable.btn_red2);
                }

                tvAction.setText("Supprimer");
                tvAction.setOnClickListener(v -> {
                    containerAlertes.removeView(alerteView);
                    Toast.makeText(BloodLinkMedecinActivity.this,
                            "Réponse supprimée", Toast.LENGTH_SHORT).show();
                });

                containerAlertes.addView(alerteView);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Medecin", "Erreur chargement donneur : " + error.getMessage());
            }
        });
    }

    // ✅ ANNULER UNE ALERTE
    private void annulerAlerte(String alerteId) {
        DatabaseReference alerteRef = FirebaseDatabase.getInstance()
                .getReference("alertes")
                .child(alerteId);

        alerteRef.child("statut").setValue("annule")
                .addOnSuccessListener(aVoid -> {
                    Log.d("Medecin", "✅ Alerte annulée : " + alerteId);
                    Toast.makeText(this, "Alerte annulée", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Medecin", "❌ Erreur annulation : " + e.getMessage());
                    Toast.makeText(this, "Erreur d'annulation", Toast.LENGTH_SHORT).show();
                });
    }

    // ✅ ÉCOUTER LES RÉPONSES EN TEMPS RÉEL
    private void listenToAlertResponses() {
        SocketManager.getInstance().onAlertResponse(args -> {
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

                    // ✅ Rafraîchir l'affichage si on est sur l'écran alertes
                    if (sListeAttente.getVisibility() == View.VISIBLE) {
                        chargerAlertesEnvoyees();
                    }
                });

            } catch (JSONException e) {
                Log.e("Medecin", "Erreur parsing réponse : " + e.getMessage(), e);
            }
        });
    }

    private void setupAlertForm() {
        TextInputEditText rayon = findViewById(R.id.rayon);

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

        tvValide.setOnClickListener(v -> {
            String rayonStr = rayon.getText().toString().trim();
            String zoneStr = zone.getText().toString().trim();
            String coordsStr = longlat.getText().toString().trim();

            if (zoneStr.isEmpty() || rayonStr.isEmpty() || groupeSelectionneZone.isEmpty() ||
                    coordsStr.isEmpty() || !coordsStr.contains(",")) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isConnectedToServer) {
                Toast.makeText(this, "Connexion au serveur en cours...", Toast.LENGTH_SHORT).show();
                connectToServer();
                new Handler().postDelayed(() -> tvValide.performClick(), 2000);
                return;
            }

            String[] parts = coordsStr.split(",");
            if (parts.length == 2) {
                try {
                    double lat = Double.parseDouble(parts[0].trim());
                    double lon = Double.parseDouble(parts[1].trim());
                    double rayonKm = Double.parseDouble(rayonStr);

                    sendAlerte(idMedecinConnecte, zoneStr, lat, lon, rayonKm, groupeSelectionneZone);

                    zone.setText("");
                    rayon.setText("");
                    longlat.setText("");
                    resetG2Selection();

                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Format invalide", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void connectToServer() {
        Log.d("Medecin", "Connexion au serveur Socket.IO...");

        SocketManager socketManager = SocketManager.getInstance();
        socketManager.connect();

        socketManager.onRegistered(args -> {
            runOnUiThread(() -> {
                isConnectedToServer = true;
                Log.d("Medecin", "✅ Enregistré sur le serveur");
                Toast.makeText(this, "✅ Connecté au serveur", Toast.LENGTH_SHORT).show();
            });
        });

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
    }

    // ✅ CLASSE INTERNE POUR STOCKER LES INFOS D'ALERTE
    private static class AlerteInfo {
        String alerteId;
        String zone;
        String groupe;
        long timestamp;

        AlerteInfo(String alerteId, String zone, String groupe) {
            this.alerteId = alerteId;
            this.zone = zone;
            this.groupe = groupe;
            this.timestamp = System.currentTimeMillis();
        }
    }
}