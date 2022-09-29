package com.instinct.c2cservice;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout drawer1;
    Toolbar toolbar1;

    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String profileImageUrlVal ,emailVal;
    CircleImageView profileimgheader;
    TextView emailheader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        toolbar1=findViewById(R.id.toolbar);
        toolbar1.setTitle("Home");
        setSupportActionBar(toolbar1);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("user");

        drawer1 = findViewById(R.id.user_drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer1, toolbar1,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer1.addDrawerListener(toggle);
        toggle.syncState();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new UserHomeFragment()).commit();
            navigationView.setCheckedItem(R.id.u_home);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mUser == null) {
            Intent i = new Intent(UserMainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        } else {
            databaseReference.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    updateNavheaader();
                    if (snapshot.exists()) {
                        if (snapshot.hasChild("ProfileImage")) {
                            profileImageUrlVal = snapshot.child("ProfileImage").getValue().toString();
                            Picasso.get().load(profileImageUrlVal).into(profileimgheader);

                        }
                        emailVal = snapshot.child("email").getValue().toString();
                        emailheader.setText(emailVal);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UserMainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

        @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.u_home:
                toolbar1.setTitle("Home");

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new UserHomeFragment()).commit();
                break;
            case R.id.u_profile:
                Intent intent = new Intent(UserMainActivity.this, ProfileUActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.u_myposts:
                Intent it = new Intent(UserMainActivity.this, UploadPost.class);
               startActivity(it);
                finish();
                break;
            case R.id.u_share:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT,"Check out this android app");
                i.putExtra(Intent.EXTRA_TEXT,"Your Appication Link is Here");
                startActivity(Intent.createChooser(i,"Share app via"));
                break;
            case R.id.u_logout:
                 mAuth.signOut();
                Intent in = new Intent(UserMainActivity.this, LoginActivity.class);
                startActivity(in);
                finish();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                break;
        }

        drawer1.closeDrawer(GravityCompat.START);
        return true;
    }

    public void exitOnBack(){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage("Do You want to exit the application")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.exit(0);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Nothing
                    }
                }).show();

    }

    @Override
    public void onBackPressed() {
        if (drawer1.isDrawerOpen(GravityCompat.START)) {
            drawer1.closeDrawer(GravityCompat.START);
        } else {
            exitOnBack();
        }
    }

    public void updateNavheaader(){
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        emailheader = headerView.findViewById(R.id.txtuemail);
        profileimgheader = headerView.findViewById(R.id.uprofile_image);
    }


}