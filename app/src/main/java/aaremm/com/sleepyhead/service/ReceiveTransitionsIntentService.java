package aaremm.com.sleepyhead.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.List;

import aaremm.com.sleepyhead.activity.AlarmActivity;

public class ReceiveTransitionsIntentService extends IntentService {
    /**
     * Sets an identifier for the service
     */
    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");
    }

    /**
     * Handles incoming intents
     *
     * @param intent The Intent sent by Location Services. This
     *               Intent is provided
     *               to Location Services (inside a PendingIntent) when you call
     *               addGeofences()
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // First check for errors
        if (LocationClient.hasError(intent)) {
            // Get the error code with a static method
            int errorCode = LocationClient.getErrorCode(intent);
            // Log the error
            Log.e("ReceiveTransitionsIntentService",
                    "Location Services error: " +
                            Integer.toString(errorCode)
            );
            /*
             * You can also send the error code to an Activity or
             * Fragment with a broadcast Intent
             */
        /*
         * If there's no error, get the transition type and the IDs
         * of the geofence or geofences that triggered the transition
         */
        } else {
            // Get the type of transition (entry or exit)
            int transitionType =
                    LocationClient.getGeofenceTransition(intent);
            List<Geofence> mList =
                    LocationClient.getTriggeringGeofences(intent);
            // Test that a valid transition was reported
            if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                startActivity(new Intent(this, AlarmActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK));

            }
        }
    }
}