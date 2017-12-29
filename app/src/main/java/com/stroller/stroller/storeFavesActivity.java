package com.stroller.stroller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class storeFavesActivity extends AppCompatActivity {


    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRootRef = database.getReference();
    DatabaseReference myChildRef = myRootRef.child("fave");

    Button mButtonAddToFaves;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_faves);

        //get UI elements
        mButtonAddToFaves = (Button) findViewById(R.id.buttonAddToFaves);

    }

    @Override
    protected void onStart() {
        super.onStart();
        myChildRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
/*                String text =  dataSnapshot.getValue(String.class);
                DatabaseReference newRef = myRootRef.child("fave").push();
                newRef.setValue(text);*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mButtonAddToFaves.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myChildRef.push().setValue("ukraine");
                myChildRef.child("uniqueCh").setValue("Hello Unique");
            }
        });


    }
}
