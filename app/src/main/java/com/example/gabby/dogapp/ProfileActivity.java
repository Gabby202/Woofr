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

import org.w3c.dom.Text;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private TextView userEmailTextView;
    private Button logoutButton;
    private EditText usernameEditText;
    private  EditText addressEditText;
    private  Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        FirebaseUser user = firebaseAuth.getCurrentUser();

        userEmailTextView = (TextView) findViewById(R.id.userEmailTextView);
        userEmailTextView.setText("Welcome " + user.getEmail());
        logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == logoutButton) {
                    firebaseAuth.signOut();
                    finish();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference();
        usernameEditText = (EditText) findViewById(R.id.userNameEditText);
        addressEditText = (EditText) findViewById(R.id.addressEditText);
        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == saveButton) {
                    saveUserInformation();
                }
            }
        });
    }

    private void saveUserInformation() {
        String username = usernameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        UserInformation userInformation = new UserInformation(username, address);
        FirebaseUser user = firebaseAuth.getCurrentUser();
        databaseReference.child(user.getUid()).setValue(userInformation);
        Toast.makeText(this, "Information Saved!", Toast.LENGTH_LONG).show();
    }
}
