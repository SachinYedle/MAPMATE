package com.example.admin1.locationsharing.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.interfaces.ItemClickListener;
import com.example.admin1.locationsharing.pojo.Contact;

import java.util.ArrayList;

/**
 * Created by sid-riktam-2 on 15-11-2016.
 */

public class ContactRecyclerViewAdapter extends RecyclerView.Adapter<ContactRecyclerViewAdapter.ViewHolder>  {

    private Context context;
    private ArrayList<Contact> contacts;

    public ContactRecyclerViewAdapter(Context context, ArrayList<Contact> contacts) {
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
        holder.name.setText(contacts.get(position).getName());
        holder.phone.setText(contacts.get(position).getPhone());
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(contacts.get(position).isSelected());
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!contacts.get(position).isSelected()){
                    contacts.get(position).setSelected(true);
                }else {
                    contacts.get(position).setSelected(false);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name,phone;
        CheckBox checkBox;
        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            phone = (TextView) itemView.findViewById(R.id.phone);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }

    }
}
