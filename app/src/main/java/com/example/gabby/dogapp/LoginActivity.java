package com.example.gabby.dogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.widget.Toast.LENGTH_LONG;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button loginButton;
    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView registerTextView;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        //checks if user is already logged in
        if(firebaseAuth.getCurrentUser() != null) {
//          start profile activity
//            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child("walkers").child(userId);
//            ref.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.exists()) {
//                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//                        intent.putExtra("ownerOrWalker", "walker");
//                        System.out.println("LOGGED IN AS WALKER =================== ");
//                        startActivity(intent);
//
//                    } else {
//                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//                        intent.putExtra("ownerOrWalker", "owner");
//                        System.out.println("LOGGED IN AS OWNER =================== ");
//                        startActivity(intent);
//                    }
//                }
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });

            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();

        }
        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        registerTextView = (TextView) findViewById(R.id.registerTextView);
        progressDialog = new ProgressDialog(this);
        registerTextView.setOnClickListener(this);


    }

    private void userLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

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
        progressDialog.setMessage("Logging in");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        progressDialog.dismiss();

                        if(task.isSuccessful()) {
                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            System.out.println("USER: " + userId + " IS THE CURRENT USER LOGGIN IN ===================");
//                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child("walkers").child(userId);
//                            ref.addValueEventListener(new ValueEventListener() {
//                                @Override
//                                //start profile activity
//                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                    if (dataSnapshot.exists()) {
//                                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//                                        intent.putExtra("ownerOrWalker", "walker");
//                                        System.out.println("LOGGING IN AS WALKER =================== ");
//                                        startActivity(intent);
//
//                                    } else {
//                                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//                                        intent.putExtra("ownerOrWalker", "owner");
//                                        System.out.println("LOGGING IN AS OWNER =================== ");
//                                        startActivity(intent);
//                                    }
//                                }
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//
//                                }
//                            });
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));

                            finish();

                        }else{
                            return;
                        }
                    }
                });

    }

    public void onClick(View view) {
        if(view == loginButton) {

            userLogin();
        }

        if(view == registerTextView) {

            finish(); //closes activity
            //changes to RegisterActivity
            startActivity(new Intent(this, RegisterActivity.class));
        }
    }
}
