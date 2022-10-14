package com.instinct.c2cservice;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileAcivity extends AppCompatActivity {

    CircleImageView prof_imgS;
    ImageView prof_imgL;
    TextView shopnm,shopadd,name,email,phone,exp;
    String prof_imgSVal ,prof_imgLVal;
    RelativeLayout setup_detail;
    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;
    private ProgressBar mProgressCircle;
    private DatabaseReference mDatabaseRef= FirebaseDatabase.getInstance().getReference().child("user");;

    private List<Upload> mUploads;

    private TextView noupload;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");
    String sname,uid;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);

        noupload = findViewById(R.id.nouploads);
        noupload.setVisibility(View.GONE);

        name = findViewById(R.id.Name);
        shopnm = findViewById(R.id.txtshopnm);
        shopadd = findViewById(R.id.txtaddress);
        email = findViewById(R.id.txtemailcontact);
        phone = findViewById(R.id.txtno);
        prof_imgL = findViewById(R.id.imageprofL);
        prof_imgS = findViewById(R.id.imgprof);
        exp= findViewById(R.id.txtexp);
        setup_detail = findViewById(R.id.layout1);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mProgressCircle = findViewById(R.id.progress_circle);
        mUploads = new ArrayList<>();


        Intent i = getIntent() ;
        sname = i.getStringExtra("name");
        getUid();


    }

    public void getUid() {

        databaseReference.orderByChild("fullname").equalTo(sname).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                    uid = postSnapshot.getKey();

                    if (postSnapshot.hasChild("ProfileImage")) {


                        prof_imgSVal = postSnapshot.child("ProfileImage").getValue().toString();
                        prof_imgLVal = postSnapshot.child("ProfileImage").getValue().toString();
                        Picasso.get().load(prof_imgSVal).into(prof_imgS);
                        Picasso.get().load(prof_imgLVal).into(prof_imgL);
                        String shopnmVal = postSnapshot.child("ShopName").getValue().toString();
                        String shopaddVal = postSnapshot.child("ShopAddress").getValue().toString();
                        String phoneVal = postSnapshot.child("phoneno").getValue().toString();
                        String emailVal = postSnapshot.child("email").getValue().toString();
                        String expVal = postSnapshot.child("Experience").getValue().toString();
                        email.setText(emailVal);
                        name.setText(sname);
                        shopnm.setText(shopnmVal);
                        shopadd.setText(shopaddVal);
                        phone.setText(phoneVal);
                        exp.setText(expVal + " Years");
                    }
                    else {
                        setup_detail.setVisibility(View.GONE);
                        String phoneVal = snapshot.child("phoneno").getValue().toString();
                        String emailVal = snapshot.child("email").getValue().toString();
                        email.setText(emailVal);
                        name.setText(sname);
                        phone.setText(phoneVal);

                    }
                   // if (postSnapshot.hasChild("uploads")) {
                        mDatabaseRef.child(uid).child("upload").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChildren()) {
                                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                        Upload upload = postSnapshot.getValue(Upload.class);
                                        mUploads.add(upload);
                                    }

                                    mAdapter = new ImageAdapter(UserProfileAcivity.this, mUploads);

                                    mRecyclerView.setAdapter(mAdapter);
                                    mProgressCircle.setVisibility(View.INVISIBLE);
                                } else {
                                    mProgressCircle.setVisibility(View.INVISIBLE);
                                    noupload.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(UserProfileAcivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                mProgressCircle.setVisibility(View.INVISIBLE);
                            }

                        });
//                }
//                    else{
//                        mProgressCircle.setVisibility(View.INVISIBLE);
//                        noupload.setVisibility(View.VISIBLE);
//
//                    }
                            }

                        }




            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }



}