package com.stroller.stroller;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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
    private String userID = "user9";

    private String old_road_name = "";
    private int caller_id;


    public ViewDialog(Activity actv, String itemValueStr, int id) {
        super(actv);
        // TODO Auto-generated constructor stub
        this.activity = actv;
        this.caller_id = id;
        this.old_road_name = itemValueStr;
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
        currentUserRef.push().setValue(input);


        /*currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(input)){
                    //print "this name already exists in dialog"
                }
                else{
                    currentUserRef.push().setValue(input);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
    }

    private void changeInputInDataBase(final String newInput,String oldInput) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = "user9";         // should be userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        usersListRef = myRef.child("users");
        currentUserRef = usersListRef.child(userID); // should be         currentUserRef = usersListRef.child(userID);

        Query toBeChangedStringQuery = currentUserRef.orderByValue().equalTo(oldInput);
        toBeChangedStringQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().setValue(newInput);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
