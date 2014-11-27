package aaremm.com.sleepyhead.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import aaremm.com.sleepyhead.R;
import aaremm.com.sleepyhead.config.BApp;


/**
 * Created by rahul on 02-11-2014.
 */
public class ActivityRecognitionIS extends IntentService {

    public boolean IS_LOOK_ACTIVITY_VISIBLE = false;
    public String USER_ACTIVITY = "";
    NotificationManager nf;
    NotificationCompat.Builder mBuilder;
    int notifyID = 10001;
    Intent iDoI, iDontI;
    PendingIntent iDoPI, iDontPI;
    public ActivityRecognitionIS() {
        super("ActivityRecognitionIS");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // If the incoming intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {
            // Get the update
            ActivityRecognitionResult result =
                    ActivityRecognitionResult.extractResult(intent);
            // Get the most probable activity
            DetectedActivity mostProbableActivity =
                    result.getMostProbableActivity();
            /*
             * Get the probability that this activity is the
             * the user's actual activity
             */
            int confidence = mostProbableActivity.getConfidence();
            /*
             * Get an integer describing the type of activity
             */
            int activityType = mostProbableActivity.getType();
            doSomethingFromType(activityType);

            /*
             * At this point, you have retrieved all the information
             * for the current update. You can display this
             * information to the user in a notification, or
             * send it to an Activity or Service in a broadcast
             * Intent.
             */
        } else {
            /*
             * This implementation ignores intents that don't contain
             * an activity update. If you wish, you can report them as
             * errors.
             */
        }
    }

    /**
     * Map detected activity types to strings
     *
     * @param activityType The detected activity type
     * @return A user-readable name for the type
     */
    private void doSomethingFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                // subway or bus
                if (BApp.getInstance().isAlarmNeeded()){
                    showAlarmNotification();
                }
                Log.d("Activity", "Vehicle");
                break;

        }
        Log.d("UserActivity", "Something");
    }

    private void showAlarmNotification() {
        nf = (NotificationManager) BApp.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        iDoI = new Intent(this, UserChoice.class);
        iDoI.setAction(BApp.USER_ACTION_YES);
        iDoPI = PendingIntent.getBroadcast(this, 12345, iDoI, PendingIntent.FLAG_UPDATE_CURRENT);

        iDontI = new Intent(this, UserChoice.class);
        iDontI.setAction(BApp.USER_ACTION_NO);
        iDontPI = PendingIntent.getBroadcast(this, 12345, iDontI, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(this)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker("Need WAKE UP Alarm?")
                .setContentTitle("Wake Up Alarm")
                .setContentText("Do you need to be awakened before reaching certain place?")
                .setVibrate(new long[]{0, 1000, 1000,1000})
                .addAction(R.drawable.ic_launcher, "Yes", iDoPI)
                .addAction(R.drawable.ic_launcher, "No", iDontPI);
        nf.notify(notifyID, mBuilder.build());

    }
}
