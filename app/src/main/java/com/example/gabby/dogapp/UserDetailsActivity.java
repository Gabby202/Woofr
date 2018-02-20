package com.example.gabby.dogapp;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class UserDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    //Firebase
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private EditText nameEditText;
    private EditText addressEditText;
    private EditText phoneEditText;
    private EditText bioEditText;
    private Button finishButton;
    private Button uploadImageButton;
    private Button chooseImageButton;
    private RadioButton isWalkerRadioButton, isNotWalkerRadioButton;
    private ImageView imageView;
    private RadioGroup groupRadioGroup;

    private Uri filePath;

    private final int PICK_IMAGE_REQUEST = 71;


    //private TextView registerTextView;
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
        uploadImageButton = (Button) findViewById(R.id.uploadImageButton);
        uploadImageButton.setOnClickListener(this);
        chooseImageButton = (Button) findViewById(R.id.chooseImageButton);
        chooseImageButton.setOnClickListener(this);
        isWalkerRadioButton = (RadioButton) findViewById(R.id.isWalkerRadioButton);
        isNotWalkerRadioButton = (RadioButton) findViewById(R.id.isNotWalkerRadioButton);

    }

    public void sendDetails() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String bio = bioEditText.getText().toString().trim();
        boolean isWalker;

        if(isWalkerRadioButton.isChecked()) {
            isWalker = true;
        } else {
            isWalker = false;
        }

        if(isWalkerRadioButton.isChecked() == false && isNotWalkerRadioButton.isChecked() == false) {
            Toast.makeText(this, "Please choose an option", Toast.LENGTH_LONG).show();
            return;
        }


        UserInformation userInformation = new UserInformation(name, phone, address, bio, isWalker);

        FirebaseUser user = firebaseAuth.getCurrentUser();

        //if statement here to differentiate between
        if(isWalker) {
            databaseReference.child("walkers/"+user.getUid()).setValue(userInformation);
        } else {
            databaseReference.child("non-walkers/"+user.getUid()).setValue(userInformation);

        }

        Toast.makeText(this, "Information Saved!", Toast.LENGTH_LONG).show();
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            /*try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }*/
        }
    }

    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            FirebaseUser user = firebaseAuth.getCurrentUser();

            StorageReference ref = storageReference.child("images/"+ user.getUid().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(UserDetailsActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UserDetailsActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    @Override
    public void onClick(View view) {
        if(view == finishButton) {
            sendDetails();
            finish();

            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));


        }

        else if(view == chooseImageButton){
            chooseImage();

        }

        else if(view == uploadImageButton){
            uploadImage();

        }
    }
}
