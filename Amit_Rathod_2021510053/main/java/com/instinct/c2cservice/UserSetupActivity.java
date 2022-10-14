package com.instinct.c2cservice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSetupActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 101;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    StorageReference storageRef;
    ProgressDialog mLoadingBar;

    EditText username,address,city;
    CircleImageView profileimg;
    Button upload,skip;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usersetupacc);


        profileimg = findViewById(R.id.profile_image);
        username = findViewById(R.id.editTextUserName);
        address  = findViewById(R.id.editTextUserAddress);
        city  = findViewById(R.id.editTextUserCity);
        upload  = findViewById(R.id.btnUup);
        skip  = findViewById(R.id.btnSkip);


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("user");
        storageRef = FirebaseStorage.getInstance().getReference().child("Profileimage");

        mLoadingBar = new ProgressDialog(UserSetupActivity.this);

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserSetupActivity.this, UserMainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userName = username.getText().toString();
                String userAddress = address.getText().toString();
                String userCity = city.getText().toString();

                if (userName.isEmpty() || userAddress.isEmpty() || userCity.isEmpty()) {
                    Toast.makeText(UserSetupActivity.this, "Please fill all detail...", Toast.LENGTH_SHORT).show();
                } else if (imageUri==null) {
                    Toast.makeText(UserSetupActivity.this, "Please select profile image", Toast.LENGTH_SHORT).show();
                }
                else{

                    mLoadingBar.setTitle("Adding Profile");
                    mLoadingBar.setMessage("Please wait...");
                    mLoadingBar.setCanceledOnTouchOutside(false);
                    mLoadingBar.show();


                    storageRef.child(mUser.getUid()).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                storageRef.child(mUser.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        HashMap h = new HashMap();
                                        h.put("UserName",userName);
                                        h.put("Address",userAddress);
                                        h.put("City",userCity);
                                        h.put("ProfileImage",uri.toString());


                                        databaseReference.child(mUser.getUid()).updateChildren(h).addOnSuccessListener(new OnSuccessListener() {
                                            @Override
                                            public void onSuccess(Object o) {
                                                Intent intent = new Intent(UserSetupActivity.this, UserMainActivity.class);
                                                startActivity(intent);
                                                mLoadingBar.dismiss();
                                                Toast.makeText(UserSetupActivity.this, "Setup Profile Completed", Toast.LENGTH_SHORT).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                mLoadingBar.dismiss();
                                                Toast.makeText(UserSetupActivity.this,"Setup Failed Try Again"+ e.toString(), Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        profileimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,REQUEST_CODE);
            }
        });


    }


    @Override
    public void  onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==REQUEST_CODE && resultCode== Activity.RESULT_OK){

            imageUri=data.getData();
            profileimg.setImageURI(imageUri);
        }

    };

    @Override
    public void onBackPressed() {

        Intent i = new Intent(UserSetupActivity.this, UserMainActivity.class);
        startActivity(i);
        finish();

        super.onBackPressed();
    }
}
