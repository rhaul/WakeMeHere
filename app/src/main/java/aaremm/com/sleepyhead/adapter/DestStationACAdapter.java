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

/**
 * Created by rahul on 28-11-2014.
 */
public class DestStationACAdapter extends BaseAdapter implements Filterable {

    private Context mContext;
    private List<String> stations;
    private List<String> allstations;

    public DestStationACAdapter(Context context, List<String> list) {
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
        return stations.get(index);
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
            convertView = inflater.inflate(R.layout.item_dest_station, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.tv_deststation_name)).setText(getItem(position));
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                stations = allstations;
                FilterResults filterResults = new FilterResults();
                List<String> resultList = new ArrayList<String>();
                if (constraint != null) {

                    for (int i = 0; i < stations.size(); i++) {
                        String item = stations.get(i);
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
                    stations = (List<String>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }
}