package com.instinct.c2cservice;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    TextView txtSignUp;
    private TextInputEditText email,password;
    private MaterialButton login;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    ProgressDialog mLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.edtSignInEmail);
        password = findViewById(R.id.edtSignInPassword);
        login = findViewById(R.id.btnSignIn);
        txtSignUp = findViewById(R.id.txtSignUp);

        mAuth = FirebaseAuth.getInstance();
        mLoadingBar = new ProgressDialog(this);

        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String e = email.getText().toString();
                final String pass = password.getText().toString();

                if (e.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Fill all details...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mLoadingBar.setTitle("Login");
                    mLoadingBar.setMessage("Please wait...");
                    mLoadingBar.setCanceledOnTouchOutside(false);
                    mLoadingBar.show();

                    mAuth.signInWithEmailAndPassword(e,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                mLoadingBar.dismiss();
                                mUser = mAuth.getCurrentUser();

                                Toast.makeText(LoginActivity.this, "Login is Successful", Toast.LENGTH_SHORT).show();
                                String uid = mUser.getUid();

                                databaseReference.child("user").child(uid).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        String user_type = snapshot.child("usertype").getValue(String.class);
                                        boolean check = snapshot.hasChild("ProfileImage");

                                        if (user_type.equals("User")) {


                                                Intent i = new Intent(LoginActivity.this, UserMainActivity.class);
                                                startActivity(i);
                                                finish();
                                        } else {

                                            if(check){

                                                Intent i = new Intent(LoginActivity.this, SPmainActivity.class);
                                                startActivity(i);
                                                finish();
                                            }
                                            else {

                                                Intent i = new Intent(LoginActivity.this, SetupActivity.class);
                                                startActivity(i);
                                                finish();
                                            }


                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                            else {
                                mLoadingBar.dismiss();
                                Toast.makeText(LoginActivity.this, "Login Failed...Check your credentials", Toast.LENGTH_SHORT).show();
                            }
                        }

                    });


                }
            }
        });
    }
    @Override
    protected void onStart() {

        if(!isConnected(this)){
            showCustomDialog();
        }
        super.onStart();
    }


    private boolean isConnected(LoginActivity loginActivity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) loginActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

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
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this)
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