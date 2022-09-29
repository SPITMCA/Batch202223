package com.instinct.c2cservice;

import android.app.SearchableInfo;
import android.content.Context;
import android.content.SharedPreferences;
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

public class UserHomeFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private ServicePostAdapter mAdapter;
   SearchView inputsearch;
    private ProgressBar mProgressCircle;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    private TextView noupload;
    private DatabaseReference mDatabaseRef,type;
    private List<Server_Post> mUploads;
    public String key;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_userhome, container, false);
        mRecyclerView = (rootView).findViewById(R.id.Urecycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mProgressCircle = (rootView).findViewById(R.id.progress_circle);
        inputsearch = (rootView).findViewById(R.id.search);

        EditText inputsearchET = (rootView).findViewById(androidx.appcompat.R.id.search_src_text);
        inputsearchET.setTextColor(getResources().getColor(R.color.blue_color));
        inputsearchET.setHintTextColor(getResources().getColor(R.color.blue_color));

        inputsearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });

        mUploads = new ArrayList<>();


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("user");

        if(mUser != null){
            mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            key = postSnapshot.getKey();
                            String user_type = dataSnapshot.child(key).child("usertype").getValue(String.class);
                            if(user_type.equals("Service Provider")) {

                                Server_Post post = postSnapshot.getValue(Server_Post.class);
                                mUploads.add(post);
                            }
                        }

                        mAdapter = new ServicePostAdapter(getContext(), mUploads);

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



        return rootView;

    }

}

