package com.richez.ladylyn.buscommuterapp;

import android.app.Service;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Ricojhon on 10/02/2019.
 */

public class ConnectionDetector {
    Context context;

    public ConnectionDetector(Context context) {
        this.context = context;
    }

    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }
}
