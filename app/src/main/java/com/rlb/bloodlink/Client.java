package com.rlb.bloodlink;

public class Client {
 private long id;
 private String email, name, sexe, telephone,adresse, role, groupe;
 // ⚠️ OPTIONNEL : Utile si vous voulez distinguer l'ID SQLite de l'ID Firebase
 // Dans votre cas actuel, ce n'est PAS nécessaire car id = firebaseKey
 private String firebaseKey; // Non utilisé actuellement
 private double latitude, longitude;
 private boolean connecte;
 private String fcmToken; // ⚠️ NOUVEAU : Token pour les notifications


 public Client() {} // Obligatoire pour Firebase

 public Client(long id, String email, String name, String sexe, String telephone,String adresse,
               String role, String groupe,
               double latitude, double longitude,boolean connecte) {
  this.id = id;
  this.email = email;
  this.name = name;
  this.sexe = sexe;
  this.telephone = telephone;
  this.adresse=adresse;
  this.role = role;
  this.groupe = groupe;
  this.latitude = latitude;
  this.longitude = longitude;
  this.connecte=connecte;
  this.firebaseKey = String.valueOf(id); // Même valeur que id
  this.fcmToken = ""; // Initialisé vide
 }

 public long getId() {
  return id;
 }

 public void setId(long id) {
  this.id = id;
 }

 public String getEmail() {
  return email;
 }

 public void setEmail(String email) {
  this.email = email;
 }

 public String getName() {
  return name;
 }

 public void setName(String name) {
  this.name = name;
 }

 public String getSexe() {
  return sexe;
 }

 public void setSexe(String sexe) {
  this.sexe = sexe;
 }

 public String getTelephone() {
  return telephone;
 }

 public void setTelephone(String telephone) {
  this.telephone = telephone;
 }

 public String getAdresse() {
  return adresse;
 }

 public void setAdresse(String adresse) {
  this.adresse = adresse;
 }

 public String getRole() {
  return role;
 }

 public void setRole(String role) {
  this.role = role;
 }

 public String getGroupe() {
  return groupe;
 }

 public void setGroupe(String groupe) {
  this.groupe = groupe;
 }

 public double getLatitude() {
  return latitude;
 }

 public void setLatitude(double latitude) {
  this.latitude = latitude;
 }

 public double getLongitude() {
  return longitude;
 }

 public void setLongitude(double longitude) {
  this.longitude = longitude;
 }

 public boolean isConnecte() {
  return connecte;
 }

 public void setConnecte(boolean connecte) {
  this.connecte = connecte;
 }

 // ⚠️ OPTIONNEL : Getter/Setter pour firebaseKey
 public String getFirebaseKey() { return firebaseKey; }
 public void setFirebaseKey(String firebaseKey) { this.firebaseKey = firebaseKey; }
 // ⚠️ NOUVEAU : Getter/Setter pour le token FCM
 public String getFcmToken() { return fcmToken; }
 public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
}
