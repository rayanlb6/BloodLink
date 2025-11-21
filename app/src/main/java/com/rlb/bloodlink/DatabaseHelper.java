package com.rlb.bloodlink;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ClientDB";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_CLIENT = "client";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_ROLE = "role";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_TELEPHONE = "telephone";
    private static final String COLUMN_ADRESSE = "adresse";
    private static final String COLUMN_GROUPE = "groupe";
    private static final String COLUMN_SEXE = "sexe";

    private static final String TABLE_ALERTES = "alertes";
    private static final String COL_ID = "id";
    private static final String COL_ID_DONNEUR = "id_donneur";
    private static final String COL_ID_MEDECIN = "id_medecin";
    private static final String COL_ZONE = "zone";
    private static final String COL_GROUPE = "groupe";
    private static final String COL_DATE = "date";
    private static final String COL_STATUT = "statut";

    private static final String CREATE_TABLE_ALERTES = "CREATE TABLE " + TABLE_ALERTES + " ("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_ID_DONNEUR + " INTEGER, "
            + COL_ID_MEDECIN + " INTEGER, "
            + COL_ZONE + " TEXT, "
            + COL_GROUPE + " TEXT, "
            + COL_DATE + " TEXT, "
            + COL_STATUT + " TEXT"
            + ")";

    private static final String CREATE_CLIENT_TABLE = "CREATE TABLE " + TABLE_CLIENT + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_EMAIL + " TEXT, "
            + COLUMN_NAME + " TEXT, "
            + COLUMN_SEXE + " TEXT, "
            + COLUMN_TELEPHONE + " TEXT, "
            + COLUMN_ADRESSE + " TEXT, "
            + COLUMN_ROLE + " TEXT, "
            + COLUMN_GROUPE + " TEXT) ";

    private final DatabaseReference firebaseRef;
    private final DatabaseReference firebaseAlertesRef;
    private final DatabaseReference firebaseAlertesHistoryRef;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    private ValueEventListener alertesListener;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        firebaseRef = FirebaseDatabase.getInstance().getReference("clients");
        firebaseAlertesRef = FirebaseDatabase.getInstance().getReference("alertes");
        firebaseAlertesHistoryRef = FirebaseDatabase.getInstance().getReference("alertes_history");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CLIENT_TABLE);
        db.execSQL(CREATE_TABLE_ALERTES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALERTES);
        onCreate(db);
    }

    public long insertClientRole(String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROLE, role);
        long id = db.insert(TABLE_CLIENT, null, values);
        db.close();

        Client client = new Client(id, null, null, null, null, null, role, null, 0, 0, true);
        firebaseRef.child(String.valueOf(id)).setValue(client);

        return id;
    }

    public int updateNom(long id, String nom) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, nom);
        int rows = db.update(TABLE_CLIENT, values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        firebaseRef.child(String.valueOf(id)).child("name").setValue(nom);
        return rows;
    }

    public int updateEmail(long id, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        int rows = db.update(TABLE_CLIENT, values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        firebaseRef.child(String.valueOf(id)).child("email").setValue(email);
        return rows;
    }

    public int updateTelephone(long id, String telephone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TELEPHONE, telephone);
        int rows = db.update(TABLE_CLIENT, values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        firebaseRef.child(String.valueOf(id)).child("telephone").setValue(telephone);
        return rows;
    }

    public int updateSexe(long id, String sexe) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SEXE, sexe);
        int rows = db.update(TABLE_CLIENT, values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        firebaseRef.child(String.valueOf(id)).child("sexe").setValue(sexe);
        return rows;
    }

    public int updateAdresse(long id, String adresse) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ADRESSE, adresse);
        int rows = db.update(TABLE_CLIENT, values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        firebaseRef.child(String.valueOf(id)).child("adresse").setValue(adresse);
        return rows;
    }

    public int updateGroupeRhesus(long id, String groupe) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUPE, groupe);
        int rows = db.update(TABLE_CLIENT, values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        firebaseRef.child(String.valueOf(id)).child("groupe").setValue(groupe);
        return rows;
    }

    public Cursor getLastIdCursor() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_CLIENT + " ORDER BY " + COLUMN_ID + " DESC LIMIT 1";
        return db.rawQuery(query, null);
    }

    public void getAllAlerteActif(AlertesCallback callback) {
        Query query = firebaseAlertesRef.orderByChild("statut").equalTo("actif");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Alerte> aList = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Alerte alerte = userSnapshot.getValue(Alerte.class);
                    if (alerte != null) {
                        alerte.setFirebaseId(userSnapshot.getKey());
                        aList.add(alerte);
                    }
                }
                callback.onAlertesLoaded(aList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error);
            }
        });
    }

    public void listenToActiveAlertes(AlertesCallback callback) {
        alertesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Alerte> aList = new ArrayList<>();
                Log.d("Firebase", "Alertes reçues : " + snapshot.getChildrenCount());

                for (DataSnapshot alertSnapshot : snapshot.getChildren()) {
                    Alerte alerte = alertSnapshot.getValue(Alerte.class);
                    if (alerte != null) {
                        alerte.setFirebaseId(alertSnapshot.getKey());
                        aList.add(alerte);
                        Log.d("Firebase", "Alerte chargée : " + alertSnapshot.getKey() +
                                " - Zone: " + alerte.getZone() + " - Groupe: " + alerte.getGroupe());
                    }
                }
                callback.onAlertesLoaded(aList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Erreur écoute alertes : " + error.getMessage());
                callback.onError(error);
            }
        };

        firebaseAlertesRef.orderByChild("statut").equalTo("actif")
                .addValueEventListener(alertesListener);

        Log.d("Firebase", "Écoute des alertes actives démarrée");
    }

    public void stopListeningToAlertes() {
        if (alertesListener != null) {
            firebaseAlertesRef.removeEventListener(alertesListener);
            Log.d("Firebase", "Écoute des alertes arrêtée");
        }
    }

    // ✅ NOUVELLE VERSION : Utilise Cloud Functions via Realtime Database Trigger
    public void insertAlertFirebase(int idMedecin, String zone, double distance,
                                    double longitude, double latitude, String statut,
                                    String date, String groupe) {
        Alerte alerte = new Alerte(idMedecin, zone, groupe, distance, date,
                statut.toLowerCase(), longitude, latitude);

        String id = firebaseAlertesRef.push().getKey();

        if (id != null) {
            // ✅ AJOUT : Ajouter les métadonnées pour déclencher les notifications
            Map<String, Object> alerteData = new HashMap<>();
            alerteData.put("id_medecin", alerte.getId_medecin());
            alerteData.put("zone", alerte.getZone());
            alerteData.put("groupe", alerte.getGroupe());
            alerteData.put("distance", alerte.getDistance());
            alerteData.put("date", alerte.getDate());
            alerteData.put("statut", alerte.getStatut());
            alerteData.put("longitude", alerte.getLongitude());
            alerteData.put("latitude", alerte.getLatitude());
            alerteData.put("shouldNotify", true); // ✅ FLAG pour Cloud Function
            alerteData.put("timestamp", System.currentTimeMillis());

            firebaseAlertesRef.child(id).setValue(alerteData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firebase", "✅ Alerte enregistrée - ID: " + id +
                                " | Zone: " + zone + " | Groupe: " + groupe +
                                " | Rayon: " + distance + " km");

                        // ✅ Déclencher l'envoi manuel des notifications
                        sendNotificationsToDonneurs(zone, groupe, distance, latitude, longitude);
                    })
                    .addOnFailureListener(e ->
                            Log.e("Firebase", "❌ Erreur envoi alerte : " + e.getMessage()));
        } else {
            Log.e("Firebase", "❌ Impossible de générer un ID pour l'alerte");
        }
    }

    // ✅ VERSION AMÉLIORÉE : Filtrer par groupe ET distance
    private void sendNotificationsToDonneurs(String zone, String groupe, double rayon,
                                             double alertLat, double alertLon) {
        Log.d("Firebase", "🔍 Recherche de donneurs pour groupe " + groupe + " dans un rayon de " + rayon + " km");

        firebaseRef.orderByChild("role").equalTo("donneur")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int count = 0;
                        int eligible = 0;

                        for (DataSnapshot donneurSnapshot : snapshot.getChildren()) {
                            try {
                                Client donneur = donneurSnapshot.getValue(Client.class);

                                if (donneur == null) continue;

                                // ✅ Vérifications détaillées
                                boolean hasGroupe = donneur.getGroupe() != null && donneur.getGroupe().equals(groupe);
                                boolean isConnecte = donneur.isConnecte();
                                boolean hasLocation = donneur.getLatitude() != 0 && donneur.getLongitude() != 0;

                                Log.d("Firebase", "Donneur " + donneur.getId() +
                                        " - Groupe: " + (hasGroupe ? "✓" : "✗") +
                                        " - Connecté: " + (isConnecte ? "✓" : "✗") +
                                        " - Position: " + (hasLocation ? "✓" : "✗"));

                                if (!hasGroupe || !isConnecte || !hasLocation) {
                                    continue;
                                }

                                // ✅ Calculer la distance
                                double distance = haversine(
                                        alertLat, alertLon,
                                        donneur.getLatitude(), donneur.getLongitude()
                                );

                                Log.d("Firebase", "Distance calculée : " + distance + " km (max: " + rayon + " km)");

                                if (distance <= rayon) {
                                    eligible++;
                                    String fcmToken = donneurSnapshot.child("fcmToken").getValue(String.class);

                                    if (fcmToken != null && !fcmToken.isEmpty()) {
                                        sendPushNotification(
                                                fcmToken,
                                                "🚨 Alerte BloodLink - Don de sang urgent",
                                                "Un don de sang " + groupe + " est nécessaire à " + zone +
                                                        " (à " + String.format("%.1f", distance) + " km de vous)",
                                                zone,
                                                groupe
                                        );
                                        count++;
                                        Log.d("Firebase", "✅ Notification envoyée à donneur " + donneur.getId());
                                    } else {
                                        Log.w("Firebase", "⚠️ Pas de token FCM pour donneur " + donneur.getId());
                                    }
                                }

                            } catch (Exception e) {
                                Log.e("Firebase", "Erreur traitement donneur : " + e.getMessage());
                            }
                        }

                        Log.d("Firebase", "📊 Résultats : " + eligible + " donneurs éligibles, " +
                                count + " notifications envoyées sur " + snapshot.getChildrenCount() + " donneurs totaux");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Erreur récupération donneurs : " + error.getMessage());
                    }
                });
    }

    // ✅ Fonction haversine pour calculer la distance
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Rayon de la Terre en km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }

    // ✅ VERSION SIMPLIFIÉE : Utilise HTTPURLConnection (inclus dans Android)
    private void sendPushNotification(String token, String title, String body, String zone, String groupe) {
        new Thread(() -> {
            try {
                // ✅ Utiliser l'API FCM v1 (moderne)
                String projectId = "votre-projet-id"; // ⚠️ REMPLACER
                String url = "https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send";

                // ⚠️ TEMPORAIRE : Pour tester, utilisons l'ancienne API (à remplacer)
                String legacyUrl = "https://fcm.googleapis.com/fcm/send";
                String serverKey = "VOTRE_SERVER_KEY"; // ⚠️ À obtenir depuis Firebase Console

                org.json.JSONObject json = new org.json.JSONObject();
                org.json.JSONObject notification = new org.json.JSONObject();
                org.json.JSONObject data = new org.json.JSONObject();

                notification.put("title", title);
                notification.put("body", body);
                notification.put("sound", "default");
                notification.put("priority", "high");

                data.put("zone", zone);
                data.put("groupe", groupe);
                data.put("click_action", "FLUTTER_NOTIFICATION_CLICK");

                json.put("to", token);
                json.put("priority", "high");
                json.put("notification", notification);
                json.put("data", data);

                java.net.URL urlObj = new java.net.URL(legacyUrl);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "key=" + serverKey);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                java.io.OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("FCM", "Code réponse : " + responseCode);

                if (responseCode == 200) {
                    java.io.BufferedReader br = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();
                    Log.d("FCM", "✅ Notification envoyée : " + response.toString());
                } else {
                    java.io.BufferedReader br = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getErrorStream()));
                    StringBuilder error = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        error.append(line);
                    }
                    br.close();
                    Log.e("FCM", "❌ Erreur " + responseCode + " : " + error.toString());
                }

                conn.disconnect();

            } catch (Exception e) {
                Log.e("FCM", "❌ Exception : " + e.getMessage(), e);
            }
        }).start();
    }

    public void getClient(int id, ClientCallback callback) {
        firebaseRef.child(String.valueOf(id)).get()
                .addOnSuccessListener(snapshot -> {
                    Client client = snapshot.getValue(Client.class);
                    if (client != null) {
                        Log.d("Firebase", "Client chargé : " + client.getName() +
                                " - Groupe: " + client.getGroupe() +
                                " - Lat: " + client.getLatitude() +
                                " - Lon: " + client.getLongitude());
                    }
                    callback.onClientLoaded(client);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Erreur chargement client : " + e.getMessage());
                    callback.onError(e);
                });
    }

    public long saveAlertHistory(int idDonneur, int idMedecin, String zone,
                                 String groupe, String date, String statut) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ID_MEDECIN, idMedecin);
        values.put(COL_ID_DONNEUR, idDonneur);
        values.put(COL_ZONE, zone);
        values.put(COL_GROUPE, groupe);
        values.put(COL_DATE, date);
        values.put(COL_STATUT, statut);
        long id = db.insert(TABLE_ALERTES, null, values);
        db.close();
        return id;
    }

    public Cursor getAlertHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ALERTES, null);
    }

    public void updateLocation(int idDonneur, double latitude, double longitude) {
        firebaseRef.child(String.valueOf(idDonneur)).child("longitude").setValue(longitude);
        firebaseRef.child(String.valueOf(idDonneur)).child("latitude").setValue(latitude);
        Log.d("Firebase", "📍 Localisation mise à jour pour donneur " + idDonneur +
                " : Lat=" + latitude + ", Lon=" + longitude);
    }

    public void updateConnecte(int idDonneur, boolean connecte) {
        firebaseRef.child(String.valueOf(idDonneur)).child("connecte").setValue(connecte);
        Log.d("Firebase", "Donneur " + idDonneur + " - Connecté : " + connecte);
    }

    public void updateFcmToken(int userId, String token) {
        firebaseRef.child(String.valueOf(userId)).child("fcmToken").setValue(token)
                .addOnSuccessListener(aVoid ->
                        Log.d("Firebase", "✅ Token FCM mis à jour pour user " + userId + " : " + token.substring(0, 20) + "..."))
                .addOnFailureListener(e ->
                        Log.e("Firebase", "❌ Erreur MAJ token : " + e.getMessage()));
    }

    public void acceptOrRejectAlert(String idAlert, int idDonneur, int idMedecin, boolean accept) {
        String id = firebaseAlertesHistoryRef.push().getKey();
        AlerteData ad = new AlerteData(idAlert, idDonneur, idMedecin, accept);

        if (id != null) {
            firebaseAlertesHistoryRef.child(id).setValue(ad)
                    .addOnSuccessListener(aVoid ->
                            Log.d("Firebase", "Réponse alerte enregistrée : " +
                                    (accept ? "ACCEPTÉE" : "REFUSÉE")))
                    .addOnFailureListener(e ->
                            Log.e("Firebase", "Erreur enregistrement réponse : " + e.getMessage()));
        }
    }

    public List<AlerteData> getAlertDecision(String idAlerte) {
        List<AlerteData> aList = new ArrayList<>();
        Query query = firebaseAlertesHistoryRef.orderByChild("alerte").equalTo(idAlerte);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    AlerteData alerte = userSnapshot.getValue(AlerteData.class);
                    aList.add(alerte);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE", error.getMessage());
            }
        });
        return aList;
    }

    public void uploadProfileImage(Uri imageUri, int idDonneur) {
        if (imageUri != null) {
            StorageReference photoRef = FirebaseStorage.getInstance()
                    .getReference()
                    .child("profile_images/" + idDonneur + ".jpg");

            photoRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            Log.d("Firebase", "✅ Image uploadée : " + imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "❌ Échec de l'upload : " + e.getMessage());
                    });
        }
    }
}