package com.stroller.stroller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class FragmentTwo extends Fragment {

    public FragmentTwo() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //  return inflater.inflate(R.layout.fragment_two, container, false);


        View v = inflater.inflate(R.layout.fragment_two, container, false);

        ListView lstItems = (ListView)v.findViewById(R.id.favesList);

        ArrayList<String> prueba = new ArrayList<String>();
        prueba.add("Paris1");
        prueba.add("Paris2");
        prueba.add("Alicante");
        //classic list
        // final ArrayAdapter<String> allItemsAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(), android.R.layout.simple_list_item_1,prueba);
        //lstItems.setAdapter(allItemsAdapter);


        //customized list
        lstItems.setAdapter(new CustomAdapter(this, prueba));

        lstItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Intent intent = new Intent(v.getContext(), MapsActivity.class);
                intent.putExtra("FAVES_OR_SEARCH","faves");
                startActivity(intent);
            }
        });
        return v;
    }

}