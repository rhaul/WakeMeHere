package aaremm.com.sleepyhead.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import aaremm.com.sleepyhead.R;
import aaremm.com.sleepyhead.config.BApp;

/**
 * Created by rahul on 29-11-2014.
 */
public class JourneyAdapter extends BaseAdapter{
    private List<String> jStationList;
    private Context mContext;
    private int color;
    public JourneyAdapter(Context context, List<String> d, int lineColor) {
        this.mContext = context;
        this.jStationList = d;
        color = lineColor;
        BApp.getInstance().setAlarmStationNo(jStationList.size()-2);
    }
    public int getCount() {
        return jStationList.size();
    }

    public String getItem(int position) {
        return jStationList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {

        public TextView type;
        public TextView name;
        public TextView status;

    }
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        ViewHolder holder;

        if (vi == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.item_journey_station, parent, false);

            holder = new ViewHolder();
            holder.type = (TextView) vi.findViewById(R.id.tv_jStation_type);
            holder.type.setBackgroundColor(mContext.getResources().getColor(color));
            holder.name = (TextView) vi.findViewById(R.id.tv_jStation_name);
            holder.status = (TextView) vi.findViewById(R.id.tv_jStation_status);
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        holder.name.setText(jStationList.get(position));
        if(position == 0){
            holder.type.setText("S");
        }else if(position == (getCount()-1)){
            holder.type.setText("D");
        }
        else if(position == BApp.getInstance().getAlarmStationNo()){
            holder.type.setText("A");
        }else {
            holder.type.setText("O");
        }
        if(position == BApp.getInstance().getCurrentStationNo()) {
            if (BApp.getInstance().getStatus() == 0) {
                holder.status.setText("I'm here");
                holder.status.setVisibility(View.VISIBLE);
            } else {
                holder.status.setText("I just left");
                holder.status.setVisibility(View.VISIBLE);
            }
        }else if(position == BApp.getInstance().getAlarmStationNo()){
            holder.status.setText("Alarm goes off here");
            holder.status.setVisibility(View.VISIBLE);
        }else{
            holder.status.setVisibility(View.GONE);
        }
        return vi;

    }
}