package com.riktam.mapmate.locationsharing.utils;

import android.content.Intent;
import android.provider.Settings;

import com.riktam.mapmate.locationsharing.acitivities.FriendsActivity;
import com.riktam.mapmate.locationsharing.acitivities.MainActivity;
import com.riktam.mapmate.locationsharing.acitivities.MapActivity;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.db.operations.FriendsTableOperations;
import com.riktam.mapmate.locationsharing.db.operations.UserLastknownLocationOperations;

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
    public void navigateToMainActivity(){
        FriendsTableOperations.getInstance().deleteFriendsTableData();
        UserLastknownLocationOperations.getInstance().deleteUserLastKnownData();
        MyApplication.getInstance().sharedPreferencesData.setEmail("");
        Intent intent = new Intent(MyApplication.getCurrentActivityContext(),MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        MyApplication.getCurrentActivityContext().startActivity(intent);
    }

    public void navigateToMapActivity(){
        Intent intent = new Intent(MyApplication.getCurrentActivityContext(),MapActivity.class);
        MyApplication.getCurrentActivityContext().startActivity(intent);
    }

    public void navigateToMapActivity(String email){
        Intent intent = new Intent(MyApplication.getCurrentActivityContext(), MapActivity.class);
        intent.putExtra("email", email);
        MyApplication.getCurrentActivityContext().startActivity(intent);
    }


    public void navgateToSettingsToStartGPS(){
        Intent callGPSSettingIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        MyApplication.getCurrentActivityContext().startActivity(callGPSSettingIntent);
    }

    public void navgateToSettingssToStartInternet(){
        Intent callGPSSettingIntent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
        MyApplication.getCurrentActivityContext().startActivity(callGPSSettingIntent);
    }
    public void navigateToFriendsActivity(){
        Intent intent = new Intent(MyApplication.getCurrentActivityContext(),FriendsActivity.class);
        MyApplication.getCurrentActivityContext().startActivity(intent);
    }
}

