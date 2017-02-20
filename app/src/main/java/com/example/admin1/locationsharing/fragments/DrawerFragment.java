package com.example.admin1.locationsharing.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;


/**
 * Created by admin1 on 6/12/16.
 */

public class DrawerFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.drawer_layout,container,false);
        TextView userNameText = (TextView) view.findViewById(R.id.drawer_user_name_textView);
        userNameText.setText(MyApplication.getInstance().sharedPreferencesData.getFirstName()+" "+ MyApplication.getInstance().sharedPreferencesData.getLastName());
        return view;
    }
}
