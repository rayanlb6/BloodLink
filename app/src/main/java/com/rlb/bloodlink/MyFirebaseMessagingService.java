package com.rlb.bloodlink;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM";
    private static final String CHANNEL_ID = "bloodlink_alerts";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Message reçu de : " + remoteMessage.getFrom());

        // Vérifier si le message contient des données
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Données du message : " + remoteMessage.getData());

            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String zone = remoteMessage.getData().get("zone");
            String groupe = remoteMessage.getData().get("groupe");

            sendNotification(title, body, zone, groupe);
        }

        // Vérifier si le message contient une notification
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Notification : " + title + " - " + body);
            sendNotification(title, body, null, null);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Nouveau token FCM : " + token);

        // Sauvegarder le token localement
        saveTokenToPreferences(token);

        // Envoyer le token à Firebase Database
        updateTokenInFirebase(token);
    }

    private void saveTokenToPreferences(String token) {
        SharedPreferences prefs = getSharedPreferences("BloodLinkPrefs", MODE_PRIVATE);
        prefs.edit().putString("fcmToken", token).apply();
        Log.d(TAG, "Token sauvegardé localement");
    }

    private void updateTokenInFirebase(String token) {
        SharedPreferences prefs = getSharedPreferences("BloodLinkPrefs", MODE_PRIVATE);
        int userId = (int) prefs.getLong("userId", 0);

        if (userId != 0) {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            dbHelper.updateFcmToken(userId, token);
            Log.d(TAG, "Token mis à jour dans Firebase pour user " + userId);
        }
    }

    private void sendNotification(String title, String messageBody, String zone, String groupe) {
        // Intent pour ouvrir l'application
        Intent intent = new Intent(this, BloodLinkDonneurActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Son par défaut
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Créer la notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.localisateur) // ⚠️ Créer cette icône
                        .setContentTitle(title != null ? title : "🩸 BloodLink")
                        .setContentText(messageBody)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        // Ajouter des informations supplémentaires si disponibles
        if (zone != null && groupe != null) {
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(messageBody + "\n📍 " + zone + "\n🩸 Groupe : " + groupe));
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Créer le canal de notification (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alertes BloodLink",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications pour les demandes de don de sang urgentes");
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
        Log.d(TAG, "Notification affichée");
    }
}