package com.riktam.mapmate.locationsharing.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.acitivities.FriendsActivity;
import com.riktam.mapmate.locationsharing.acitivities.FriendsRouteActivity;
import com.riktam.mapmate.locationsharing.acitivities.MapActivity;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.utils.Navigator;
import com.squareup.picasso.Picasso;


/**
 * Created by admin1 on 6/12/16.
 */

public class DrawerFragment extends Fragment implements View.OnClickListener {
    private TextView usernametextView, emailTextView, myRouteTextView,
            logoutTextView, friendsLocationTextView, friendsTextView;
    private ImageView profileImageView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.drawer_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setValuesToViews();
    }

    private void initializeViews(View view) {
        profileImageView = (ImageView) view.findViewById(R.id.drawer_item_user_profile_imageView);
        usernametextView = (TextView) view.findViewById(R.id.drawer_item_username_textView);
        emailTextView = (TextView) view.findViewById(R.id.drawer_item_email_textView);
        myRouteTextView = (TextView) view.findViewById(R.id.drawer_item_my_route_textView);
        logoutTextView = (TextView) view.findViewById(R.id.drawer_item_logout_textView);
        friendsLocationTextView = (TextView) view.findViewById(R.id.drawer_item_friends_location_textView);
        friendsTextView = (TextView) view.findViewById(R.id.drawer_item_friends_textView);

        myRouteTextView.setOnClickListener(this);
        logoutTextView.setOnClickListener(this);
        friendsLocationTextView.setOnClickListener(this);
        friendsTextView.setOnClickListener(this);
    }

    private void setValuesToViews() {
        usernametextView.setText(MyApplication.getInstance().sharedPreferencesData.getFirstName() +
                " " + MyApplication.getInstance().sharedPreferencesData.getLastName());
        emailTextView.setText(MyApplication.getInstance().sharedPreferencesData.getEmail());

        Picasso.with(MyApplication.getCurrentActivityContext()).load(MyApplication.getInstance().sharedPreferencesData.getProfilePic()).into(profileImageView);
    }

    @Override
    public void onClick(View view) {
        if (getActivity() instanceof MapActivity) {
            ((MapActivity) getActivity()).closeDrawer();
        } else if(getActivity() instanceof FriendsActivity) {
            ((FriendsActivity) getActivity()).closeDrawer();
        } else {
            ((FriendsRouteActivity  ) getActivity()).closeDrawer();
        }
        switch (view.getId()) {
            case R.id.drawer_item_my_route_textView:
                Navigator.getInstance().navigateToFriendsRouteActivity(MyApplication.getInstance().sharedPreferencesData.getEmail(), "drawer");
                getActivity().finish();
                break;
            case R.id.drawer_item_logout_textView:
                Navigator.getInstance().navigateToMainActivity();
                break;
            case R.id.drawer_item_friends_textView:
                Navigator.getInstance().navigateToFriendsActivity();
                getActivity().finish();
                break;
            case R.id.drawer_item_friends_location_textView:
                if(getActivity() instanceof MapActivity){
                    ((MapActivity) getActivity()).closeDrawer();
                }else {
                    Navigator.getInstance().navigateToMapActivity();
                    getActivity().finish();
                }
                break;
        }
    }
}
