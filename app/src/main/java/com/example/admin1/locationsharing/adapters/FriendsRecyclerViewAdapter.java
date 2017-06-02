package com.example.admin1.locationsharing.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.Friends;
import com.example.admin1.locationsharing.db.operations.FriendsTableOperations;
import com.example.admin1.locationsharing.interfaces.ItemClickListener;
import com.example.admin1.locationsharing.mappers.FriendsDataMapper;
import com.example.admin1.locationsharing.pojo.FriendsData;
import com.example.admin1.locationsharing.utils.BitMapMerging;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;

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

    private Bitmap getProfileBitmap(String name){
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getRandomColor();
        CustomLog.i("Color","color:"+color);
        String nameFirstLetter = name.charAt(0) + "".toUpperCase();
        TextDrawable drawable = TextDrawable.builder()
                .buildRect(nameFirstLetter, color);
        return BitMapMerging.getInstance().drawableToBitmap(drawable);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.profileImageView.setImageBitmap(getProfileBitmap(friendsArrayList.get(position).getFriendFirstName()));
        holder.nameTextView.setText(friendsArrayList.get(position).getFriendFirstName());
        holder.emailTextView.setText(highlightText(friendsArrayList.get(position).getFriendsEmail()));
        holder.statusTextView.setText(friendsArrayList.get(position).getStatus());
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
        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            emailTextView = (TextView) itemView.findViewById(R.id.friends_recycler_item_email_textView);
            statusTextView = (TextView) itemView.findViewById(R.id.friends_recycler_item_status_textView);
            nameTextView = (TextView) itemView.findViewById(R.id.friends_recycler_item_name_textView);
            profileImageView = (ImageView)itemView.findViewById(R.id.friends_recycler_item_profile_imageView);
            statusTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onItemClick(view, getAdapterPosition(), friendsArrayList.get(getAdapterPosition()));
        }
    }
}