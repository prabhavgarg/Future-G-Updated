package com.gargprabhav.future_g;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gargprabhav.future_g.GetSetListView;
import com.gargprabhav.future_g.R;

import java.util.ArrayList;

public class ListViewAdapter extends ArrayAdapter<GetSetListView>{
    private Activity context;
    private final ArrayList<GetSetListView> itemValues;

    public ListViewAdapter(Activity context, ArrayList<GetSetListView> values) {
        super(context, -1, values);
        this.context = context;
        this.itemValues = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_item, null, true);
        TextView itemTitle = (TextView) rowView.findViewById(R.id.item_title);
        TextView itemDate = (TextView) rowView.findViewById(R.id.item_date);
        ImageView itemImage = (ImageView) rowView.findViewById(R.id.item_image);
        itemTitle.setText(itemValues.get(position).getTitle());
        itemDate.setText(itemValues.get(position).getDate());
        itemImage.setImageResource(itemValues.get(position).getImage());
        return rowView;
    }
}