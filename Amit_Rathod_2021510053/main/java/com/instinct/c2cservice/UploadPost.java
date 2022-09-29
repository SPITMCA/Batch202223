package com.instinct.c2cservice;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class UploadPost extends AppCompatActivity {

    private Button btnSend;
    private EditText edtPostdes;
    String fullname,phoneno;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    private DatabaseReference mDatabaseRef,userref;
    ProgressDialog mLoadingBar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userpost);

        btnSend = findViewById(R.id.btn_send);
        edtPostdes = findViewById(R.id.edt_postdesc);
        FirebaseAuth mAuth;
        FirebaseUser mUser;
        mLoadingBar=new ProgressDialog( this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("post");
        userref = FirebaseDatabase.getInstance().getReference().child("user");


        userref.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){

                         fullname = snapshot.child("fullname").getValue().toString();
                        phoneno = snapshot.child("phoneno").getValue().toString();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadPost.this, "Something went wrong", Toast.LENGTH_SHORT).show();

            }
        });




        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String description = edtPostdes.getText().toString();

                if(description.isEmpty()){
                    Toast.makeText(UploadPost.this, "Add Description ", Toast.LENGTH_SHORT).show();

                }
                 else {
                    mLoadingBar.setTitle("Adding Post");
                    mLoadingBar.setCanceledOnTouchOutside(false);
                    mLoadingBar.show();

                    Date date=new Date();
                    SimpleDateFormat formatter=new SimpleDateFormat("dd-M-yyy hh:mm:ss");
                    String strDate=formatter.format(date);

                    HashMap h = new HashMap();
                    h.put("FullName",fullname);
                    h.put("Phone",phoneno);
                    h.put("PostDesc",description);


                    mDatabaseRef.child(mUser.getUid()+strDate).updateChildren(h).addOnSuccessListener(new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            mLoadingBar.dismiss();
                            Toast.makeText(UploadPost.this, "Post added ", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mLoadingBar.dismiss();
                            Toast.makeText(UploadPost.this,"SomThing went wrong Try Again"+ e.toString(), Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }
        });

    }

    @Override
    public void onBackPressed() {

        Intent i = new Intent(UploadPost.this, UserMainActivity.class);
        startActivity(i);
        finish();

    }
    }

