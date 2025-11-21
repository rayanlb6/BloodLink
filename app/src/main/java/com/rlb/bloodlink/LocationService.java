package com.rlb.bloodlink;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private static final String CHANNEL_ID = "bloodlink_location";
    private static final int NOTIFICATION_ID = 1001;
    private static final long UPDATE_INTERVAL = 5 * 60 * 1000; // 5 minutes
    private static final float MIN_DISTANCE = 100; // 100 mètres

    private LocationManager locationManager;
    private LocationListener locationListener;
    private int idDonneur;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "📍 Service de localisation en cours de création...");

        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            // Récupérer l'ID du donneur
            SharedPreferences prefs = getSharedPreferences("BloodLinkPrefs", MODE_PRIVATE);
            idDonneur = (int) prefs.getLong("userId", 0);

            Log.d(TAG, "ID Donneur : " + idDonneur);

            if (idDonneur == 0) {
                Log.e(TAG, "❌ ID donneur invalide, arrêt du service");
                stopSelf();
                return;
            }

            // Créer le notification channel
            createNotificationChannel();

            // Démarrer en foreground
            startForeground(NOTIFICATION_ID, createNotification());
            Log.d(TAG, "✅ Service démarré en foreground");

            // Démarrer le suivi de localisation
            startLocationUpdates();

        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur dans onCreate : " + e.getMessage(), e);
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service démarré");
        return START_STICKY; // Redémarrer si tué par le système
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Suivi de localisation BloodLink",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Permet de vous localiser pour les alertes de don de sang");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, BloodLinkDonneurActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("BloodLink actif")
                .setContentText("Votre position est partagée pour recevoir les alertes")
                .setSmallIcon(R.drawable.localisateur)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void startLocationUpdates() {
        Log.d(TAG, "🔍 Démarrage des mises à jour de localisation...");

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Mettre à jour via Socket.IO
                SocketManager.getInstance().updateLocation(idDonneur, latitude, longitude);

                Log.d(TAG, "📍 Position mise à jour : " + latitude + ", " + longitude);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "Status changé : " + provider + " - " + status);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                Log.d(TAG, "✅ Provider activé : " + provider);
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Log.w(TAG, "⚠️ Provider désactivé : " + provider);
            }
        };

        // Vérifier les permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "❌ Permission de localisation non accordée - Arrêt du service");
            stopSelf();
            return;
        }

        if (locationManager == null) {
            Log.e(TAG, "❌ LocationManager est null - Arrêt du service");
            stopSelf();
            return;
        }

        try {
            boolean hasProvider = false;

            // GPS (plus précis)
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        UPDATE_INTERVAL,
                        MIN_DISTANCE,
                        locationListener
                );
                Log.d(TAG, "✅ GPS activé - Mises à jour demandées");
                hasProvider = true;
            } else {
                Log.w(TAG, "⚠️ GPS désactivé");
            }

            // Network (moins précis mais fonctionne en intérieur)
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        UPDATE_INTERVAL,
                        MIN_DISTANCE,
                        locationListener
                );
                Log.d(TAG, "✅ Network activé - Mises à jour demandées");
                hasProvider = true;
            } else {
                Log.w(TAG, "⚠️ Network désactivé");
            }

            if (!hasProvider) {
                Log.e(TAG, "❌ Aucun provider de localisation disponible");
            }

            // Récupérer la dernière position connue immédiatement
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation == null) {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (lastLocation != null) {
                Log.d(TAG, "📍 Position initiale récupérée");
                locationListener.onLocationChanged(lastLocation);
            } else {
                Log.w(TAG, "⚠️ Aucune position connue disponible");
            }

        } catch (SecurityException e) {
            Log.e(TAG, "❌ Erreur permissions : " + e.getMessage());
            stopSelf();
        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur inattendue : " + e.getMessage(), e);
            stopSelf();
        }
    }

    private void updateNotificationWithLocation(double lat, double lon) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("BloodLink actif")
                    .setContentText(String.format("Position: %.4f, %.4f", lat, lon))
                    .setSmallIcon(R.drawable.localisateur)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();

            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service arrêté");

        // Arrêter les mises à jour de localisation
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }

        // Marquer le donneur comme déconnecté
        if (idDonneur != 0) {
            FirebaseDatabase.getInstance()
                    .getReference("clients")
                    .child(String.valueOf(idDonneur))
                    .child("connecte")
                    .setValue(false);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}