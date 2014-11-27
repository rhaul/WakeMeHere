package aaremm.com.sleepyhead.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import aaremm.com.sleepyhead.R;
import aaremm.com.sleepyhead.config.BApp;
import aaremm.com.sleepyhead.service.UserLocationService;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class AlarmActivity extends Activity {

    @InjectView(R.id.b_alarm_ok)Button b_setAlarm;
    @InjectView(R.id.tv_alarm_address)TextView tv_address;
    @InjectView(R.id.tv_alarm_radius)TextView tv_radius;

    Vibrator vib;
    NotificationManager nf;
    NotificationCompat.Builder mBuilder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        ButterKnife.inject(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        displayATRNotification();
        vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(new long[]{0,1000,500},0);
        tv_address.setText(BApp.getInstance().getDestAdress());
        tv_radius.setText(BApp.getInstance().getGeofenceRadius()+" metres away.");
        b_setAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vib.cancel();
                BApp.getInstance().resetAlarm();
                nf.cancel(15000);
                Intent pushIntent = new Intent(AlarmActivity.this, UserLocationService.class);
                stopService(pushIntent);
                finish();
            }
        });
    }

    private void displayATRNotification() {
        nf = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker("About to reach!")
                .setContentTitle("Destination Alarm")
                .setContentText(BApp.getInstance().getDestAdress()+" at "+(int)BApp.getInstance().getGeofenceRadius()+" m away.");
        nf.notify(15000, mBuilder.build());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.alarm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
