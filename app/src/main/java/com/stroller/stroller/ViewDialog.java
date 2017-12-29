package com.stroller.stroller;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by tala on 20-Dec-17.
 */

public class ViewDialog {

    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef= mFirebaseDatabase.getReference();
    private DatabaseReference usersListRef = myRef.child("users");
    private DatabaseReference currentUserRef;
    private String userID = "user9";



    public void showDialog(Activity activity, String msg){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.add_to_faves_dialog);

        TextView text = (TextView) dialog.findViewById(R.id.addtoFaves_text_dialog);
        text.setText(msg);


        Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText roadGivenName = (EditText) dialog.findViewById(R.id.road_name);
                final String roadname = roadGivenName.getText().toString(); //elmoshkele hon sheklha
                addinputToDataBase(roadname);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void addinputToDataBase(final String input) {

        currentUserRef = usersListRef.child(userID); // should be  currentUserRef = usersListRef.child(userID);
        currentUserRef.push().setValue(input);

/*
        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
        });
*/

    }


}
