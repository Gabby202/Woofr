package com.example.gabby.dogapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ShowUsersActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private EditText nameEditText;
    private EditText addressEditText;
    private EditText phoneEditText;
    private EditText bioEditText;
    private Button finishButton;

    //private TextView registerTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

    }




    @Override
    public void onClick(View view) {
        if(view == finishButton) {
            finish();

            startActivity(new Intent(getApplicationContext(), HomeActivity.class));


        }
    }
}
