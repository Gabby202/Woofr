package com.example.gabby.dogapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity {

    /*==================== Declare local variables to reference layout items ==============*/
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private EditText usernameEditText;
    private  EditText addressEditText;
    private  Button saveButton;

    /*==================== "Main" method ====================================*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        //get firebase authentication object so we can use the methods
        //get firebase database
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        //if user is not logged in, go back to login activity
        if(firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        //reference current logged in user and assign to variable "user"
        FirebaseUser user = firebaseAuth.getCurrentUser();

        /*======================= reference layout items with local variabbles =================*/
        usernameEditText = (EditText) findViewById(R.id.userNameEditText);
        addressEditText = (EditText) findViewById(R.id.addressEditText);
        saveButton = (Button) findViewById(R.id.saveButton);

        //set listener on savebutton to call method
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == saveButton) {
                    saveUserInformation();
                    finish();
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                }
            }
        });
    }

    //interacts with database
    private void saveUserInformation() {
        //sets local variables to get info typed in text boxes
        String username = usernameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        //reference java class i made which will store information for the user
        UserInformation userInformation = new UserInformation(username, address);
        //get current user logged in
        FirebaseUser user = firebaseAuth.getCurrentUser();
        //get unique id for user, and set the variables inside the object made above as children to it
        databaseReference.child(user.getUid()).setValue(userInformation);
        //success popup
        Toast.makeText(this, "Information Saved!", Toast.LENGTH_LONG).show();

        
    }
}
