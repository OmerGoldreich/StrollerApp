package com.stroller.stroller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class CustomAdapter extends BaseAdapter {
    private Context context;
    private ArrayList mylist;
    private static LayoutInflater inflater=null;
    public CustomAdapter(FragmentTwo fragmentTwo, ArrayList list) {
        this.mylist=list;
        this.context=fragmentTwo.getContext();
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mylist.size();
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
        View rowView;
        rowView = inflater.inflate(R.layout.list_row_layout, null);
        TextView textView = (TextView) rowView.findViewById(R.id.row_text);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.list_loc_icon);
        textView.setText((CharSequence) mylist.get(position));
        imageView.setImageResource(R.drawable.like);
        return rowView;
    }
}
