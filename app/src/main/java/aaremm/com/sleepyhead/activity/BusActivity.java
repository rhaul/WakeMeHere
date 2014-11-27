package aaremm.com.sleepyhead.activity;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import aaremm.com.sleepyhead.R;
import aaremm.com.sleepyhead.adapter.PlaceAutoCompleteAdapter;
import aaremm.com.sleepyhead.config.BApp;
import aaremm.com.sleepyhead.service.UserLocationService;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class BusActivity extends FragmentActivity implements View.OnClickListener {

    @InjectView(R.id.autoTV_place_search)AutoCompleteTextView actv_search;
    @InjectView(R.id.ll_bus_settings)LinearLayout ll_alarmSettings;
    @InjectView(R.id.seekBar_bus_distance)SeekBar sb_distance;
    @InjectView(R.id.tv_bus_distanceValue)TextView tv_distanceVal;
    @InjectView(R.id.b_bus_setAlarm)Button b_setAlarm;
    @InjectView(R.id.pb_loading)ProgressBar pb_loading;


    AlertDialog dialog;

    private GoogleMap mMap;
    private Marker now,destiny;
    private Circle circle;
    private Geocoder geocoder;
    LocationManager manager;
    double destLat,destLong;
    float radius = 200;
    String destAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus);
        ButterKnife.inject(this);
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Geocoder.isPresent()) {
            geocoder = new Geocoder(this, Locale.getDefault());
        }
        actv_search.setAdapter(new PlaceAutoCompleteAdapter(this, android.R.layout.simple_dropdown_item_1line));
        actv_search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                destAddress = (String) parent.getItemAtPosition(position);
                // Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
                setDestLocation(destAddress);
            }
        });
        sb_distance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radius = (200+(progress*200));
                if(radius== 1000) {
                    tv_distanceVal.setText(1+" km");
                }else{
                    tv_distanceVal.setText(radius + " m");
                }
                showGeofence(radius);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        actv_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    setDestLocation(v.getText().toString());
                    return true;
                }
                return false;
            }
        });
        b_setAlarm.setOnClickListener(this);
    }

    private void showGeofence(double metVal) {
        if(circle != null){
            circle.remove();
        }
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(destLat, destLong))
                .radius(metVal)
                .fillColor(0x20ff0000)
                .strokeColor(Color.TRANSPARENT)
                .strokeWidth(2);

        circle = mMap.addCircle(circleOptions);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.getUiSettings().setZoomControlsEnabled(false);
            //mMap.setOnMarkerClickListener(this);
            // Check if we were successful in obtaining the map.
            if (mMap == null) {
                Toast.makeText(this, "Google Maps couldn't be loaded! Try Again", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void setDestLocation(String addr) {
        pb_loading.setVisibility(View.VISIBLE);
        actv_search.clearFocus();
        actv_search.dismissDropDown();
        InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(actv_search.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        (new GetAddressTask(this)).execute(addr);
    }
    private void setUpDestMarker(double latitude, double longitude) {
        if (destiny != null) {
            destiny.remove();
        }
        LatLng loc = new LatLng(latitude,longitude);
        destiny = mMap.addMarker(new MarkerOptions().position(loc).title("Destination"));

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(loc, 16);
        mMap.animateCamera(cameraUpdate);
        showAlarmSetOptions();
    }

    private void showAlarmSetOptions() {
        ll_alarmSettings.setVisibility(View.VISIBLE);
    }
    private void hideAlarmSetOptions() {
        ll_alarmSettings.setVisibility(View.GONE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bus, menu);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.b_bus_setAlarm:{
                checkGPS();
                break;
            }
        }
    }


    private void checkGPS() {
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            /*Toast.makeText(this, "GPS enabled -start",
                    Toast.LENGTH_SHORT).show();*/
            BApp.getInstance().setGeofenceLL(new LatLng(destLat,destLong));
            BApp.getInstance().setDestinationAddr(destAddress);
            BApp.getInstance().setGeofenceRadius(radius);
            Intent pushIntent = new Intent(this, UserLocationService.class);
            startService(pushIntent);
            IntentFilter intentFilter = new IntentFilter(BApp.BROADCAST);
            registerReceiver(onLocationChanged, intentFilter);
            hideAlarmSetOptions();

        } else {
            /*Toast.makeText(this, "GPS disabled",
                    Toast.LENGTH_SHORT).show();*/
            showDialogEnableGPS();
        }
    }

    private void showDialogEnableGPS() {
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "Enable GPS to set the alarm. Click OK to go to"
                + " location services settings.";

        builder.setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                Intent gpsOptionsIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(gpsOptionsIntent);
                                d.dismiss();
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        }
                );

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        IntentFilter intentFilter = new IntentFilter(BApp.BROADCAST);
        registerReceiver(onLocationChanged, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
        }
        unregisterReceiver(onLocationChanged);
    }

    private void displayAlarmSetNotification() {
        NotificationManager nf;
        NotificationCompat.Builder mBuilder;
        nf = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker("Destination Alarm Set")
                .setContentTitle("Destination Alarm Set")
                .setContentText(destAddress+" at "+(int)radius+" m");
        nf.notify(15000, mBuilder.build());
    }

    private BroadcastReceiver onLocationChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(BApp.INTENT_ACTION);

            if (BApp.LC_ACTION.equalsIgnoreCase(action)) {
                updateMyLocMarker();
            } else if (BApp.GEOFENCES_ACTION.equalsIgnoreCase(action)) {
                //displayCLandGeofences();
                displayAlarmSetNotification();
            }
        }
    };

    private void updateMyLocMarker() {
        if (now != null) {
            now.remove();
        }
        // Creating a LatLng object for the current location
        LatLng latLng = BApp.getInstance().getCurrentLocation();
        now = mMap.addMarker(new MarkerOptions().position(latLng).title("I am here!"));
        now.showInfoWindow();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(BApp.getInstance().getGeofenceLL());
        builder.include(latLng);
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,200);
        mMap.animateCamera(cameraUpdate);
    }

    private class GetAddressTask extends
            AsyncTask<String, Void, Boolean> {
        Context mContext;
        public GetAddressTask(Context context) {
            super();
            mContext = context;
        }
        @Override
        protected Boolean doInBackground(String... params) {
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocationName(params[0], 1);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(mContext, "Connection Timed out!", Toast.LENGTH_SHORT).show();
            }if (addresses != null && addresses.size()>0) {
                //loading.setVisibility(View.VISIBLE);
                destLat = addresses.get(0).getLatitude();
                destLong = addresses.get(0).getLongitude();
                Address address = addresses.get(0);
                destAddress = String.format(
                        "%s, %s",
                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(0) : "",
                        // Locality is usually a city
                        address.getLocality());
                return true;
            }else{
                return false;
            }
        }
        @Override
        protected void onPostExecute(Boolean value) {
            pb_loading.setVisibility(View.GONE);
            if(value){
                setUpDestMarker(destLat,destLong);
                showGeofence(radius);
            }
        }
    }
}
