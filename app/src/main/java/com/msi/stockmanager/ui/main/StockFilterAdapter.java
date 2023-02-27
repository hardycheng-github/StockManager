package com.msi.stockmanager.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.msi.stockmanager.R;

import java.util.ArrayList;
import java.util.List;

public abstract class StockFilterAdapter extends ArrayAdapter<String> {
    public static final int SUGGESTION_LIMITS = 15;
    Context mContext;
    List<String> items, tempItems, suggestions;

    public StockFilterAdapter(Context context, List<String> items) {
        super(context, R.layout.stock_filter_dropdown_item, items);
        this.mContext = context;
        this.items = items;
        tempItems = new ArrayList<>(items); // this makes the difference.
        suggestions = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.stock_filter_dropdown_item, null);
        }
        String val = getItem(position);
        if (val != null) {
            View container = convertView.findViewById(R.id.container);
            container.setOnClickListener(v->onItemSelected(position, val));
            TextView text = convertView.findViewById(R.id.text);
            if (text != null) {
                text.setText(val);
            }
        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    /**
     * Custom Filter implementation for custom suggestions we provide.
     */
    Filter nameFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            String str = (String) resultValue;
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                for (String val : tempItems) {
                    if (val.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions.add(val);
                        if(SUGGESTION_LIMITS > 0 && suggestions.size() >= SUGGESTION_LIMITS){
                            break;
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<String> filterList = (ArrayList<String>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (String val : filterList) {
                    add(val);
                    notifyDataSetChanged();
                }
            }
        }
    };

    public abstract void onItemSelected(int position, String target);
}
