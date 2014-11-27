package aaremm.com.sleepyhead.service;

import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import aaremm.com.sleepyhead.activity.MainActivity;
import aaremm.com.sleepyhead.config.BApp;

/**
 * Created by rahul on 02-11-2014.
 */
public class UserChoice extends BroadcastReceiver {

    DevicePolicyManager mDPM;
    NotificationManager nm;
    LocationManager manager;

    @Override
    public void onReceive(Context context, Intent intent) {
        manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String action = intent.getAction();

        if (BApp.USER_ACTION_YES.equalsIgnoreCase(action)) {
            // start set alarm activity
            BApp.getInstance().startActivity(new Intent(BApp.getInstance(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else if (BApp.USER_ACTION_NO.equalsIgnoreCase(action)) {
            // voluntarily not needed
            BApp.getInstance().setSPBoolean(BApp.WA_NOT_NEEDED_STATUS,true);
            BApp.getInstance().setSPLong(BApp.WA_NOT_NEEDED_TIME_VALUE, System.currentTimeMillis());
        }
    }
}