package com.instinct.c2cservice;

import android.content.Intent;
import android.net.Uri;
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
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileUActivity extends AppCompatActivity {

    CircleImageView prof_imgS;
    ImageView prof_imgL;
    TextView username,add,name,email,phone;

    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String prof_imgSVal ,prof_imgLVal;
    RelativeLayout setup_detail;

    private TextView noupload;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profileu);

        name = findViewById(R.id.Name);
        username = findViewById(R.id.txtusername);
        add = findViewById(R.id.txtaddress);
        email = findViewById(R.id.txtemailcontact);
        phone = findViewById(R.id.txtno);
        prof_imgL = findViewById(R.id.imageprofL);
        prof_imgS = findViewById(R.id.imgprof);
        setup_detail = findViewById(R.id.layout1);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("user");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

//
//        phone.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String phoneno  = phone.getText().toString();
//                if(phoneno.isEmpty()){
//                    Toast.makeText(ProfileUActivity.this, "no num", Toast.LENGTH_SHORT).show();
//
//                }
//                else{
//                    String s= "+91" + phoneno;
//                Intent i = new Intent(Intent.ACTION_CALL);
//                i.setData(Uri.parse(s));
//                startActivity(i);
//
//                }
//            }
//        });
//

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
                            String usernmVal = snapshot.child("UserName").getValue().toString();
                            String shopaddVal = snapshot.child("Address").getValue().toString();
                            String phoneVal = snapshot.child("phoneno").getValue().toString();
                            String emailVal = snapshot.child("email").getValue().toString();
                            email.setText(emailVal);
                            name.setText(nameVal);
                            username.setText(usernmVal);
                            add.setText(shopaddVal);
                            phone.setText(phoneVal);
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
                    Toast.makeText(ProfileUActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

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
            Intent intent = new Intent(ProfileUActivity.this, UserSetupActivity.class);
            startActivity(intent);
            finish();
        }

        return true;
    }

    @Override
    public void onBackPressed() {

        Intent i = new Intent(ProfileUActivity.this, UserMainActivity.class);
        startActivity(i);
        finish();

        super.onBackPressed();
    }


}
