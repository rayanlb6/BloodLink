package com.rlb.bloodlink;

public interface ClientCallback {
    void onClientLoaded(Client client);
    void onError(Exception e);
}
