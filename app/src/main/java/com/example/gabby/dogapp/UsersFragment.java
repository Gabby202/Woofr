package com.example.gabby.dogapp;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.Map;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class UsersFragment extends android.support.v4.app.Fragment {
    TextView textViewName, textViewBio, textViewAddress, textViewPhone;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ImageView imageView;
    private Uri downloadURI;
    public UsersFragment() {
        // Required empty public constructor

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();


        // Retrieve new posts as they are added to Firebase
        databaseReference.addChildEventListener(new ChildEventListener() {
            // Retrieve new posts as they are added to Firebase
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String, Object> newPost = (Map<String, Object>) snapshot.getValue();
                System.out.println("Name: " + newPost.get("name"));
                textViewName.setText("User " + newPost.get("name"));
                textViewBio.setText("User " + newPost.get("bio"));
                textViewAddress.setText("User " + newPost.get("address"));
                textViewPhone.setText("User " + newPost.get("phone"));



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
       // String[] users =  {"Alex", "Maria", "Sophie", "Laura", "Stacey", "Noelle"};
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        textViewName = (TextView) view.findViewById(R.id.textViewName);
        textViewBio = (TextView) view.findViewById(R.id.textViewBio);
        textViewAddress = (TextView) view.findViewById(R.id.textViewAddress);
        textViewPhone = (TextView) view.findViewById(R.id.textViewPhone);
        imageView =(ImageView) view.findViewById(R.id.image);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

        Bundle bundle = getArguments();
        String message = Integer.toString(bundle.getInt("count"));
        int i = bundle.getInt("count");

                DataSnapshot usersSnapshot = dataSnapshot.child("walkers");
                Iterable<DataSnapshot> usersChildren = usersSnapshot.getChildren();


                String[] names = new String[10];
                String[] bios= new String[10];
                String[] addresses= new String[10];
                String[] phones= new String[10];
                String[] userID = new String[10];

                int x = 0;
                for (DataSnapshot user : usersChildren) {

                    names[x]= user.child("name").getValue().toString();
                    bios[x]=user.child("bio").getValue().toString();
                    addresses[x]=user.child("address").getValue().toString();
                    phones[x]=user.child("phone").getValue().toString();
                    userID[x]=user.getKey().toString();
                    System.out.println(userID[x]);







                    x++;

                }

                storageReference = FirebaseStorage.getInstance().getReference();



                storageReference.child("images/"+userID[i-1]).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(getContext()).load(uri).into(imageView);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });






                //String url = "https://firebasestorage.googleapis.com/v0/b/dogapp-8bfb0.appspot.com/o/images%2F2aeb4292-fa3a-45be-a1e6-b7628132bf01?alt=media&token=cce7c8c8-f67e-4360-8b79-f8c7b177a2a8";


                textViewName.setText(names[i-1]);
                textViewBio.setText(bios[i-1]);
                textViewAddress.setText(addresses[i-1]);
                textViewPhone.setText(phones[i-1]);




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }


}
