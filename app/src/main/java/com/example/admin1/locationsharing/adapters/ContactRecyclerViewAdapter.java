package com.example.admin1.locationsharing.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.customviews.RoundedImageView;
import com.example.admin1.locationsharing.db.dao.Contacts;
import com.example.admin1.locationsharing.interfaces.ItemClickListener;
import com.example.admin1.locationsharing.pojo.Contact;
import com.example.admin1.locationsharing.utils.CustomLog;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;


public class ContactRecyclerViewAdapter extends RecyclerView.Adapter<ContactRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Contacts> contacts;
    private ItemClickListener itemClickListener;
    private String searchText;

    public ContactRecyclerViewAdapter(Context context, ArrayList<Contacts> contacts) {
        this.context = context;
        this.contacts = contacts;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        View view = inflater.inflate(R.layout.contact_recyclerview_layout,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        String name = contacts.get(position).getFirst_name() +" "+ contacts.get(position).getLast_name();
        holder.nameTextView.setText(highlightText(name));

        if(contacts.get(position).getIs_location_shared()){
            holder.addRemoveTextTextView.setText("Remove");
        }else {
            holder.addRemoveTextTextView.setText("Add");
        }

        holder.phoneTextView.setText(contacts.get(position).getPhone());
        String imageUri = contacts.get(position).getPhoto();

        holder.roundedImageView.setImageBitmap(imageUritoBmp(imageUri));
    }

    public CharSequence highlightText(String name){
        if(searchText != null && searchText.length()>0){
            SpannableStringBuilder stringBuilder = null;
            int index = name.toLowerCase().indexOf(searchText.toLowerCase());
            while(index > -1){
                stringBuilder = new SpannableStringBuilder(name);
                ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(135, 228, 158));
                stringBuilder.setSpan(fcs, index, index + searchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                index = name.toLowerCase().indexOf(searchText.toLowerCase(), index + 1);
            }
            CustomLog.d("highliteText",stringBuilder+"");
            return stringBuilder;
        }else{
            return name;
        }
    }
    public Bitmap imageUritoBmp(String imageUri){
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
        return photo;
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void setFilter(ArrayList<Contacts> contactsArrayList,String searchText) {
        contacts = new ArrayList<>();
        contacts.addAll(contactsArrayList);
        this.searchText = searchText;
        CustomLog.d("FilteredContact","Size:"+contacts.size());
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView nameTextView,phoneTextView,addRemoveTextTextView;
        ImageView roundedImageView;
        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            nameTextView = (TextView) itemView.findViewById(R.id.name_text_view);
            phoneTextView = (TextView) itemView.findViewById(R.id.phone_text_view);
            addRemoveTextTextView = (TextView) itemView.findViewById(R.id.add_remove_text_text_view);
            roundedImageView = (RoundedImageView)itemView.findViewById(R.id.photo_image_view);
        }

        @Override
        public void onClick(View view) {
            if(addRemoveTextTextView.getText().toString().equals("Add")){
                addRemoveTextTextView.setText("Remove");
            }else {
                addRemoveTextTextView.setText("Add");
            }
            int position = getAdapterPosition();
            itemClickListener.onItemClick(view, position,contacts);
        }
    }
}
