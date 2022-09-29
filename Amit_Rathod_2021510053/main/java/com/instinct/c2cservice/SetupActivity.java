package com.instinct.c2cservice;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 101;
    String stype[] = {"Electrician","Interior Designer","Maintaince","Decorator"};
     static boolean skipclick = false;
    DatabaseReference databaseReference;
     FirebaseAuth mAuth;
     FirebaseUser mUser;
     StorageReference storageRef;
    ProgressDialog mLoadingBar;

    EditText  shopname,shopaddress,shopcity,experience;
    CircleImageView profileimg;
    Button upload,skip;
    AutoCompleteTextView service;
    ArrayAdapter<String> items;
    Uri imageUri;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setupaccount);

        profileimg = findViewById(R.id.profile_image);
        shopname = findViewById(R.id.editTextShopName);
        shopaddress  = findViewById(R.id.editTextShopAddress);
        shopcity  = findViewById(R.id.editTextShopCity);
        experience  = findViewById(R.id.editTextExperience);
        upload  = findViewById(R.id.btnUup);
        skip  = findViewById(R.id.btnSkip);
        service = findViewById(R.id.autoCompleteTextView);

        items = new ArrayAdapter<String>(SetupActivity.this,R.layout.list_item,stype);
        service.setThreshold(1);
        service.setDropDownBackgroundResource(R.color.blue_color);
        service.setAdapter(items);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("user");
        storageRef = FirebaseStorage.getInstance().getReference().child("Profileimage");

        mLoadingBar = new ProgressDialog(SetupActivity.this);

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                skipclick=true;
                Intent intent = new Intent(SetupActivity.this, SPmainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String shopName = shopname.getText().toString();
                String shopAddress = shopaddress.getText().toString();
                String shopCity = shopcity.getText().toString();
                String exp = experience.getText().toString();
                String serv = service.getText().toString();

                if (shopName.isEmpty() || shopAddress.isEmpty() || shopCity.isEmpty() || exp.isEmpty() || serv.isEmpty()) {
                    Toast.makeText(SetupActivity.this, "Please fill all detail...", Toast.LENGTH_SHORT).show();
                } else if (imageUri==null) {
                    Toast.makeText(SetupActivity.this, "Please select profile image", Toast.LENGTH_SHORT).show();
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
                                        h.put("ShopName",shopName);
                                        h.put("ShopAddress",shopAddress);
                                        h.put("ShopCity",shopCity);
                                        h.put("Experience",exp);
                                        h.put("Service",serv);
                                        h.put("ProfileImage",uri.toString());


                                        databaseReference.child(mUser.getUid()).updateChildren(h).addOnSuccessListener(new OnSuccessListener() {
                                            @Override
                                            public void onSuccess(Object o) {
                                                Intent intent = new Intent(SetupActivity.this, SPmainActivity.class);
                                                startActivity(intent);
                                                mLoadingBar.dismiss();
                                                Toast.makeText(SetupActivity.this, "Setup Profile Completed", Toast.LENGTH_SHORT).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                mLoadingBar.dismiss();
                                                Toast.makeText(SetupActivity.this,"Setup Failed Try Again"+ e.toString(), Toast.LENGTH_SHORT).show();

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

}
