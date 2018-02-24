package com.stroller.stroller;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stroller.stroller.navigationPackage.Highlight;

import java.util.ArrayList;
import java.util.List;


public class ViewDialog extends Dialog implements
        android.view.View.OnClickListener {


    public Activity activity;
    public Button add;

    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth;
    private DatabaseReference myRef= mFirebaseDatabase.getReference();
    private DatabaseReference usersListRef = myRef.child("users");
    private DatabaseReference currentUserRef;
    private String userID;
    private List<LatLng> decodedPolyline;

    private String old_road_name = "";
    private int caller_id;
    private ArrayList<String> faves_list;

    private TextView name_evaluation = null;
    private EditText roadGivenName=null;
    ViewDialog(Activity actv, String itemValueStr, int id) {
        super(actv);
        // TODO Auto-generated constructor stub
        this.activity = actv;
        this.caller_id = id;
        this.old_road_name = itemValueStr;
    }
    ViewDialog(Activity actv, String itemValueStr, int id, List<LatLng> decodedPolyline){
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
        faves_list = FragmentTwo.exportCurrUserFavesList; //changed on 31/1 ,was currUserFavesList

        name_evaluation = findViewById(R.id.nameReport);
        roadGivenName = findViewById(R.id.road_name);
        roadGivenName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                 }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(checkIfInputExists(String.valueOf(s))){
                    name_evaluation.setText(R.string.exists);
                    add.setEnabled(false);
                    
                }
                else {
                    name_evaluation.setText("");
                    add.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        ImageButton addToFavesFromMapsActivity = this.activity.findViewById(R.id.imageButton);
        if(addToFavesFromMapsActivity != null){
            addToFavesFromMapsActivity.setEnabled(false);
            addToFavesFromMapsActivity.setImageResource(R.drawable.faves);
        }
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


    private boolean checkIfInputExists(String input) {
        for(String entry:faves_list){
            if(entry.equals(input)){
                return true;
            }
        }
        return false;
    }

    private void addinputToDataBase(final String input) {
        currentUserRef = usersListRef.child(userID);
        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(input)){
                    currentUserRef.child(input).child("road_name").setValue(input);
                    currentUserRef.child(input).child("road").setValue(decodedPolyline);
                    currentUserRef.child(input).child("instruct_start_points").setValue(MapsActivity.route_instruc_strt_pnts);
                    currentUserRef.child(input).child("duration").setValue(MapsActivity.duration);
                    currentUserRef.child(input).child("duration_in_minutes").setValue(String.valueOf(MapsActivity.stroller_duration_minutes));
                    currentUserRef.child(input).child("googleMaps_duration").setValue(String.valueOf(MapsActivity.google_original_duration));
                    currentUserRef.child(input).child("instructions").setValue(MapsActivity.instructions);

                    List<Highlight> highlights = MapsActivity.highlights;
                    List<LatLng> highlights_pnts=new ArrayList<>();
                    List<String> highlights_category=new ArrayList<>();
                    List<String> highlights_name=new ArrayList<>();

                    for(Highlight highlight:highlights){
                        highlights_pnts.add(new LatLng(highlight.latitude,highlight.longitude));
                        highlights_category.add(highlight.category);
                        highlights_name.add(highlight.name);
                    }
                    currentUserRef.child(input).child("highlights_points").setValue(highlights_pnts);
                    currentUserRef.child(input).child("highlights_category").setValue(highlights_category);
                    currentUserRef.child(input).child("highlights_name").setValue(highlights_name);
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
        currentUserRef = usersListRef.child(userID);

        final DatabaseReference newRoadNode = currentUserRef.child(newInput);
        final DatabaseReference oldRoadNode = currentUserRef.child(oldInput);

        DatabaseReference oldRoadDurationRef = oldRoadNode.child("duration");
        DatabaseReference oldRoad_duration_in_minutes_Ref = oldRoadNode.child("duration_in_minutes");
        DatabaseReference oldRoad_googleMaps_duration_Ref = oldRoadNode.child("googleMaps_duration");

        DatabaseReference oldRoadInstructionsRef = oldRoadNode.child("instructions");
        DatabaseReference oldRoadPointsRef = oldRoadNode.child("road");
        DatabaseReference oldRoad_instruct_start_points_Ref = oldRoadNode.child("instruct_start_points");
        DatabaseReference oldRoad_highlights_points_Ref = oldRoadNode.child("highlights_points");
        DatabaseReference oldRoad_highlights_category_Ref = oldRoadNode.child("highlights_category");
        DatabaseReference oldRoad_highlights_name_Ref = oldRoadNode.child("highlights_name");

        newRoadNode.child("road_name").setValue(newInput);
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
        oldRoad_duration_in_minutes_Ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String oldDuration = dataSnapshot.getValue(String.class);
                newRoadNode.child("duration_in_minutes").setValue(oldDuration);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        oldRoad_googleMaps_duration_Ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String oldDuration = dataSnapshot.getValue(String.class);
                newRoadNode.child("googleMaps_duration").setValue(oldDuration);
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
        oldRoad_instruct_start_points_Ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int index=0;
                for(DataSnapshot childDS: dataSnapshot.getChildren()){
                    double lat = childDS.child("latitude").getValue(Double.class);
                    double lng = childDS.child("longitude").getValue(Double.class);
                    newRoadNode.child("instruct_start_points").child(String.valueOf(index)).child("latitude").setValue(lat);
                    newRoadNode.child("instruct_start_points").child(String.valueOf(index)).child("longitude").setValue(lng);
                    index++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        oldRoad_highlights_points_Ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int index=0;
                for(DataSnapshot childDS: dataSnapshot.getChildren()){
                    double lat = childDS.child("latitude").getValue(Double.class);
                    double lng = childDS.child("longitude").getValue(Double.class);
                    newRoadNode.child("highlights_points").child(String.valueOf(index)).child("latitude").setValue(lat);
                    newRoadNode.child("highlights_points").child(String.valueOf(index)).child("longitude").setValue(lng);
                    index++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        oldRoad_highlights_category_Ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int index=0;
                for(DataSnapshot childDS: dataSnapshot.getChildren()){
                    String category = childDS.getValue(String.class);
                    newRoadNode.child("highlights_category").child(String.valueOf(index)).setValue(category);
                    index++;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        oldRoad_highlights_name_Ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int index=0;
                for(DataSnapshot childDS: dataSnapshot.getChildren()){
                    String highlightName = childDS.getValue(String.class);
                    newRoadNode.child("highlights_name").child(String.valueOf(index)).setValue(highlightName);
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