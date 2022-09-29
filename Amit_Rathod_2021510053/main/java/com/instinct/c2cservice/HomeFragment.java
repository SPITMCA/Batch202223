package com.instinct.c2cservice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private UserPostAdapter mAdapter;
    private ProgressBar mProgressCircle;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    private TextView noupload;
    private DatabaseReference mDatabaseRef,type;
    private List<UserPost> mUploads;
    public String key;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mRecyclerView = (rootView).findViewById(R.id.Srecycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mProgressCircle = (rootView).findViewById(R.id.progress_circle);
        mUploads = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("post");

        if(mUser != null){
            mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                                UserPost post = postSnapshot.getValue(UserPost.class);
                                mUploads.add(post);

                        }

                        mAdapter = new UserPostAdapter(getContext(), mUploads);

                        mRecyclerView.setAdapter(mAdapter);
                        mProgressCircle.setVisibility(View.GONE);
                    }
                    else{

                        mProgressCircle.setVisibility(View.INVISIBLE);
                    }
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    mProgressCircle.setVisibility(View.INVISIBLE);
                }
            });

        }




        return  rootView;
    }





}
