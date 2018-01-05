package com.stroller.stroller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

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



    private class Holder{
        TextView textView;
        ImageView imageView;
        Spinner spinner;
    }
    private Holder holder;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView;
        rowView = inflater.inflate(R.layout.list_row_layout, null);
        TextView textView = (TextView) rowView.findViewById(R.id.row_text);
        textView.setText((CharSequence) mylist.get(position));

        ImageView imageView = (ImageView) rowView.findViewById(R.id.list_loc_icon);
        imageView.setImageResource(R.drawable.like2);



/*
        if(convertView==null){
            Holder holder = new Holder();
            if(holder==null)return convertView;
            inflater= (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_row_layout,null);
            holder.textView = (TextView) convertView.findViewById(R.id.row_text) ;
            holder.imageView = (ImageView) convertView.findViewById(R.id.list_loc_icon);
            holder.spinner = (Spinner)convertView.findViewById(R.id.spinner);
            convertView.setTag(holder);
        }
        else{
            holder = (Holder)convertView.getTag();
            if(holder==null)return convertView;

        }
        holder.textView.setText((CharSequence) mylist.get(position));
        holder.imageView.setImageResource(R.drawable.like);
        if(holder.spinner==null){return convertView;}
        holder.spinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(context, view);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.options_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        return true;
                    }
                });

                popup.show();//showing popup menu
            }
        });

        */
        return rowView;
    }
}
