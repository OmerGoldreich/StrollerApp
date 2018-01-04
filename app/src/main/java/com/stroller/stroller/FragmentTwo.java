package com.stroller.stroller;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class FragmentTwo extends Fragment {

    public ArrayList<String> currUserFavesList = new ArrayList<String>();

    private int LastChangedItemPositionInList=-1;

    //add Firebase Database stuff
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference usersListRef;
    private DatabaseReference currentUserRef;

    private  String userID;


    public FragmentTwo() {
        // Required empty public constructor
    }


//    public ArrayList<String> currUserFavesList = new ArrayList<String>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //  return inflater.inflate(R.layout.fragment_two, container, false);


        View v = inflater.inflate(R.layout.fragment_two, container, false);

        final ListView lstItems = v.findViewById(R.id.favesList);

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

        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be useable.
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = "user8";         // should be userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        usersListRef = myRef.child("users");
        currentUserRef = usersListRef.child(userID); // should be         currentUserRef = usersListRef.child(userID);
        currentUserRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                currUserFavesList.add(dataSnapshot.getValue(String.class));
                mCustomAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                int index = LastChangedItemPositionInList;
                currUserFavesList.set(index,dataSnapshot.getValue(String.class));
                mCustomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                currUserFavesList.remove(dataSnapshot.getValue(String.class));
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
        currentUserRef = usersListRef.child(userID); // should be  currentUserRef = usersListRef.child(userID);
        Query toBeDeletedStringQuery = currentUserRef.orderByValue().equalTo(s);
        toBeDeletedStringQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}