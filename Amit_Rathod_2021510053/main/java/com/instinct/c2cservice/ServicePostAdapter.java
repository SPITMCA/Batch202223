package com.instinct.c2cservice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ServicePostAdapter extends RecyclerView.Adapter<ServicePostAdapter.SerPostViewHolder> implements Filterable {

    private Context mContext;
    private List<Server_Post> mUploads;
    List<Server_Post> backUp;
    public Server_Post uploadCurrent;
    public ServicePostAdapter(Context context, List<Server_Post> uploads) {
        mContext = context;
        mUploads = uploads;
        backUp = new ArrayList<>(uploads);
    }

    @NonNull
    @Override
    public SerPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.service_singlepost, parent, false);
        return new SerPostViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull SerPostViewHolder holder, int position) {
         uploadCurrent = mUploads.get(position);

        holder.cityS.setText(uploadCurrent.getShopCity());
        holder.serviceS.setText(uploadCurrent.getService());
        holder.shopnameS.setText(uploadCurrent.getShopName());
        Picasso.get()
                .load(uploadCurrent.getProfileImage())
                .placeholder(R.drawable.imagesplaceholder)
                .fit()
                .into(holder.profileServ);
        Picasso.get()
                .load(uploadCurrent.getProfileImage())
                .placeholder(R.drawable.imagesplaceholder)
                .fit()
                .into(holder.img_upload);

    }


    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence keyword) {
                List<Server_Post> filtereddata = new ArrayList<>();
                if(keyword.toString().isEmpty()){
                    filtereddata.addAll(backUp);
                }
                else{

                    for(Server_Post obj : backUp){
                        if(obj.getShopCity().toString().toLowerCase().contains(keyword.toString().toLowerCase())){
                         filtereddata.add(obj);
                        }
                    }

                }
                FilterResults results =  new FilterResults();
                results.values =filtereddata;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

                mUploads.clear();
                mUploads.addAll((ArrayList<Server_Post>)filterResults.values);
                notifyDataSetChanged();
            }
        };


    public class SerPostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView profileServ;
        ImageView  img_upload;
        TextView cityS,shopnameS,serviceS;

        public SerPostViewHolder(View itemView) {
            super(itemView);
            profileServ= (itemView).findViewById(R.id.imgpicS);
            cityS= (itemView).findViewById(R.id.txtv_city);
            shopnameS= (itemView).findViewById(R.id.txtv_shopname);
            serviceS= (itemView).findViewById(R.id.txtv_service);
            img_upload= (itemView).findViewById(R.id.imagv_upload);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {

            int position = getAbsoluteAdapterPosition();
            Intent i =new Intent(mContext,UserProfileAcivity.class);
            i.putExtra("name",mUploads.get(position).getFullname());
            mContext.startActivity(i);

        }
    }
}
