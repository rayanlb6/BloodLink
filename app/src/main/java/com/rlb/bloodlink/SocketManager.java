package com.rlb.bloodlink;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import java.net.URISyntaxException;

public class SocketManager {

    private static final String TAG = "SocketManager";
    private static SocketManager instance;
    private Socket socket;
    private static final String SERVER_URL = "https://bloodlink-server.onrender.com";

    private SocketManager() {
        try {
            socket = IO.socket(SERVER_URL);
        } catch (URISyntaxException e) {
            Log.e(TAG, "Erreur création socket : " + e.getMessage());
        }
    }

    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public void connect() {
        if (socket != null && !socket.connected()) {
            socket.connect();
            Log.d(TAG, "✅ Connexion au serveur...");
        }
    }

    public void disconnect() {
        if (socket != null && socket.connected()) {
            socket.disconnect();
            Log.d(TAG, "❌ Déconnexion du serveur");
        }
    }

    public boolean isConnected() {
        return socket != null && socket.connected();
    }

    // Enregistrer un utilisateur
    public void register(int userId, String role, String groupe,
                         double latitude, double longitude) {
        try {
            JSONObject data = new JSONObject();
            data.put("userId", userId);
            data.put("role", role);
            data.put("groupe", groupe);
            data.put("latitude", latitude);
            data.put("longitude", longitude);

            socket.emit("register", data);
            Log.d(TAG, "Enregistrement envoyé pour user " + userId);
        } catch (JSONException e) {
            Log.e(TAG, "Erreur register : " + e.getMessage());
        }
    }

    // Mettre à jour la position
    public void updateLocation(int userId, double latitude, double longitude) {
        try {
            JSONObject data = new JSONObject();
            data.put("userId", userId);
            data.put("latitude", latitude);
            data.put("longitude", longitude);

            socket.emit("updateLocation", data);
        } catch (JSONException e) {
            Log.e(TAG, "Erreur updateLocation : " + e.getMessage());
        }
    }

    // Envoyer une alerte
    public void sendAlert(int medecinId, String zone, String groupe,
                          double rayon, double latitude, double longitude) {
        try {
            JSONObject data = new JSONObject();
            data.put("medecinId", medecinId);
            data.put("zone", zone);
            data.put("groupe", groupe);
            data.put("rayon", rayon);
            data.put("latitude", latitude);
            data.put("longitude", longitude);

            socket.emit("sendAlert", data);
            Log.d(TAG, "🚨 Alerte envoyée");
        } catch (JSONException e) {
            Log.e(TAG, "Erreur sendAlert : " + e.getMessage());
        }
    }

    // Répondre à une alerte
    public void respondToAlert(String alerteId, int donneurId,
                               int medecinId, boolean accepted) {
        try {
            JSONObject data = new JSONObject();
            data.put("alerteId", alerteId);
            data.put("donneurId", donneurId);
            data.put("medecinId", medecinId);
            data.put("accepted", accepted);

            socket.emit("respondToAlert", data);
            Log.d(TAG, (accepted ? "✅ Accepté" : "❌ Refusé"));
        } catch (JSONException e) {
            Log.e(TAG, "Erreur respondToAlert : " + e.getMessage());
        }
    }

    // Écouter les événements
    public void onRegistered(Emitter.Listener listener) {
        socket.on("registered", listener);
    }

    public void onNewAlert(Emitter.Listener listener) {
        socket.on("newAlert", listener);
    }

    public void onAlertSent(Emitter.Listener listener) {
        socket.on("alertSent", listener);
    }

    public void onAlertResponse(Emitter.Listener listener) {
        socket.on("alertResponse", listener);
    }

    public void removeAllListeners() {
        if (socket != null) {
            socket.off();
        }
    }
}