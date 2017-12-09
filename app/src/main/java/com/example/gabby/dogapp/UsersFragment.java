package com.example.gabby.dogapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class UsersFragment extends android.support.v4.app.Fragment {
    TextView textView;
    public UsersFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        String[] user =  {"Alex", "Maria", "Sophie", "Laura", "Stacey", "Noelle"};
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        textView = (TextView) view.findViewById(R.id.textView2);
        Bundle bundle = getArguments();
        String message = Integer.toString(bundle.getInt("count"));
        int i = bundle.getInt("count");
        textView.setText("User" + message + " " + user[i]);
        return view;
    }


}
