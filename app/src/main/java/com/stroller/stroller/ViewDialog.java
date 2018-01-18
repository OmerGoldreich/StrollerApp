package com.stroller.stroller;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.stroller.stroller.navigationPackage.DirectionFinder;
import com.stroller.stroller.navigationPackage.Route;

import java.util.List;

/**
 * Created by tala on 20-Dec-17.
 */

public class ViewDialog extends Dialog implements
        android.view.View.OnClickListener {


    public Activity activity;
    public Button add;

    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef= mFirebaseDatabase.getReference();
    private DatabaseReference usersListRef = myRef.child("users");
    private DatabaseReference currentUserRef;
    private String userID;
    PolylineOptions roadOnMap = MapsActivity.getLineOptions();
    private List<LatLng> decodedPolyline;

    private String old_road_name = "";
    private int caller_id;


    public ViewDialog(Activity actv, String itemValueStr, int id) {
        super(actv);
        // TODO Auto-generated constructor stub
        this.activity = actv;
        this.caller_id = id;
        this.old_road_name = itemValueStr;
    }
    public ViewDialog(Activity actv, String itemValueStr, int id, List<LatLng> decodedPolyline){
        super(actv);
        this.activity = actv;
        this.caller_id = id;
        this.old_road_name = itemValueStr;
        this.decodedPolyline = decodedPolyline;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(caller_id == 0){
            setContentView(R.layout.add_to_faves_dialog);
        } else {
            setContentView(R.layout.change_roadname_in_faves);
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();


        add = findViewById(R.id.btn_dialog);
        add.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        EditText roadGivenName = findViewById(R.id.road_name);
        final String roadname = roadGivenName.getText().toString();
        if(!roadname.equals("")){
            if(caller_id == 0){
                addinputToDataBase(roadname);
                dismiss();
            } else{
                changeInputInDataBase(roadname, old_road_name);
                dismiss();
            }
        }
    }

    private void addinputToDataBase(final String input) {
        currentUserRef = usersListRef.child(userID); // should be  currentUserRef = usersListRef.child(userID);

        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(input)){
                }
                else{
                    currentUserRef.child(input).child("road_name").setValue(input);
                    currentUserRef.child(input).child("road").setValue(decodedPolyline);
                    currentUserRef.child(input).child("instruct_start_points").setValue(MapsActivity.route_instruc_strt_pnts);
                    currentUserRef.child(input).child("duration").setValue(MapsActivity.duration);
                    currentUserRef.child(input).child("instructions").setValue(MapsActivity.instructions);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void changeInputInDataBase(final String newInput,String oldInput) {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        usersListRef = myRef.child("users");
        currentUserRef = usersListRef.child(userID); // should be         currentUserRef = usersListRef.child(userID);

        final DatabaseReference newRoadNode = currentUserRef.child(newInput);
        final DatabaseReference oldRoadNode = currentUserRef.child(oldInput);

        newRoadNode.child("road_name").setValue(newInput);

        DatabaseReference oldRoadDurationRef = oldRoadNode.child("duration");
        DatabaseReference oldRoadInstructionsRef = oldRoadNode.child("instructions");
        DatabaseReference oldRoadPointsRef = oldRoadNode.child("road");

        oldRoadDurationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String oldDuration = dataSnapshot.getValue(String.class);
                newRoadNode.child("duration").setValue(oldDuration);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        oldRoadInstructionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String oldInstructions = dataSnapshot.getValue(String.class);
                newRoadNode.child("instructions").setValue(oldInstructions);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        oldRoadPointsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int index=0;
                for(DataSnapshot childDS: dataSnapshot.getChildren()){
                    double lat = childDS.child("latitude").getValue(Double.class);
                    double lng = childDS.child("longitude").getValue(Double.class);
                    newRoadNode.child("road").child(String.valueOf(index)).child("latitude").setValue(lat);
                    newRoadNode.child("road").child(String.valueOf(index)).child("longitude").setValue(lng);
                    index++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        oldRoadNode.removeValue();
    }
}