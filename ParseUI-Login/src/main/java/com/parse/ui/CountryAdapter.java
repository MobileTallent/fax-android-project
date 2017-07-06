package com.parse.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CountryAdapter extends ArrayAdapter<Country> {
    private Context mContext;
    private int mResource;
    private List<Country> mItems;

    public CountryAdapter(Context context, int resource, List<Country> objects) {
        super(context, resource,R.id.countryCode, objects);
        this.mContext = context;
        this.mResource = resource;
        this.mItems = objects;
    }

    static class ViewHolder {
        ImageView flag;
        TextView code;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        final ViewHolder holder;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(mResource, null);
            holder = new ViewHolder();
            holder.flag = (ImageView) v.findViewById(R.id.countryFlag);
            holder.code = (TextView) v.findViewById(R.id.countryCode);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        Country fn = mItems.get(position);
        holder.flag.setImageResource(fn.getFlagResources());
        holder.code.setText(fn.getName());
        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
