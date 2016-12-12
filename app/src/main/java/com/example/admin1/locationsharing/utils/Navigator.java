package com.example.admin1.locationsharing.utils;

import android.content.Intent;
import android.provider.Settings;

import com.example.admin1.locationsharing.acitivities.MapActivity;
import com.example.admin1.locationsharing.app.MyApplication;

/**
 * Created by admin1 on 5/12/16.
 */

public class Navigator {

    public static void navigateToMapActivity(){
        Intent intent = new Intent(MyApplication.getInstance().getCurrentActivityContext(),MapActivity.class);
        MyApplication.getInstance().getCurrentActivityContext().startActivity(intent);
    }
    public static void navgateToSettingsToStartGPS(){
        Intent callGPSSettingIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        MyApplication.getInstance().getCurrentActivityContext().startActivity(callGPSSettingIntent);
    }

    public static void navgateToSettingssToStartInternet(){
        Intent callGPSSettingIntent = new Intent(
                Settings.ACTION_DATE_SETTINGS);
        MyApplication.getInstance().getCurrentActivityContext().startActivity(callGPSSettingIntent);
    }
}

