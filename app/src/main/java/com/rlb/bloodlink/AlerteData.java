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


}
