package aaremm.com.sleepyhead.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import aaremm.com.sleepyhead.R;
import aaremm.com.sleepyhead.adapter.DestStationACAdapter;
import aaremm.com.sleepyhead.adapter.JourneyAdapter;
import aaremm.com.sleepyhead.adapter.SourceStationACAdapter;
import aaremm.com.sleepyhead.config.BApp;
import aaremm.com.sleepyhead.service.UserActivityService;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class MetroActivity extends Activity {

    @InjectView(R.id.acet_metro_source)
    AutoCompleteTextView actv_source;
    @InjectView(R.id.acet_metro_dest)
    AutoCompleteTextView actv_dest;
    @InjectView(R.id.ll_metro_config)
    LinearLayout ll_config;
    TextView tv_inbw;
    @InjectView(R.id.lv_metro_journey)
    ListView lv_journey;
    @InjectView(R.id.b_metro_setAlarm)
    Button b_setAlarm;

    List<Integer> SlineNos;
    List<Integer> DlineNos;
    List<String> journey;
    String source = "";
    String dest = "";
    int lineNo = -1;
    JourneyAdapter journeyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metro);
        ButterKnife.inject(this);

        BApp.getInstance().resetMetroAlarm();
        actv_source.setAdapter(new SourceStationACAdapter(this, BApp.getInstance().getAllStations()));
        actv_source.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SlineNos = BApp.getInstance().stations.get(actv_source.getText().toString());
                actv_dest.setAdapter(new DestStationACAdapter(MetroActivity.this, BApp.getInstance().getStationsOnLineNos(SlineNos, actv_source.getText().toString())));
                actv_dest.requestFocus();
                actv_dest.showDropDown();
            }
        });

        actv_dest.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (SlineNos == null) {
                    Toast.makeText(MetroActivity.this, "PLease enter Source Station first!", Toast.LENGTH_SHORT).show();
                    actv_source.requestFocus();
                    return true;
                }
                return false;
            }
        });
        actv_dest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(actv_dest.getWindowToken(), 0);
                if (journeyAdapter != null) {
                    lv_journey.setAdapter(null);
                    journeyAdapter.notifyDataSetChanged();
                }
                DlineNos = BApp.getInstance().stations.get(actv_dest.getText().toString());
                compareLineNos(SlineNos, DlineNos);
            }
        });

        b_setAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BApp.getInstance().setDestMetroStationName(dest);
                Intent pushIntent = new Intent(MetroActivity.this, UserActivityService.class);
                startService(pushIntent);
                IntentFilter intentFilter = new IntentFilter(BApp.USER_ACTIVITY_BROADCAST);
                registerReceiver(onUserActivityChanged, intentFilter);
                b_setAlarm.setVisibility(View.GONE);
                displayAlarmSetNotification();
                Toast.makeText(MetroActivity.this, "Destination Alarm Set for "+dest, Toast.LENGTH_LONG).show();
            }
        });
    }


    private void displayAlarmSetNotification() {
        NotificationManager nf;
        NotificationCompat.Builder mBuilder;
        nf = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker("Destination Alarm Set")
                .setContentTitle("Destination Alarm Set")
                .setContentText(dest);
        nf.notify(15000, mBuilder.build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(BApp.USER_ACTIVITY_BROADCAST);
        registerReceiver(onUserActivityChanged, intentFilter);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(onUserActivityChanged);
    }

    private BroadcastReceiver onUserActivityChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(BApp.INTENT_ACTION);
                journeyAdapter.notifyDataSetChanged();
        }
    };

    private void compareLineNos(List<Integer> slineNos, List<Integer> dlineNos) {
        slineNos.retainAll(dlineNos);
        if (!slineNos.isEmpty()) {
            source = actv_source.getText().toString();
            dest = actv_dest.getText().toString();
            lineNo = slineNos.get(0);
            journey = BApp.getInstance().getStationListAtLineNoFromTO(lineNo, source, dest);
            if (journey.size() < 3) {
                Toast.makeText(this, "Please select a considerable route!", Toast.LENGTH_LONG).show();
                ll_config.setVisibility(View.GONE);
                b_setAlarm.setVisibility(View.VISIBLE);
            } else {
                journeyAdapter = new JourneyAdapter(this, journey);
                lv_journey.setAdapter(journeyAdapter);
                ll_config.setVisibility(View.VISIBLE);
                b_setAlarm.setVisibility(View.VISIBLE);
            }
            //Toast.makeText(this, "Line No: " + slineNos.get(0).toString(), Toast.LENGTH_LONG).show();
        } else {
            ll_config.setVisibility(View.GONE);
            Log.d("s/d :", actv_source.getText().toString() + "/" + actv_dest.getText().toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.metro, menu);
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
