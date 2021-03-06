package com.kulak.izabel.cameraapp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class NavDrawerListAdapter extends BaseAdapter {

    private static final String TAG = "NavDrawerListAdapter";
    private Context context;
    private ArrayList<NavDrawerItem> navDrawerItems;

    public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
    }

    @Override
    public int getCount() {
        return navDrawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        return navDrawerItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.nav_list_item, null);
        }

        Log.d(TAG, "convertView: "+ convertView.toString());
        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon_menu_item);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.text_menu_item);

        Log.d("NawDrawerItem", "position: "+ position);
        NavDrawerItem navDrawerItem = navDrawerItems.get(position);
        imgIcon.setImageResource(navDrawerItem.getIcon());
        txtTitle.setText(navDrawerItem.getTitle());

        return convertView;
    }
}