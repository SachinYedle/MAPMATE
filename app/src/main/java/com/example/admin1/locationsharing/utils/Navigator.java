package com.example.admin1.locationsharing.utils;

import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.acitivities.FriendsActivity;
import com.example.admin1.locationsharing.acitivities.MapActivity;
import com.example.admin1.locationsharing.app.MyApplication;

/**
 * Created by admin1 on 5/12/16.
 */

public class Navigator {

    private static Navigator instance = null;

    public static Navigator getInstance() {
        if (instance == null) {
            instance = new Navigator();
        }
        return instance;
    }
    public void navigateToMapActivity(){
        Intent intent = new Intent(MyApplication.getCurrentActivityContext(),MapActivity.class);
        MyApplication.getCurrentActivityContext().startActivity(intent);
    }
    public void navgateToSettingsToStartGPS(){
        Intent callGPSSettingIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        MyApplication.getCurrentActivityContext().startActivity(callGPSSettingIntent);
    }

    public void navgateToSettingssToStartInternet(){
        Intent callGPSSettingIntent = new Intent(Settings.ACTION_DATE_SETTINGS);
        MyApplication.getCurrentActivityContext().startActivity(callGPSSettingIntent);
    }
    public void navigateToFriendsActivity(){
        Intent intent = new Intent(MyApplication.getCurrentActivityContext(),FriendsActivity.class);
        MyApplication.getCurrentActivityContext().startActivity(intent);
    }
}

