package com.manish.weatherforecast.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class NetworkChangeReceiver extends BroadcastReceiver {

    //If network is not active then it broadcast receiver active and show message to user network not active or disconnect network
    @Override
    public void onReceive(Context context, Intent intent) {
        onNetworkChange();
    }
    public abstract void onNetworkChange();
}