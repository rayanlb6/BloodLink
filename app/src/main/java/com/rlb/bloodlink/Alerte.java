package com.rlb.bloodlink;

public class Alerte {
    private String firebaseId;
    private int id_medecin;
    private String zone;
    private String groupe;
    private double distance;
    private String date;
    private String statut;
    private double longitude,latitude;


    public Alerte() {} // Obligatoire pour Firebase

    public Alerte( int id_medecin,String zone, String groupe, double distance,String date, String statut,double longitude,double latitude) {
        this.id_medecin = id_medecin;
        this.zone = zone;
        this.groupe = groupe;
        this.distance = distance;
        this.date = date;
        this.statut = statut;
        this.latitude=latitude;
        this.longitude=longitude;
    }
    // ⚠️ AJOUTER ces getters/setters
    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }
    public int getId_medecin() {
        return id_medecin;
    }

    public void setId_medecin(int id_medecin) {
        this.id_medecin = id_medecin;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getGroupe() {
        return groupe;
    }

    public void setGroupe(String groupe) {
        this.groupe = groupe;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


}
