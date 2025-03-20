package com.app.jamis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.ImageView;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private ImageView noInternetImageView;

    public NetworkChangeReceiver(ImageView noInternetImageView) {
        this.noInternetImageView = noInternetImageView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isConnected(context)) {
            noInternetImageView.setVisibility(View.GONE); // Hide "No Internet" image
        } else {
            noInternetImageView.setVisibility(View.VISIBLE); // Show "No Internet" image
        }
    }

    private boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }
}