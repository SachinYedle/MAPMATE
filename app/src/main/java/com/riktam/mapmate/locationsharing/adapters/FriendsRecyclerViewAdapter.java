package com.riktam.mapmate.locationsharing.adapters;

import android.app.Activity;
import android.content.Context;
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
import com.riktam.mapmate.locationsharing.acitivities.FriendsActivity;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.interfaces.ItemClickListener;
import com.riktam.mapmate.locationsharing.pojo.FriendsData;
import com.riktam.mapmate.locationsharing.utils.CustomLog;
import com.squareup.picasso.Picasso;
import com.tonicartos.superslim.GridSLM;
import com.tonicartos.superslim.LinearSLM;

import java.util.ArrayList;

/**
 * Created by admin1 on 13/1/17.
 */

public class FriendsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ItemClickListener itemClickListener;
    private ArrayList<FriendsData> friendsArrayList;
    private Context context;
    private String searchText;
    public static final int VIEW_TYPE_HEADER = 0x01;
    public static final int VIEW_TYPE_FRIENDS = 0x00;
    public static final int VIEW_TYPE_GOOGLE_FRIENDS = 0x02;


    public FriendsRecyclerViewAdapter(ArrayList<FriendsData> friendsArrayList) {
        context = MyApplication.getCurrentActivityContext();
        this.friendsArrayList = friendsArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View view = null;
        if (viewType == VIEW_TYPE_HEADER) {
            view = inflater.inflate(R.layout.friends_recycler_header, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == VIEW_TYPE_FRIENDS) {
            view = inflater.inflate(R.layout.friends_recycleview_layout, parent, false);
            return new FriendViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.gmail_friends_recycler_layout, parent, false);
            return new GoogleFriendViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_FRIENDS) {
            if (((FriendsActivity) context).googleFriendsHeaderTextView.getVisibility() == View.VISIBLE) {
                ((FriendsActivity) context).googleFriendsHeaderTextView.setVisibility(View.GONE);
            }
            if (friendsArrayList.get(position).getFriendProfileUrl() != null) {
                Picasso.with(MyApplication.getCurrentActivityContext()).load(friendsArrayList.get(position).getFriendProfileUrl()).into(((FriendViewHolder) holder).profileImageView);
            }
            ((FriendViewHolder) holder).nameTextView.setText(highlightText(friendsArrayList.get(position).getFriendFirstName()));
            ((FriendViewHolder) holder).emailTextView.setText(highlightText(friendsArrayList.get(position).getFriendsEmail()));
            if (friendsArrayList.get(position).getStatus().equalsIgnoreCase(context.getString(R.string.start)) || friendsArrayList.get(position).getStatus().equalsIgnoreCase(context.getString(R.string.stop))) {
                ((FriendViewHolder) holder).shareLocationSwitch.setVisibility(View.VISIBLE);
                ((FriendViewHolder) holder).statusTextView.setVisibility(View.GONE);
                ((FriendViewHolder) holder).shareLocationSwitch.setChecked(friendsArrayList.get(position).getStatus().equalsIgnoreCase(context.getString(R.string.stop)));
            } else {
                ((FriendViewHolder) holder).statusTextView.setVisibility(View.VISIBLE);
                ((FriendViewHolder) holder).shareLocationSwitch.setVisibility(View.GONE);
                ((FriendViewHolder) holder).statusTextView.setText(friendsArrayList.get(position).getStatus());
            }
            if (friendsArrayList.get(position).getStatus().equals("add")) {
                ((FriendViewHolder) holder).statusTextView.setVisibility(View.GONE);
            }
        } else if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            //((HeaderViewHolder) holder).headerTextView.setText(friendsArrayList.get(position).getFriendFirstName());
        } else if (getItemViewType(position) == VIEW_TYPE_GOOGLE_FRIENDS) {
            if (position >= (10 + ((FriendsActivity) context).googleFriendsStartingPos) && ((FriendsActivity) context).googleFriendsHeaderTextView.getVisibility() == View.GONE) {
                ((FriendsActivity) context).googleFriendsHeaderTextView.setVisibility(View.VISIBLE);
            }
            ((GoogleFriendViewHolder) holder).nameTextView.setText(highlightText(friendsArrayList.get(position).getFriendFirstName()));
            ((GoogleFriendViewHolder) holder).emailTextView.setText(highlightText(friendsArrayList.get(position).getFriendsEmail()));

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

    @Override
    public int getItemViewType(int position) {
        if (position == ((FriendsActivity) context).googleFriendsStartingPos) {
            return VIEW_TYPE_HEADER;
        } else if (friendsArrayList.get(position).getStatus() != null || friendsArrayList.get(position).getFriendProfileUrl() != null) {
            return VIEW_TYPE_FRIENDS;
        } else {
            return VIEW_TYPE_GOOGLE_FRIENDS;
        }
//        return super.getItemViewType(position);
    }

    public CharSequence highlightText(String name) {
        if (searchText != null && searchText.length() > 0) {
            SpannableStringBuilder stringBuilder = null;
            int index = name.toLowerCase().indexOf(searchText.toLowerCase());
            while (index > -1) {
                stringBuilder = new SpannableStringBuilder(name);
                ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(0, 0, 255));

                stringBuilder.setSpan(fcs, index, index + searchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                index = name.toLowerCase().indexOf(searchText.toLowerCase(), index + 1);
            }
            CustomLog.d("highliteText", stringBuilder + "");
            return stringBuilder;
        } else {
            return name;
        }
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView emailTextView, statusTextView, nameTextView;
        private ImageView profileImageView;
        private Switch shareLocationSwitch;

        public FriendViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            shareLocationSwitch = (Switch) itemView.findViewById(R.id.friends_recycler_item_share_location_switch);
            shareLocationSwitch.setOnClickListener(this);
            emailTextView = (TextView) itemView.findViewById(R.id.friends_recycler_item_email_textView);
            emailTextView.setOnClickListener(this);
            statusTextView = (TextView) itemView.findViewById(R.id.friends_recycler_item_status_textView);
            nameTextView = (TextView) itemView.findViewById(R.id.friends_recycler_item_name_textView);
            nameTextView.setOnClickListener(this);
            profileImageView = (ImageView) itemView.findViewById(R.id.friends_recycler_item_profile_imageView);
            profileImageView.setOnClickListener(this);
            statusTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onItemClick(view, getAdapterPosition(), friendsArrayList.get(getAdapterPosition()));
        }
    }

    public class GoogleFriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView emailTextView, nameTextView;
        private ImageView profileImageView;

        public GoogleFriendViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            emailTextView = (TextView) itemView.findViewById(R.id.gmail_friends_recycler_item_email_textView);
            nameTextView = (TextView) itemView.findViewById(R.id.gmail_friends_recycler_item_name_textView);
            profileImageView = (ImageView) itemView.findViewById(R.id.gmail_friends_recycler_item_profile_imageView);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onItemClick(view, getAdapterPosition(), friendsArrayList.get(getAdapterPosition()));
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView headerTextView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerTextView = (TextView) itemView.findViewById(R.id.friends_recycler_header_item);
        }
    }
}