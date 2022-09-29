package com.instinct.c2cservice;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

public class SignUpActivity extends AppCompatActivity {

    TextView txtSignIn;
    private TextInputEditText fullname, email, password, confpass, phone;
    private MaterialButton signup;

    FirebaseAuth mAuth;
    ProgressDialog mLoadingBar;

    String type[] = {"User", "Service Provider"};

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    AutoCompleteTextView userType;
    ArrayAdapter<String> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        fullname = findViewById(R.id.fullname);
        password = findViewById(R.id.edtSignUpPassword);
        confpass = findViewById(R.id.edtSignUpConfirmPassword);
        phone = findViewById(R.id.mobile);
        email = findViewById(R.id.email);
        signup = findViewById(R.id.btnSignUp);
        txtSignIn = findViewById(R.id.txtSignIn);
        userType = findViewById(R.id.uesrType);


        items = new ArrayAdapter<String>(this, R.layout.list_item, type);
        userType.setAdapter(items);
        userType.setDropDownBackgroundResource(R.color.blue_color);

        mAuth = FirebaseAuth.getInstance();
        mLoadingBar = new ProgressDialog(this);
        txtSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final String e = email.getText().toString();
                final String pass = password.getText().toString();
                final String name = fullname.getText().toString();
                final String conf = confpass.getText().toString();
                final String phoneno = phone.getText().toString();
                final String type = userType.getText().toString();


                if (name.isEmpty() || pass.isEmpty() || conf.isEmpty() || phoneno.isEmpty() || e.isEmpty() || type.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill all detail...", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                    Toast.makeText(SignUpActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                } else if (phoneno.length() < 10) {
                    Toast.makeText(SignUpActivity.this, "Check Phone Number ", Toast.LENGTH_SHORT).show();
                } else if (pass.length() < 8) {
                    Toast.makeText(SignUpActivity.this, "Password too short", Toast.LENGTH_SHORT).show();
                } else if (!pass.equals(conf)) {
                    Toast.makeText(SignUpActivity.this, "Password do not matched", Toast.LENGTH_SHORT).show();
                }
                else {

                    mLoadingBar.setTitle("Registration");
                    mLoadingBar.setMessage("Please wait...");
                    mLoadingBar.setCanceledOnTouchOutside(false);
                    mLoadingBar.show();


                    mAuth.createUserWithEmailAndPassword(e, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                UserData info = new UserData(name, e, type, phoneno);
                                databaseReference.child("user").child(mAuth.getUid()).setValue(info).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        mLoadingBar.dismiss();
                                        Toast.makeText(SignUpActivity.this, "Registration is Successful", Toast.LENGTH_SHORT).show();
                                        Intent i = new Intent(SignUpActivity.this, LoginActivity.class);
                                        startActivity(i);
                                        finish();

                                    }
                                });


                            } else {
                                mLoadingBar.dismiss();
                                Toast.makeText(SignUpActivity.this, "Registration is Failed", Toast.LENGTH_SHORT).show();
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


    private boolean isConnected(SignUpActivity signUpActivity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) signUpActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

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
        AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this)
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
