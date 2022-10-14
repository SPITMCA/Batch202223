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

public class SPmainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout drawer;
    Toolbar toolbar;
private int count=0;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String profileImageUrlVal ,emailVal;
    CircleImageView profileimgheader;
    TextView emailheader;


    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spmain);


         toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Home");

        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("user");

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mUser==null){
            Intent i = new Intent(SPmainActivity.this, SPmainActivity.class);
            startActivity(i);
            finish();
        }
        else{
            databaseReference.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    updateNavheaader();
                    if(snapshot.exists()){
                        if(snapshot.hasChild("ProfileImage")) {
                            profileImageUrlVal = snapshot.child("ProfileImage").getValue().toString();
                            Picasso.get().load(profileImageUrlVal).into(profileimgheader);

                        }
                        emailVal = snapshot.child("email").getValue().toString();
                        emailheader.setText(emailVal);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SPmainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                }
            });
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                toolbar.setTitle("Home");

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).commit();
                break;
            case R.id.nav_profile:
                toolbar.setTitle("Profile");

                intent = new Intent(SPmainActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_upload:
                 intent = new Intent(SPmainActivity.this, UploadImages.class);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_share:
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Check out this android app");
                intent.putExtra(Intent.EXTRA_TEXT,"Your Appication Link is Here");
                startActivity(Intent.createChooser(intent,"Share app via"));
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                intent  = new Intent(SPmainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
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

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else  {
            exitOnBack();
        }
    }

    public void updateNavheaader(){
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        emailheader = headerView.findViewById(R.id.txtemail);
        profileimgheader = headerView.findViewById(R.id.profile_image);
    }
}
