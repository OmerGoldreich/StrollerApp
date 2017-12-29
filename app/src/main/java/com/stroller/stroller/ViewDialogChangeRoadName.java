package com.stroller.stroller;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by tala on 21-Dec-17.
 */

public class ViewDialogChangeRoadName {
    private String new_road_name;

    //add Firebase Database stuff
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference usersListRef;
    private DatabaseReference currentUserRef;

    private  String userID;

    public ViewDialogChangeRoadName(String itemValueStr) {
        this.new_road_name=itemValueStr;
    }


    public void showDialog(Activity activity, String msg){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.change_roadname_in_faves);

        TextView text = (TextView) dialog.findViewById(R.id.change_name_msg);
        text.setText(msg);

        Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog_new_name);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText roadNewGivenName = (EditText) dialog.findViewById(R.id.change_road_name);
                final String roadNewname = roadNewGivenName.getText().toString();
                changeInputInDataBase(roadNewname,new_road_name);
                dialog.dismiss();
            }
        });
        dialog.show();
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
