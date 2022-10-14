package com.instinct.c2cservice;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class SplashScreen extends AppCompatActivity{
    FirebaseAuth mAuth;
   FirebaseUser mUser;
   DatabaseReference databaseReference;
    SetupActivity setup = new SetupActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if(!isConnected(this)){
            showCustomDialog();
        }
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();


        Runnable runnable = new Runnable() {
                         @Override
                         public void run() {

                             if(mUser!=null){
                                 String uid = mUser.getUid();

                                 databaseReference.child("user").addValueEventListener(new ValueEventListener() {
                                     @Override
                                     public void onDataChange(@NonNull DataSnapshot snapshot) {
                                         if(snapshot.exists()) {

                                             String user_type = snapshot.child(uid).child("usertype").getValue(String.class);
                                             boolean check = snapshot.child(uid).hasChild("ProfileImage");
                                             if (user_type.equals("User")) {


                                                 Intent i = new Intent(SplashScreen.this, UserMainActivity.class);
                                                 startActivity(i);
                                                 finish();
                                             } else {

                                                 if (check) {

                                                     Intent i = new Intent(SplashScreen.this, SPmainActivity.class);
                                                     startActivity(i);
                                                     finish();
                                                 } else {

                                                     Intent i = new Intent(SplashScreen.this, SetupActivity.class);
                                                     startActivity(i);
                                                     finish();
                                                 }

                                             }
                                         }
                                         else{
                                             Intent i = new Intent(SplashScreen.this, LoginActivity.class);
                                             startActivity(i);
                                             finish();
                                         }
                                     }

                                     @Override
                                     public void onCancelled(@NonNull DatabaseError error) {

                                     }
                                 });
                             }
                             else {

                                 Intent i = new Intent(SplashScreen.this,LoginActivity.class);
                                 startActivity(i);
                                 finish();
                             }
                         }
                     };


                     Handler handler = new Handler();
                     handler.postDelayed(runnable,2000);


    }

    private boolean isConnected(SplashScreen splashScreen) {
        ConnectivityManager connectivityManager = (ConnectivityManager) splashScreen.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifi =connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobdata =connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if((wifi !=null && wifi.isConnected())||(mobdata !=null && mobdata.isConnected())){
            return true;
        }
        else{
            return false;
        }

    }


    private void showCustomDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(SplashScreen.this)
                .setMessage("Please connect to internet to proceed further")

                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.exit(0);

                    }
                }).show();

    }


}
