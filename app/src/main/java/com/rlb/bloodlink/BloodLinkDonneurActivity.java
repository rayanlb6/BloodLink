package com.rlb.bloodlink;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.Manifest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BloodLinkDonneurActivity extends AppCompatActivity {

    LinearLayout containerAlertes;
    DatabaseHelper dbHelper;
    int idDonneur;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 102;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;
    private LocationManager lm;
    private Client currentClient;
    private boolean isRegistered = false; // ✅ NOUVEAU : Flag pour éviter les doubles enregistrements

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blood_link_donneur);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.d("Donneur", "========== DÉMARRAGE DONNEUR ==========");

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        dbHelper = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("BloodLinkPrefs", MODE_PRIVATE);
        idDonneur = (int) prefs.getLong("userId", 0);

        Log.d("Donneur", "ID Donneur : " + idDonneur);

        Cursor cursor = dbHelper.getLastIdCursor();
        String nom = "Utilisateur";
        if (cursor != null && cursor.moveToFirst()) {
            nom = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            cursor.close();
        }

        TextView tv1 = findViewById(R.id.tv1);
        tv1.setText("Bonjour " + nom);

        containerAlertes = findViewById(R.id.containerAlertes);

        // ✅ ÉTAPE 1 : Connecter au serveur Socket.IO en premier
        Log.d("Donneur", "Étape 1 : Connexion au serveur Socket.IO...");
        connectToServer();

        // ✅ ÉTAPE 2 : Charger le profil client
        Log.d("Donneur", "Étape 2 : Chargement du profil...");
        dbHelper.getClient(idDonneur, new ClientCallback() {
            @Override
            public void onClientLoaded(Client client) {
                if (client == null) {
                    Log.e("Donneur", "❌ Client NULL retourné par Firebase");
                    Toast.makeText(BloodLinkDonneurActivity.this,
                            "Erreur : Profil non trouvé", Toast.LENGTH_LONG).show();
                    return;
                }

                currentClient = client;
                Log.d("Donneur", "✅ Client chargé : " + client.getName());
                Log.d("Donneur", "   - Groupe : " + client.getGroupe());
                Log.d("Donneur", "   - Lat : " + client.getLatitude());
                Log.d("Donneur", "   - Lon : " + client.getLongitude());

                // ✅ ÉTAPE 3 : Enregistrer sur le serveur Socket MAINTENANT
                registerOnServer();
            }

            @Override
            public void onError(Exception e) {
                Log.e("Donneur", "❌ Erreur chargement client : " + e.getMessage(), e);
                Toast.makeText(BloodLinkDonneurActivity.this,
                        "Impossible de charger vos informations", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ ÉTAPE 4 : Démarrer les autres services
        initFCM();
        startLocationService();
        dbHelper.updateConnecte(idDonneur, true);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d("Permissions", "Code: " + requestCode + ", Résultat: " +
                (grantResults.length > 0 ? grantResults[0] : "vide"));

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "✅ Permission de localisation accordée");
                Toast.makeText(this, "Permission accordée", Toast.LENGTH_SHORT).show();
                new android.os.Handler().postDelayed(this::startLocationService, 500);
            } else {
                Log.w("Permissions", "❌ Permission de localisation refusée");
                Toast.makeText(this, "L'application a besoin de la localisation pour fonctionner",
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "✅ Permission arrière-plan accordée");
                Toast.makeText(this, "Localisation en arrière-plan activée", Toast.LENGTH_SHORT).show();
            } else {
                Log.w("Permissions", "⚠️ Permission arrière-plan refusée");
                Toast.makeText(this, "La localisation fonctionnera uniquement quand l'app est ouverte",
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "✅ Permission notifications accordée");
            } else {
                Log.w("Permissions", "❌ Permission notifications refusée");
            }
        }
    }

    private void initFCM() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "❌ Échec récupération token", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d("FCM", "✅ Token FCM : " + token.substring(0, 30) + "...");

                    SharedPreferences prefs = getSharedPreferences("BloodLinkPrefs", MODE_PRIVATE);
                    prefs.edit().putString("fcmToken", token).apply();

                    dbHelper.updateFcmToken(idDonneur, token);
                    Toast.makeText(this, "Notifications activées", Toast.LENGTH_SHORT).show();
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("FCM", "Demande permission notifications...");
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void startLocationService() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("Donneur", "Demande de permission de localisation");
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        try {
            Intent serviceIntent = new Intent(this, LocationService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }

            Log.d("Donneur", "✅ Service de localisation démarré");
            Toast.makeText(this, "Localisation activée", Toast.LENGTH_SHORT).show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    new android.os.Handler().postDelayed(() -> {
                        ActivityCompat.requestPermissions(this,
                                new String[]{android.Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
                    }, 2000);
                }
            }
        } catch (Exception e) {
            Log.e("Donneur", "❌ Erreur démarrage service : " + e.getMessage(), e);
            Toast.makeText(this, "Erreur activation localisation", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ CONNEXION AU SERVEUR SOCKET.IO
    private void connectToServer() {
        SocketManager socketManager = SocketManager.getInstance();

        Log.d("Socket", "Connexion au serveur Socket.IO...");
        socketManager.connect();

        // ✅ Écouter la confirmation d'enregistrement
        socketManager.onRegistered(args -> {
            runOnUiThread(() -> {
                isRegistered = true;
                Log.d("Socket", "✅ Enregistré sur le serveur avec succès");
                Toast.makeText(this, "✅ Connecté - Vous recevrez les alertes",
                        Toast.LENGTH_LONG).show();
            });
        });

        // ✅ Écouter les nouvelles alertes
        socketManager.onNewAlert(args -> {
            try {
                JSONObject alerte = (JSONObject) args[0];

                String alerteId = alerte.getString("alerteId");
                int medecinId = alerte.getInt("medecinId");
                String zone = alerte.getString("zone");
                String groupe = alerte.getString("groupe");
                String distance = alerte.getString("distance");

                runOnUiThread(() -> {
                    Log.d("Socket", "🚨 ALERTE REÇUE : " + zone + " - " + groupe + " - " + distance + "km");

                    // Afficher l'alerte
                    afficherAlerte(alerteId, medecinId, zone, groupe, distance);

                    // Notification visuelle
                    Toast.makeText(this,
                            "🚨 DON DE SANG URGENT\n" + zone + " - " + groupe,
                            Toast.LENGTH_LONG).show();
                });

            } catch (JSONException e) {
                Log.e("Socket", "❌ Erreur parsing alerte : " + e.getMessage(), e);
            }
        });

        Log.d("Socket", "Listeners Socket.IO configurés");
    }

    // ✅ ENREGISTREMENT SUR LE SERVEUR (appelé APRÈS chargement du client)
    private void registerOnServer() {
        if (currentClient == null) {
            Log.e("Socket", "❌ Impossible de s'enregistrer : currentClient est NULL");

            // ✅ Réessayer après 2 secondes
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.d("Socket", "Nouvelle tentative d'enregistrement...");
                registerOnServer();
            }, 2000);
            return;
        }

        if (isRegistered) {
            Log.d("Socket", "Déjà enregistré, skip");
            return;
        }

        SocketManager socketManager = SocketManager.getInstance();

        // ✅ Vérifier que le socket est connecté
        if (!socketManager.isConnected()) {
            Log.w("Socket", "Socket non connecté, reconnexion...");
            socketManager.connect();

            // Réessayer après 1 seconde
            new Handler(Looper.getMainLooper()).postDelayed(this::registerOnServer, 1000);
            return;
        }

        Log.d("Socket", "Enregistrement sur le serveur...");
        Log.d("Socket", "  - ID : " + idDonneur);
        Log.d("Socket", "  - Groupe : " + currentClient.getGroupe());
        Log.d("Socket", "  - Lat : " + currentClient.getLatitude());
        Log.d("Socket", "  - Lon : " + currentClient.getLongitude());

        socketManager.register(
                idDonneur,
                "donneur",
                currentClient.getGroupe(),
                currentClient.getLatitude(),
                currentClient.getLongitude()
        );

        Log.d("Socket", "✅ Commande d'enregistrement envoyée");
    }

    private void afficherAlerte(String alerteId, int medecinId,
                                String zone, String groupe, String distance) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View alerteView = inflater.inflate(R.layout.template3, containerAlertes, false);

        TextView tvHopital = alerteView.findViewById(R.id.tvHopital);
        TextView tvMedecin = alerteView.findViewById(R.id.tvMedecin);
        TextView tvGroupe = alerteView.findViewById(R.id.tvGroupe);
        TextView tvDistance = alerteView.findViewById(R.id.tvDistance);
        TextView tvAccept = alerteView.findViewById(R.id.tvAccept);
        TextView tvRefuse = alerteView.findViewById(R.id.tvRefuse);

        tvHopital.setText(zone);
        tvGroupe.setText(groupe);
        tvDistance.setText("À " + distance + " km");

        // ✅ Charger le nom du médecin
        dbHelper.getClient(medecinId, new ClientCallback() {
            @Override
            public void onClientLoaded(Client medecin) {
                if (medecin != null) {
                    tvMedecin.setText("Dr " + medecin.getName());
                } else {
                    tvMedecin.setText("Dr " + medecinId);
                }
            }

            @Override
            public void onError(Exception e) {
                tvMedecin.setText("Médecin #" + medecinId);
            }
        });

        tvAccept.setOnClickListener(v -> {
            Log.d("Socket", "✅ Alerte acceptée : " + alerteId);
            SocketManager.getInstance().respondToAlert(alerteId, idDonneur, medecinId, true);
            Toast.makeText(this, "✅ Alerte acceptée - Le médecin sera notifié",
                    Toast.LENGTH_LONG).show();
            containerAlertes.removeView(alerteView);
        });

        tvRefuse.setOnClickListener(v -> {
            Log.d("Socket", "❌ Alerte refusée : " + alerteId);
            SocketManager.getInstance().respondToAlert(alerteId, idDonneur, medecinId, false);
            Toast.makeText(this, "Alerte refusée", Toast.LENGTH_SHORT).show();
            containerAlertes.removeView(alerteView);
        });

        containerAlertes.addView(alerteView, 0); // ✅ Ajouter en PREMIER

        Log.d("Alertes", "✅ Alerte ajoutée à l'interface");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ✅ Reconnecter si déconnecté
        SocketManager socketManager = SocketManager.getInstance();
        if (!socketManager.isConnected()) {
            Log.d("Socket", "Reconnexion au serveur...");
            socketManager.connect();

            // Réenregistrer après reconnexion
            if (currentClient != null) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    isRegistered = false;
                    registerOnServer();
                }, 1000);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("Donneur", "Destruction de l'activité");

        // ⚠️ NE PAS se déconnecter du socket ici car le service continue
        // SocketManager.getInstance().disconnect();

        // Le service de localisation continue en arrière-plan
    }
}