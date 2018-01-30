package com.stroller.stroller;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.stroller.stroller.navigationPackage.Highlight;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


public class FragmentTwo extends Fragment {

    public static ArrayList<String> currUserFavesList = new ArrayList<String>();

    private int LastChangedItemPositionInList=-1;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private DatabaseReference usersListRef;
    private DatabaseReference currentUserRef;
    public static List<LatLng> faves_polyline;
    public static List<LatLng> faves_instruct_pnts;
    public static String duration_from_faves;
    public static String instructions_from_faves;
    public static List<Highlight> route_highlights;
    public static List<String> route_highlights_name;
    public static List<String> route_highlights_category;
    public static List<LatLng> route_highlights_pnts;


    private  String userID;


    public FragmentTwo() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_two, container, false);

        final ListView lstItems = v.findViewById(R.id.favesList);
        TextView emptyText = v.findViewById(android.R.id.empty);
        lstItems.setEmptyView(emptyText);

        //customized list

        final CustomAdapter mCustomAdapter = new CustomAdapter(this, currUserFavesList);
        lstItems.setAdapter(mCustomAdapter);

        registerForContextMenu(lstItems);

        lstItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Intent intent = new Intent(v.getContext(), MapsActivity.class);
                intent.putExtra("FAVES_OR_SEARCH","faves");
                startActivity(intent);
            }
        });
        lstItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String roadAtCurrPosition = currUserFavesList.get(i);
                DatabaseReference currRoadRefInDB = currentUserRef.child(roadAtCurrPosition).child("road");
                final List<LatLng> finalPolylineList = new ArrayList<>();
                final List<LatLng> finalInstructPntsList = new ArrayList<>();
                final List<LatLng> finalHighLightsPntsList = new ArrayList<>();
                final List<String> finalHighLightsCategoryList = new ArrayList<>();
                final List<String> finalHighLightsNameList = new ArrayList<>();
                final String[] duration = new String[1];
                final String[] instructions = new String[1];

                route_highlights = new ArrayList<>();
                DatabaseReference road_duration = currentUserRef.child(roadAtCurrPosition).child("duration");
                DatabaseReference road_instructions = currentUserRef.child(roadAtCurrPosition).child("instructions");
                DatabaseReference instruct_start_points = currentUserRef.child(roadAtCurrPosition).child("instruct_start_points");
                DatabaseReference highlights_pnts = currentUserRef.child(roadAtCurrPosition).child("highlights_points");
                DatabaseReference highlights_category = currentUserRef.child(roadAtCurrPosition).child("highlights_category");
                DatabaseReference highlights_name = currentUserRef.child(roadAtCurrPosition).child("highlights_name");

                currRoadRefInDB.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            //every snapshot is a point on the route
                            double lat = snapshot.child("latitude").getValue(Double.class);
                            double lng = snapshot.child("longitude").getValue(Double.class);
                            LatLng point = new LatLng(lat,lng);
                            finalPolylineList.add(point);
                        }
                        faves_polyline=finalPolylineList;
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                road_duration.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        duration[0] = dataSnapshot.getValue(String.class);
                        duration_from_faves = duration[0].toString();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                road_instructions.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        instructions[0] = dataSnapshot.getValue(String.class);
                        instructions_from_faves = instructions[0];
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                instruct_start_points.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            //every snapshot is a point on the route
                            double lat = snapshot.child("latitude").getValue(Double.class);
                            double lng = snapshot.child("longitude").getValue(Double.class);
                            LatLng point = new LatLng(lat,lng);
                            finalInstructPntsList.add(point);
                        }
                        faves_instruct_pnts=finalInstructPntsList;
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                highlights_pnts.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            //every snapshot is a highlight point on the route
                            double lat = snapshot.child("latitude").getValue(Double.class);
                            double lng = snapshot.child("longitude").getValue(Double.class);
                            LatLng point = new LatLng(lat,lng);
                            finalHighLightsPntsList.add(point);
                        }
                        route_highlights_pnts = finalHighLightsPntsList;
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                highlights_category.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            //every snapshot is a highlight category on the route
                            String category = snapshot.getValue(String.class);
                            finalHighLightsCategoryList.add(category);
                        }
                        route_highlights_category = finalHighLightsCategoryList;
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                highlights_name.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            //every snapshot is a highlight category on the route
                            String name = snapshot.getValue(String.class);
                            finalHighLightsNameList.add(name);
                        }
                        route_highlights_name = finalHighLightsNameList;
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                for(int index=0; index<finalHighLightsPntsList.size(); index++){
                    LatLng pnt = finalHighLightsPntsList.get(i);
                    String categroy = finalHighLightsCategoryList.get(i);
                    String name = finalHighLightsNameList.get(i);
                    Highlight h=new Highlight(pnt.latitude,pnt.longitude,categroy,name);
                    route_highlights.add(h);
                }
                Intent intent = new Intent(getActivity(),MapsActivity.class);
                intent.putExtra("FAVES_OR_SEARCH","faves");
                startActivity(intent);
            }
        });



        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be useable.
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        usersListRef = myRef.child("users");
        currentUserRef = usersListRef.child(userID);
        currentUserRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                currUserFavesList.add(dataSnapshot.getKey());
                mCustomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                int index = LastChangedItemPositionInList;
                for(DataSnapshot childDS: dataSnapshot.getChildren()){
                    if(childDS.equals("road_name")){
                        currUserFavesList.set(index,childDS.getValue(String.class));
                    }
                }
                mCustomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                currUserFavesList.remove((dataSnapshot.getKey()));
                mCustomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return v;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String itemValueStr = currUserFavesList.get(info.position);
        this.LastChangedItemPositionInList=info.position;

        switch (item.getItemId()) {
            case R.id.edit:
                ViewDialog alert = new ViewDialog(getActivity(), itemValueStr, 1);
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();
                break;
            case R.id.delete:
                deleteFromDataBase(itemValueStr);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
        return super.onContextItemSelected(item);
    }

    private void deleteFromDataBase(String s) {
        currentUserRef = usersListRef.child(userID);
        currentUserRef.child(s).removeValue();
    }
}