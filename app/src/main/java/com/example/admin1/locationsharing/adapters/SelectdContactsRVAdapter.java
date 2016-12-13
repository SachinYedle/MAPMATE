package com.example.admin1.locationsharing.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.customviews.RoundedImageView;
import com.example.admin1.locationsharing.db.dao.Contacts;
import com.example.admin1.locationsharing.interfaces.ItemClickListener;
import com.example.admin1.locationsharing.utils.CustomLog;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by admin1 on 13/12/16.
 */

public class SelectdContactsRVAdapter extends RecyclerView.Adapter<SelectdContactsRVAdapter.ViewHolder>  {

    private Context context;
    private ArrayList<Contacts> contacts;
    private ItemClickListener itemClickListener;

    public SelectdContactsRVAdapter(Context context, ArrayList<Contacts> contacts) {
        this.context = context;
        this.contacts = contacts;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        View view = inflater.inflate(R.layout.selected_contacts_recyclerview_layout,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        String name = contacts.get(position).getFirst_name();
        String []nameArray = name.split(" ");
        holder.nameTextView.setText(nameArray[0]);
        String imageUri = contacts.get(position).getPhoto();
        Bitmap photo = null;
        try {
            if (imageUri != null) {
                photo = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(imageUri));
            } else {
                photo = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_contact_pic);
            }
        } catch (IOException e) {
            CustomLog.d("getContatcs","image loading error");
        }
        holder.roundedImage.setImageBitmap(photo);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView nameTextView;
        ImageView roundedImage;
        public ViewHolder(View itemView) {
            super(itemView);
            //itemView.setOnClickListener(this);
            nameTextView = (TextView) itemView.findViewById(R.id.name_text_view);
            roundedImage = (RoundedImageView)itemView.findViewById(R.id.photo_rounded_image_view);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onItemClick(view,getAdapterPosition(), contacts);
        }
    }
}

