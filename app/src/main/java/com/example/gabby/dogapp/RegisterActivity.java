package com.example.gabby.dogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    /*=========== Declare local variables that will reference layout items =========*/
    private Button registerButton;
    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView loginTextView;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    /*============= "Main" method ================================*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //get firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();

        //if user has logged in before and not logged out, go straight to profile page
        if(firebaseAuth.getCurrentUser() != null) {
            //start profile activity
            //finish();
            startActivity(new Intent(getApplicationContext(), DisplayProfileActivity.class));
            //just testing new activity, remove when done
            //startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        /*================== Assign local variables to reference layout items ===============*/
        progressDialog = new ProgressDialog(this);
        registerButton = (Button) findViewById(R.id.registerButton);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        loginTextView = (TextView) findViewById(R.id.loginTextView);

        //call register user function when button is pressed
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == registerButton) {
                    registerUser();
                }
            }
        });

        //switch to login activity if user clicks bottom text field asking if they have account
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == loginTextView) {
                    //will open login activity
                    finish();
                   startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }

            }
        });


    }

    //on click method, i have only used anonymous onclick methods so far
    //see above loginTextView button using anon onClick method
    //can use this one if you want them all in the same place.
    public void onClick(View view) {

    }

    //send user info entered in text boxes to the firebase authentication database
    private void registerUser() {

        //create local variables to set text to
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        //shows popup (Toast) if no info is entered
        if(TextUtils.isEmpty(email)) {
            //email is empty
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            //stopping the function executing further
            return;
        }

        if(TextUtils.isEmpty(password)) {
            //password is empty
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        //if validations are ok
        //show progressbar
        progressDialog.setMessage("Registering User...");
        progressDialog.show();

        //firebase auth method to send email and password to database (pre made method)
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        //if successful interaction with database, close this activity and start profile
                        if(task.isSuccessful()) {
                            //start profile activity
                            finish();
                            startActivity(new Intent(getApplicationContext(), UserDetailsActivity.class));

                        }else {

                            //else display popup
                            Toast.makeText(RegisterActivity.this, "Could not register... Please try again", Toast.LENGTH_SHORT).show();

                        }

                        //close spinning progress thing
                        progressDialog.dismiss();
                    }
                });


    }

}
