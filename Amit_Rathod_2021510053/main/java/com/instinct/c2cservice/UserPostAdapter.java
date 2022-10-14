package com.instinct.c2cservice;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;



public class UserPostAdapter extends RecyclerView.Adapter<UserPostAdapter.UserViewHolder>  {

    private Context mContext;
    private List<UserPost> mUploads;
    public UserPost uploadCurrent;
    public UserPostAdapter(Context context, List<UserPost> uploads) {
        mContext = context;
        mUploads = uploads;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.user_singlepost, parent, false);
        return new UserViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        uploadCurrent = mUploads.get(position);

        holder.nameU.setText(uploadCurrent.getFullName());
        holder.postU.setText(uploadCurrent.getPostDesc());
        holder.callU.setText(uploadCurrent.getPhone());

    }


    @Override
    public int getItemCount() {
        return mUploads.size();
    }



    public class UserViewHolder extends RecyclerView.ViewHolder /*implements View.OnClickListener*/ {

        TextView nameU,postU;
        AppCompatButton callU;

        public UserViewHolder(View itemView) {
            super(itemView);
            nameU    = (itemView).findViewById(R.id.txtv_fname);
            postU   = (itemView).findViewById(R.id.txtv_desc);
            callU  = (itemView).findViewById(R.id.btn_call);

             //       itemView.setOnClickListener(this);
        }

//
//        @Override
//        public void onClick(View view) {
//
//            int position = getAbsoluteAdapterPosition();
//            Intent i =new Intent(mContext,UserProfileAcivity.class);
//            i.putExtra("name",mUploads.get(position).getFullname());
//            mContext.startActivity(i);
//
//        }
    }
}
