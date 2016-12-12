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
import com.example.admin1.locationsharing.db.dao.Contacts;
import com.example.admin1.locationsharing.interfaces.ItemClickListener;
import com.example.admin1.locationsharing.pojo.Contact;

import java.util.ArrayList;



public class ContactRecyclerViewAdapter extends RecyclerView.Adapter<ContactRecyclerViewAdapter.ViewHolder>  {

    private Context context;
    private ArrayList<Contacts> contacts;

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
        String name = contacts.get(position).getFirst_name() +" "+ contacts.get(position).getFirst_name();
        holder.name.setText(name);
        holder.phone.setText(contacts.get(position).getPhone());
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(contacts.get(position).getIs_contact_added());
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!contacts.get(position).getIs_contact_added()){
                    contacts.get(position).setIs_contact_added(true);
                }else {
                    contacts.get(position).setIs_contact_added(false);
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
