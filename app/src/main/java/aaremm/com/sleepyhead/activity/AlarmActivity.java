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
import aaremm.com.sleepyhead.service.UserActivityService;
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
        final int mode = getIntent().getIntExtra("mode", 0);
        if(mode==0) {
            displayBusATRNotification();
            tv_address.setText(BApp.getInstance().getDestAdress());
            tv_radius.setText(BApp.getInstance().getGeofenceRadius() + " metres away.");
        }else{
            displayMetroATRNotification();
            tv_address.setText(BApp.getInstance().getDestMetroStationName());
            tv_radius.setVisibility(View.GONE);
        }
        vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(new long[]{0,1000,500},0);
        b_setAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vib.cancel();
                if(mode == 0) {
                    nf.cancel(15000);
                    BApp.getInstance().resetBusAlarm();
                    Intent pushIntent = new Intent(AlarmActivity.this, UserLocationService.class);
                    stopService(pushIntent);
                }else{
                    nf.cancel(15001);
                    BApp.getInstance().resetMetroAlarm();
                    Intent push1Intent = new Intent(AlarmActivity.this, UserActivityService.class);
                    stopService(push1Intent);
                }
                finish();
            }
        });
    }

    private void displayBusATRNotification() {
        nf = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_icon2)
                .setTicker("About to reach!")
                .setContentTitle("Destination Alarm")
                .setContentText(BApp.getInstance().getDestAdress()+" at "+(int)BApp.getInstance().getGeofenceRadius()+" m away.");
        nf.notify(15000, mBuilder.build());
    }

    private void displayMetroATRNotification() {
        nf = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_icon2)
                .setTicker("About to reach!")
                .setContentTitle("Destination Alarm")
                .setContentText(BApp.getInstance().getDestMetroStationName());
        nf.notify(15001, mBuilder.build());
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
