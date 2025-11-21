package com.rlb.bloodlink;

import com.google.firebase.database.DatabaseError;

import java.util.List;

public interface AlertesCallback {
    void onAlertesLoaded(List<Alerte> alertes);
    void onError(DatabaseError error);
}
