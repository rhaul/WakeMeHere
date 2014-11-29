package aaremm.com.sleepyhead.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import aaremm.com.sleepyhead.config.BApp;
import aaremm.com.sleepyhead.object.DestGeofence;

/**
 * Created by rahul on 26-08-2014.
 */
public class UserLocationService extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener,
        LocationClient.OnAddGeofencesResultListener {
    // Global constants
    private boolean accidentProneArea = false;
    private boolean isUserActivityServiceRunning = false;
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 10;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 10;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    /*
* Use to set an expiration time for a geofence. After this amount
* of time Location Services will stop tracking the geofence.
*/
    private static final long SECONDS_PER_HOUR = 60;
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    private static final long GEOFENCE_EXPIRATION_TIME =
            GEOFENCE_EXPIRATION_IN_HOURS *
                    SECONDS_PER_HOUR *
                    MILLISECONDS_PER_SECOND;

    private static final int GEOFENCE_RADIUS = 200;

    // Define an object that holds accuracy and frequency parameters
    LocationRequest mLocationRequest;
    LocationClient mLocationClient;
    private Context mContext = BApp.getInstance();
    private Location mLocation = null;
    private int postalCode = -1;

    List<Geofence> mGeofenceList = new ArrayList<Geofence>();
    // Stores the PendingIntent used to request geofence monitoring
    private PendingIntent mGeofenceRequestIntent;


    // Defines the allowable request types.
    public enum REQUEST_TYPE {
        ADD, REMOVE_INTENT, L_UP
    }

    private REQUEST_TYPE mRequestType;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
    PendingIntent mTransitionPendingIntent;

    private boolean RESET_GEOFENCES_IN_PROGRESS = false;
    private boolean SERVICE_IS_DESTROYED = false;

    // geofence
    LatLng geofenceCords;
    float radius;

    @Override
    public void onCreate() {
        startTracking();
    }

    private void setGeofence() {
        geofenceCords = BApp.getInstance().getGeofenceLL();
        radius = BApp.getInstance().getGeofenceRadius();
        mGeofenceList.add(new DestGeofence(""+geofenceCords.latitude+geofenceCords.longitude, geofenceCords.latitude, geofenceCords.longitude,radius, GEOFENCE_EXPIRATION_TIME, Geofence.GEOFENCE_TRANSITION_ENTER).toGeofence());
        addGeofence();
    }

    private void startTracking() {
        Log.d(BApp.APPTAG, "startTracking");

        // TODO : Error if Google Play Services not available
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            mLocationClient = new LocationClient(this, this, this);

            if (!mLocationClient.isConnected() || !mLocationClient.isConnecting()) {
                mLocationClient.connect();

                // Create the LocationRequest object
                mLocationRequest = LocationRequest.create();
                // Use high accuracy
                mLocationRequest.setPriority(
                        LocationRequest.PRIORITY_HIGH_ACCURACY);
                // Set the update interval to 60 seconds
                mLocationRequest.setInterval(UPDATE_INTERVAL);
                // Set the fastest update interval to 1 second
                mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
                mRequestType = REQUEST_TYPE.L_UP;
            }
        } else {
            Log.e(BApp.APPTAG, "unable to connect to google play services.");
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mLocation == null) {
            mLocation = location;
        }
        // Report to the UI that the location was updated
        Log.d("Updated Location: ", Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude()));
        BApp.getInstance().setCurrentLocation(new LatLng(location.getLatitude(), location.getLongitude()));
        Intent temp = new Intent(BApp.LOCATION_BROADCAST);
        temp.putExtra(BApp.INTENT_ACTION, BApp.LC_ACTION);
        sendBroadcast(temp);
    }

    /**
     * Start a request for geofence monitoring by calling
     * LocationClient.connect().
     */
    public void addGeofence() {
        // Start a request to add geofences
        mRequestType = REQUEST_TYPE.ADD;
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is underway
            mInProgress = true;
            mLocationClient.connect();
        }
    }

    /**
     * Start a request to remove geofences by calling
     * LocationClient.connect()
     */
    public void removeGeofences(PendingIntent requestIntent) {
        // Record the type of removal request
        mRequestType = REQUEST_TYPE.REMOVE_INTENT;
        // Store the PendingIntent
        mGeofenceRequestIntent = requestIntent;
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        // mLocationClient = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is underway
            mInProgress = true;
            // Request a connection from the client to Location Services
            mLocationClient.connect();
        }
    }

    /*
     * Create a PendingIntent that triggers an IntentService in your
     * app when a geofence transition occurs.
     */

    private PendingIntent getTransitionPendingIntent() {
        // Create an explicit Intent
        Intent intent = new Intent(this,
                ReceiveTransitionsIntentService.class);
        /*
         * Return the PendingIntent
         */
        return PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {/*
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();*/
        /*if (mLocation != null) {
            (new GetAddressTask(this)).execute(mLocation);
        }*/

        switch (mRequestType) {

            case L_UP:

                // When the location client is connected, set mock mode
                mLocationClient.requestLocationUpdates(mLocationRequest, this);
                mLocation = mLocationClient.getLastLocation();
                geofenceCords = BApp.getInstance().getGeofenceLL();
                if(geofenceCords!=null) {
                    setGeofence();
                }
                break;
            case ADD:
                // Get the PendingIntent for the request
                mTransitionPendingIntent =
                        getTransitionPendingIntent();
                // Send a request to add the current geofences
                mLocationClient.addGeofences(
                        mGeofenceList, mTransitionPendingIntent, this);
                break;

            case REMOVE_INTENT:
                mLocationClient.removeGeofences(mGeofenceRequestIntent, new LocationClient.OnRemoveGeofencesResultListener() {
                    @Override
                    public void onRemoveGeofencesByRequestIdsResult(int i, String[] strings) {

                    }

                    @Override
                    public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent pendingIntent) {
                        // If removing the geofences was successful
                        if (statusCode == LocationStatusCodes.SUCCESS) {
                            BApp.getInstance().setGeofenceLL(null);
                            onStop();
                        }
                        mInProgress = false;

                    }
                });
                break;
        }

    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(mContext, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();

        // Turn off the request flag
        mInProgress = false;
    }


    protected void onStop() {
        // If the client is connected
        if (mLocationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
            mLocationClient.removeLocationUpdates(this);
        }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
        mLocationClient.disconnect();
    }

    @Override
    public void onDestroy() {
        SERVICE_IS_DESTROYED = true;
        if (mTransitionPendingIntent != null) {
            removeGeofences(mTransitionPendingIntent);
        } else {
            onStop();
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onAddGeofencesResult(int statusCode, String[] strings) {

        // If adding the geofences was successful
        if (LocationStatusCodes.SUCCESS == statusCode) {
            Intent temp = new Intent(BApp.LOCATION_BROADCAST);
            temp.putExtra(BApp.INTENT_ACTION, BApp.GEOFENCES_ACTION);
            sendBroadcast(temp);
        } else {
             Toast.makeText(this,"Geofences not added",Toast.LENGTH_LONG).show();
        }
        // Turn off the in progress flag and disconnect the client
        mInProgress = false;
        RESET_GEOFENCES_IN_PROGRESS = false;
    }

}
