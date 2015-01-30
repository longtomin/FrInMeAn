package de.radiohacks.frinmean.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.io.Serializable;

import de.radiohacks.frinmean.Constants;

public class BroadcastNotifier {

    private LocalBroadcastManager mBroadcaster;

    public BroadcastNotifier(Context context) {

        // Gets an instance of the support library local broadcastmanager
        mBroadcaster = LocalBroadcastManager.getInstance(context);
    }

    public void notifyProgressSerialize(Serializable Data, String Action) {

        Intent localIntent = new Intent();

        // The Intent contains the custom broadcast action for this app
        localIntent.setAction(Action);

        // Puts log data into the Intent
        localIntent.putExtra(Constants.BROADCAST_DATA, Data);
        localIntent.addCategory(Intent.CATEGORY_DEFAULT);

        // Broadcasts the Intent
        mBroadcaster.sendBroadcast(localIntent);
    }

    public void notifyProgress(String Data, String Action) {

        Intent localIntent = new Intent();

        // The Intent contains the custom broadcast action for this app
        localIntent.setAction(Action);

        // Puts log data into the Intent
        localIntent.putExtra(Constants.BROADCAST_DATA, Data);
        localIntent.addCategory(Intent.CATEGORY_DEFAULT);

        // Broadcasts the Intent
        mBroadcaster.sendBroadcast(localIntent);
    }
}