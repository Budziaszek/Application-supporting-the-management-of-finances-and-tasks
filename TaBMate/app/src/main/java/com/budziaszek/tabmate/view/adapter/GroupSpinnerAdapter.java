package com.budziaszek.tabmate.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.budziaszek.tabmate.firestoreData.Group;


public class GroupSpinnerAdapter extends ArrayAdapter<Group> {

    private Group[] values;

    public GroupSpinnerAdapter(Context context, int textViewResourceId,
                               Group[] values) {
        super(context, textViewResourceId, values);
        this.values = values;
    }

    @Override
    public int getCount() {
        return values.length;
    }

    @Override
    public Group getItem(int position) {
        return values[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(values[position].getName());
        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                @NonNull ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(values[position].getName());

        return label;
    }
}