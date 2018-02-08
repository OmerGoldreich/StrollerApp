package com.stroller.stroller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class HighlightAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> titles;
    private ArrayList<Integer> images;
    private static LayoutInflater inflater = null;

    HighlightAdapter(Context context, ArrayList<String> titles, ArrayList<Integer> images) {
        this.titles = titles;
        this.images = images;
        this.context = context;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return titles.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = inflater.inflate(R.layout.highlights_row, null);
        TextView textView = rowView.findViewById(R.id.row_text);
        ImageView imageView = rowView.findViewById(R.id.list_loc_icon);
        textView.setText(titles.get(position));
        imageView.setImageResource(images.get(position));
        return rowView;
    }
}

