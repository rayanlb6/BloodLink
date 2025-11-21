package com.rlb.bloodlink;

public class AlerteData {

    public String idAlert;
    public int idMedecin,idDonneur;
    public boolean accept;
    public AlerteData(){}
    public AlerteData(String idAlert,int idDonneur,int idMedecin,boolean accept ){
        this.idAlert=idAlert;
        this.idDonneur=idDonneur;
        this.idMedecin=idMedecin;
        this.accept=accept;
    }

    public String getIdAlert() {
        return idAlert;
    }

    public void setIdAlert(String idAlert) {
        this.idAlert = idAlert;
    }

    public int getIdMedecin() {
        return idMedecin;
    }

    public void setIdMedecin(int idMedecin) {
        this.idMedecin = idMedecin;
    }

    public int getIdDonneur() {
        return idDonneur;
    }

    public void setIdDonneur(int idDonneur) {
        this.idDonneur = idDonneur;
    }

    public boolean isAccept() {
        return accept;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }
}
