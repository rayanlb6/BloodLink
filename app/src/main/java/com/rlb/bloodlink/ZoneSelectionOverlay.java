package com.rlb.bloodlink;

import android.graphics.Color;
import android.view.MotionEvent;
import android.widget.Toast;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.Distance; // Importez cette classe pour calculer le rayon

import java.util.ArrayList;

public class ZoneSelectionOverlay extends Overlay {

    private MapView mapView;
    private GeoPoint startPoint; // Utilisez GeoPoint pour stocker le point de départ
    private Polygon cercle;
    private int idMedecin;
    private String groupe;
    private boolean isDrawing = false; // Pour savoir si on est en train de dessiner

    public ZoneSelectionOverlay(MapView mapView, int idMedecin, String groupe) {
        super();
        this.mapView = mapView;

        this.groupe = groupe;

        // Initialisation du polygone (cercle) une seule fois
        this.cercle = new Polygon();
        cercle.setStrokeColor(Color.RED);
        cercle.setFillColor(0x30FF0000);
        cercle.setStrokeWidth(4f);
        // On l'ajoute à la carte dès la création
        mapView.getOverlayManager().add(cercle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
    /*
        // Conversion des pixels de l'écran en coordonnées géographiques
        IGeoPoint point = mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
        GeoPoint currentPoint = new GeoPoint(point.getLatitude(), point.getLongitude());

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                startPoint = currentPoint;
                isDrawing = true;
                // Cache le cercle initialement
                cercle.setPoints(new ArrayList<>());
                mapView.invalidate();
                return true; // Nous gérons cet événement

            case MotionEvent.ACTION_MOVE:
                if (isDrawing && startPoint != null) {
                    // Calcul du rayon en mètres
                    double distanceEnMetres = startPoint.distanceToAsDouble(currentPoint);

                    // Génération des points du cercle dynamique
                    cercle.setPoints(Polygon.pointsAsCircle(startPoint, distanceEnMetres));

                    // Important: demander à la MapView de se rafraîchir
                    mapView.invalidate();
                    return true; // Nous gérons cet événement
                }
                return false;

            case MotionEvent.ACTION_UP:
                if (isDrawing && startPoint != null) {
                    isDrawing = false;
                    double distanceEnMetres = startPoint.distanceToAsDouble(currentPoint);
                    double rayonKm = distanceEnMetres / 1000.0; // Conversion en km

                    // Appel de la méthode dans l'activité parente
                    if (mapView.getContext() instanceof BloodLinkMedecinActivity) {
                        ((BloodLinkMedecinActivity) mapView.getContext())
                                .envoyerAlerteZone(idMedecin, groupe, startPoint.getLatitude(), startPoint.getLongitude(), rayonKm);

                        Toast.makeText(mapView.getContext(), "Alerte envoyée pour un rayon de " + String.format("%.2f", rayonKm) + " km", Toast.LENGTH_LONG).show();
                    }

                    // Optionnel: Garder le cercle affiché après le relâchement, ou le cacher
                    // Si vous voulez le cacher, ajoutez :
                    // mapView.getOverlayManager().remove(cercle);

                    mapView.invalidate();
                    return true; // Nous gérons cet événement
                }
                return false;
        }*/

        return false; // Laissez les autres événements continuer (zoom, pan standard)
    }
}
