package aaremm.com.sleepyhead.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
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
    @InjectView(R.id.tv_metro_route)
    TextView tv_route;
    @InjectView(R.id.lv_metro_journey)
    ListView lv_journey;
    @InjectView(R.id.b_metro_setAlarm)
    Button b_setAlarm;


    NotificationManager nf;
    NotificationCompat.Builder mBuilder;

    List<Integer> SlineNos;
    List<Integer> DlineNos;
    List<String> journey;
    String source = "";
    String dest = "";
    int lineNo = -1;
    JourneyAdapter journeyAdapter;

    AlertDialog alertDialog;

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
                    Toast.makeText(MetroActivity.this, "Please enter Source Station first!", Toast.LENGTH_SHORT).show();
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
                if(BApp.getInstance().getSPBoolean("metroalarm")){
                    if (journeyAdapter != null) {
                        lv_journey.setAdapter(null);
                        journeyAdapter.notifyDataSetChanged();
                    }
                    Intent push1Intent = new Intent(MetroActivity.this, UserActivityService.class);
                    stopService(push1Intent);
                    b_setAlarm.setText("SET ALARM");
                    Toast.makeText(MetroActivity.this, "Destination Alarm Disabled for " + dest, Toast.LENGTH_LONG).show();
                    ll_config.setVisibility(View.GONE);
                    actv_source.setText("");
                    actv_dest.setText("");
                    actv_source.requestFocus();
                    nf.cancel(15001);
                    BApp.getInstance().setSPBoolean("metroalarm", false);
                }else {
                    BApp.getInstance().setDestMetroStationName(dest);
                    Intent pushIntent = new Intent(MetroActivity.this, UserActivityService.class);
                    startService(pushIntent);
                    IntentFilter intentFilter = new IntentFilter(BApp.USER_ACTIVITY_BROADCAST);
                    registerReceiver(onUserActivityChanged, intentFilter);
                    b_setAlarm.setText("DISABLE ALARM");
                    displayAlarmSetNotification();
                    Toast.makeText(MetroActivity.this, "Destination Alarm Set for " + dest, Toast.LENGTH_LONG).show();
                    BApp.getInstance().setSPBoolean("metroalarm", true);
                }
            }
        });

        lv_journey.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i !=(journeyAdapter.getCount()-1)){
                    showEditDialog(i);
                }
            }
        });
    }


    private void displayAlarmSetNotification() {
        nf = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_icon2)
                .setTicker("Destination Alarm Set")
                .setContentTitle("Destination Alarm Set")
                .setContentText(dest);
        nf.notify(15001, mBuilder.build());
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
                int lineColor = BApp.getInstance().lineColors[lineNo-1];
                tv_route.setText("Line No "+lineNo);
                tv_route.setTextColor(getResources().getColor(BApp.getInstance().lineColors[lineNo-1]));
                journeyAdapter = new JourneyAdapter(this, journey,lineColor);
                lv_journey.setAdapter(journeyAdapter);
                ll_config.setVisibility(View.VISIBLE);
            }
            //Toast.makeText(this, "Line No: " + slineNos.get(0).toString(), Toast.LENGTH_LONG).show();
        } else {
            ll_config.setVisibility(View.GONE);
            Log.d("s/d :", actv_source.getText().toString() + "/" + actv_dest.getText().toString());
        }
    }

    private void showEditDialog(final int position) {

        // get prompts.xml view

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dialog_station_option, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setTitle("Select "+journeyAdapter.getItem(position)+" station");
        final Button b_cl = (Button)promptsView.findViewById(R.id.b_dialog_cl);
        final Button b_aoff = (Button)promptsView.findViewById(R.id.b_dialog_aoff);

        b_cl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BApp.getInstance().setCurrentStationNo(position);
                BApp.getInstance().setStatus(0); // 0- i am here
                journeyAdapter.notifyDataSetChanged();
                if(BApp.getInstance().getSPBoolean("metroalarm")) {
                    if (BApp.getInstance().getCurrentStationNo() >= BApp.getInstance().getAlarmStationNo()) {
                        startActivity(new Intent(MetroActivity.this, AlarmActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK).putExtra("mode", 1));
                    }
                }
                alertDialog.cancel();
            }
        });

        b_aoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BApp.getInstance().setAlarmStationNo(position);
                journeyAdapter.notifyDataSetChanged();
                if(BApp.getInstance().getSPBoolean("metroalarm")) {
                    if (BApp.getInstance().getCurrentStationNo() >= BApp.getInstance().getAlarmStationNo()) {
                        startActivity(new Intent(MetroActivity.this, AlarmActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK).putExtra("mode", 1));
                    }
                }
                alertDialog.cancel();
            }
        });

        // create alert dialog
        alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
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
