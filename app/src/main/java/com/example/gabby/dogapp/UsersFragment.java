package com.example.gabby.dogapp;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.media.Rating;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
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
    TextView textViewName, textViewRating;
    private Button smsButton;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ImageView imageView;
    private RatingBar ratingBar;
    private Uri downloadURI;
    private float rating = 4;
    String[] names = new String[10];
    Float[] ratingAvgArray = new Float [10];
    //    String[] bios= new String[10];
//    String[] addresses= new String[10];
//    String[] phones= new String[10];
    String[] userID = new String[10];
    int i;

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
//                textViewName.setText("User " + newPost.get("name"));
//                textViewBio.setText("User " + newPost.get("bio"));
//                textViewAddress.setText("User " + newPost.get("address"));
//                textViewPhone.setText("User " + newPost.get("phone"));



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
//        textViewRating = (TextView) view.findViewById(R.id.textViewRating);
//        textViewBio = (TextView) view.findViewById(R.id.textViewBio);
//        textViewAddress = (TextView) view.findViewById(R.id.textViewAddress);
//        textViewPhone = (TextView) view.findViewById(R.id.textViewPhone);
        imageView =(ImageView) view.findViewById(R.id.image);
        ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
//        smsButton = (Button) view.findViewById(R.id.smsButton);

//        smsButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                sendSMSMessage();
//            }
//        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

        Bundle bundle = getArguments();
        String message = Integer.toString(bundle.getInt("count"));
        i = bundle.getInt("count");



                DataSnapshot usersSnapshot = dataSnapshot.child("users/walkers");
                Iterable<DataSnapshot> usersChildren = usersSnapshot.getChildren();

                int x = 0;
                for (DataSnapshot user : usersChildren) {
                    int ratingSum = 0;
                    float ratingsTotal = 0;
                    float ratingAvg = 0;
                    for(DataSnapshot child: user.child("rating").getChildren()){
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingsTotal++;
                    }

                    if(ratingsTotal != 0) {
                        ratingAvg = ratingSum / ratingsTotal;
                        ratingAvgArray[x] = ratingAvg;

                        if(ratingAvg >= 4) {
                            System.out.println(ratingAvg + "");
                            names[x] = user.child("name").getValue().toString();
                            userID[x] = user.getKey().toString();


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

                            textViewName.setText(names[i-1]);
//                            textViewRating.setText(ratingAvgArray[i-1] + "");
                            if(ratingAvgArray[i-1] != null) {
                                ratingBar.setRating(ratingAvgArray[i - 1]);
                            }

                            x++;
                        }
                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

//    public void sendSMSMessage() {
//
//            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
//            smsIntent.addCategory(Intent.CATEGORY_DEFAULT);
//            smsIntent.setType("vnd.android-dir/mms-sms");
//            smsIntent.setData(Uri.parse("sms:" + phones[i-1]));
//
//            startActivity(smsIntent);
//    }
}
