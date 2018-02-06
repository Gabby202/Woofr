package com.example.gabby.dogapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;


public class UsersFragment extends android.support.v4.app.Fragment {
    TextView textView;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    public UsersFragment() {
        // Required empty public constructor

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Retrieve new posts as they are added to Firebase
        databaseReference.addChildEventListener(new ChildEventListener() {
            // Retrieve new posts as they are added to Firebase
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String, Object> newPost = (Map<String, Object>) snapshot.getValue();
                System.out.println("Name: " + newPost.get("name"));
                textView.setText("User " + newPost.get("name"));

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
            //... ChildEventListener also defines onChildChanged, onChildRemoved,
            //    onChildMoved and onCanceled, covered in later sections.
        });


        // Inflate the layout for this fragment
        String[] users =  {"Alex", "Maria", "Sophie", "Laura", "Stacey", "Noelle"};
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        textView = (TextView) view.findViewById(R.id.textView2);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

        Bundle bundle = getArguments();
        String message = Integer.toString(bundle.getInt("count"));
        int i = bundle.getInt("count");

                DataSnapshot usersSnapshot = dataSnapshot.child("users");
                Iterable<DataSnapshot> usersChildren = usersSnapshot.getChildren();


                String[] names = new String[10];
                String[] bio= new String[10];
                int x = 0;
                for (DataSnapshot user : usersChildren) {

                    names[x]= user.child("name").getValue().toString();
                    bio[x]=user.child("bio").getValue().toString();
                    System.out.println(names[x] + "this is the array shit");

                    x++;

                }

                textView.setText(names[i-1]+ " "+bio[i-1]);



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }


}
