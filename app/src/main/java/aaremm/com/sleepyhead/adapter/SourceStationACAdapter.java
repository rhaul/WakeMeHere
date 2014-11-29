package aaremm.com.sleepyhead.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import aaremm.com.sleepyhead.R;
import aaremm.com.sleepyhead.object.Station;

/**
 * Created by rahul on 27-11-2014.
 */
public class SourceStationACAdapter extends BaseAdapter implements Filterable {

    private Context mContext;
    private List<Station> stations;
    private List<Station> allstations;

    public SourceStationACAdapter(Context context, List<Station> list) {
        mContext = context;
        allstations = list;
        stations = allstations;
    }

    @Override
    public int getCount() {
        return stations.size();
    }

    @Override
    public String getItem(int index) {
        return stations.get(index).getName();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_station, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.tv_station_name)).setText(stations.get(position).getName());
        List<Integer> lns = stations.get(position).getLineNo();
        String lnss = "Line "+lns.toString();
        ((TextView) convertView.findViewById(R.id.tv_station_line)).setText(lnss);
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                stations = allstations;
                FilterResults filterResults = new FilterResults();
                List<Station> resultList = new ArrayList<Station>();
                if (constraint != null) {

                    for (int i=0;i<stations.size();i++){
                        String item = stations.get(i).getName();
                        if (item.toLowerCase().contains(constraint)) {
                            resultList.add(stations.get(i));
                        }
                    }

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    stations = (List<Station>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }
}