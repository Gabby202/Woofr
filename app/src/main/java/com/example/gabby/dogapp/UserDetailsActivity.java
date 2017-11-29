package com.example.gabby.dogapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private EditText nameEditText;
    private EditText addressEditText;
    private EditText phoneEditText;
    private EditText bioEditText;
    private Button finishButton;
    private TextView registerTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        nameEditText = (EditText) findViewById(R.id.nameEditText);
        addressEditText = (EditText) findViewById(R.id.addressEditText);
        phoneEditText = (EditText) findViewById(R.id.phoneEditText);
        bioEditText = (EditText) findViewById(R.id.bioEditText);
        finishButton = (Button) findViewById(R.id.finishButton);
        finishButton.setOnClickListener(this);
    }

    public void sendDetails() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String bio = bioEditText.getText().toString().trim();

        UserInformation userInformation = new UserInformation(name, phone, address, bio);

        FirebaseUser user = firebaseAuth.getCurrentUser();
        databaseReference.child(user.getUid()).setValue(userInformation);
        Toast.makeText(this, "Information Saved!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        if(view == finishButton) {
            sendDetails();
            finish();

            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        }
    }
}
