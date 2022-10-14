package com.instinct.c2cservice;

import android.content.Intent;
import android.os.Bundle;
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

public class ProfileActivity  extends AppCompatActivity {

    CircleImageView prof_imgS;
    ImageView prof_imgL;
    TextView shopnm,shopadd,name,email,phone,exp;
    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;
    private ProgressBar mProgressCircle;
    private DatabaseReference mDatabaseRef;

    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String prof_imgSVal ,prof_imgLVal;
    RelativeLayout setup_detail;
    private List<Upload> mUploads;

    private TextView noupload;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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

        databaseReference = FirebaseDatabase.getInstance().getReference().child("user");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("user").child(mUser.getUid()).child("upload");


        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Upload upload = postSnapshot.getValue(Upload.class);
                        mUploads.add(upload);
                    }

                    mAdapter = new ImageAdapter(ProfileActivity.this, mUploads);

                    mRecyclerView.setAdapter(mAdapter);
                    mProgressCircle.setVisibility(View.INVISIBLE);
                }
                else{
                    mProgressCircle.setVisibility(View.INVISIBLE);
                    noupload.setVisibility(View.VISIBLE);
                }
            }
                @Override
                public void onCancelled (DatabaseError databaseError){
                    Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    mProgressCircle.setVisibility(View.INVISIBLE);
                }

        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mUser!=null){

            databaseReference.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(snapshot.exists()){
                        if(snapshot.hasChild("ProfileImage")) {
                            prof_imgSVal = snapshot.child("ProfileImage").getValue().toString();
                            prof_imgLVal = snapshot.child("ProfileImage").getValue().toString();
                            Picasso.get().load(prof_imgSVal).into(prof_imgS);
                            Picasso.get().load(prof_imgLVal).into(prof_imgL);


                            String nameVal = snapshot.child("fullname").getValue().toString();
                            String shopnmVal = snapshot.child("ShopName").getValue().toString();
                            String shopaddVal = snapshot.child("ShopAddress").getValue().toString();
                            String phoneVal = snapshot.child("phoneno").getValue().toString();
                            String emailVal = snapshot.child("email").getValue().toString();
                            String expVal = snapshot.child("Experience").getValue().toString();
                            email.setText(emailVal);
                            name.setText(nameVal);
                            shopnm.setText(shopnmVal);
                            shopadd.setText(shopaddVal);
                            phone.setText(phoneVal);
                            exp.setText(expVal + " Years");
                        }
                        else{
                            setup_detail.setVisibility(View.GONE);
                            String nameVal = snapshot.child("fullname").getValue().toString();
                            String phoneVal = snapshot.child("phoneno").getValue().toString();
                            String emailVal = snapshot.child("email").getValue().toString();
                            email.setText(emailVal);
                            name.setText(nameVal);
                            phone.setText(phoneVal);

                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.service_optionmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemid = item.getItemId();

        if(itemid ==R.id.action_account){
            Intent intent = new Intent(ProfileActivity.this, SetupActivity.class);
            startActivity(intent);
            finish();
          }

        return true;
    }

    @Override
    public void onBackPressed() {

        Intent i = new Intent(ProfileActivity.this, SPmainActivity.class);
        startActivity(i);
        finish();

        super.onBackPressed();
    }
}