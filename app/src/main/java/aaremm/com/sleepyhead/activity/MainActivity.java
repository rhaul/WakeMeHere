package aaremm.com.sleepyhead.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import aaremm.com.sleepyhead.R;
import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends Activity  implements View.OnClickListener {
    @InjectView(R.id.rl_select_bus)RelativeLayout rl_bus;
    @InjectView(R.id.rl_select_metro)RelativeLayout rl_metro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        rl_bus.setOnClickListener(this);
        rl_metro.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.rl_select_bus:{
                Intent intent = new Intent(this,BusActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.rl_select_metro:{
                Intent intent = new Intent(this,MetroActivity.class);
                startActivity(intent);
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
