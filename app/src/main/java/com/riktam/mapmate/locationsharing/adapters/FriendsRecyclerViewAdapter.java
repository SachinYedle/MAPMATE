package com.riktam.mapmate.locationsharing.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.interfaces.ItemClickListener;
import com.riktam.mapmate.locationsharing.pojo.FriendsData;
import com.riktam.mapmate.locationsharing.utils.BitMapMerging;
import com.riktam.mapmate.locationsharing.utils.CustomLog;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by admin1 on 13/1/17.
 */

public class FriendsRecyclerViewAdapter extends RecyclerView.Adapter<FriendsRecyclerViewAdapter.ViewHolder> {

    private ItemClickListener itemClickListener;
    private ArrayList<FriendsData> friendsArrayList;
    private Context context;
    private String searchText;

    public FriendsRecyclerViewAdapter(ArrayList<FriendsData> friendsArrayList) {
        context = MyApplication.getCurrentActivityContext();
        this.friendsArrayList = friendsArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View view = inflater.inflate(R.layout.friends_recycleview_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(friendsArrayList.get(position).getFriendProfileUrl() != null){
            Picasso.with(MyApplication.getCurrentActivityContext()).load(friendsArrayList.get(position).getFriendProfileUrl()).into(holder.profileImageView);
        }
        holder.nameTextView.setText(highlightText(friendsArrayList.get(position).getFriendFirstName()));
        holder.emailTextView.setText(highlightText(friendsArrayList.get(position).getFriendsEmail()));
        if(friendsArrayList.get(position).getStatus().equalsIgnoreCase(context.getString(R.string.start)) || friendsArrayList.get(position).getStatus().equalsIgnoreCase(context.getString(R.string.stop))){
            holder.shareLocationSwitch.setVisibility(View.VISIBLE);
            holder.statusTextView.setVisibility(View.GONE);
            holder.shareLocationSwitch.setChecked(friendsArrayList.get(position).getStatus().equalsIgnoreCase(context.getString(R.string.stop)));
        }else {
            holder.statusTextView.setVisibility(View.VISIBLE);
            holder.shareLocationSwitch.setVisibility(View.GONE);
            holder.statusTextView.setText(friendsArrayList.get(position).getStatus());
        }

        if(friendsArrayList.get(position).getStatus().equals("add")){
            holder.statusTextView.setVisibility(View.GONE);
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        friendsArrayList.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(ArrayList<FriendsData> friendsArrayList) {
        this.friendsArrayList = friendsArrayList;
        notifyDataSetChanged();
    }

    public void setFilter(ArrayList<FriendsData> friendsList, String searchText) {
        if (friendsList.size() <= 0) {
            friendsArrayList = new ArrayList<>();
            notifyDataSetChanged();
        } else {
            friendsArrayList = new ArrayList<>();
            friendsArrayList.addAll(friendsList);
            this.searchText = searchText;
            CustomLog.d("FilteredContact", "Size:" + friendsList.size());
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return friendsArrayList.size();
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public CharSequence highlightText(String name) {
        if (searchText != null && searchText.length() > 0) {
            SpannableStringBuilder stringBuilder = null;
            int index = name.toLowerCase().indexOf(searchText.toLowerCase());
            while (index > -1) {
                stringBuilder = new SpannableStringBuilder(name);
                ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(135, 228, 158));

                stringBuilder.setSpan(fcs, index, index + searchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                index = name.toLowerCase().indexOf(searchText.toLowerCase(), index + 1);
            }
            CustomLog.d("highliteText", stringBuilder + "");
            return stringBuilder;
        } else {
            return name;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView emailTextView, statusTextView,nameTextView;
        private ImageView profileImageView;
        private Switch shareLocationSwitch;
        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            shareLocationSwitch = (Switch) itemView.findViewById(R.id.friends_recycler_item_share_location_switch);
            shareLocationSwitch.setOnClickListener(this);
            emailTextView = (TextView) itemView.findViewById(R.id.friends_recycler_item_email_textView);
            emailTextView.setOnClickListener(this);
            statusTextView = (TextView) itemView.findViewById(R.id.friends_recycler_item_status_textView);
            nameTextView = (TextView) itemView.findViewById(R.id.friends_recycler_item_name_textView);
            nameTextView.setOnClickListener(this);
            profileImageView = (ImageView)itemView.findViewById(R.id.friends_recycler_item_profile_imageView);
            profileImageView.setOnClickListener(this);
            statusTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onItemClick(view, getAdapterPosition(), friendsArrayList.get(getAdapterPosition()));
        }
    }
}