package com.example.admin1.locationsharing.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.Friends;
import com.example.admin1.locationsharing.interfaces.ItemClickListener;
import com.example.admin1.locationsharing.pojo.FriendsData;

import java.util.ArrayList;

/**
 * Created by admin1 on 13/1/17.
 */

public class FriendsRecyclerViewAdapter extends RecyclerView.Adapter<FriendsRecyclerViewAdapter.ViewHolder> {

    private ItemClickListener itemClickListener;
    private ArrayList<FriendsData> friendsArrayList;
    private Context context;
    public FriendsRecyclerViewAdapter(ArrayList<FriendsData> friendsArrayList) {
        context = MyApplication.getCurrentActivityContext();
        this.friendsArrayList = friendsArrayList;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        View view = inflater.inflate(R.layout.friends_recycleview_layout,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.emailTextView.setText(friendsArrayList.get(position).getFriendsEmail());
        holder.statusTextView.setText(friendsArrayList.get(position).getStatus());
    }

    @Override
    public int getItemCount() {
        return friendsArrayList.size();
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView emailTextView,statusTextView;
        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            emailTextView = (TextView) itemView.findViewById(R.id.friends_recyclerView_email_textView);
            statusTextView = (TextView) itemView.findViewById(R.id.friends_recyclerView_status_textView);
            statusTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onItemClick(view, getAdapterPosition());
        }
    }
}
