package com.example.admin1.locationsharing.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by admin1 on 30/11/16.
 */

public class SharedPreferencesData {

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    private Context context;


    private final String PREF_NAME = "com.example.admin1.locationsharing";
    // Preferences can only be accessed by the application
    private final int PRIVATE_MODE = 0;

    //Variable Names saved in preferences
    private final String USER_ID = "userID";
    private final String USER_PHONE = "userPhone";
    private final String USER_COUNTRY_CODE = "userCountryCode";
    private final String IS_FIRST_TIME = "isFirstTime";


    public SharedPreferencesData(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    public void setUserId(String userId) {
       editor.putString(USER_ID, userId);
        editor.commit();
    }
    public void setUserPhone(String userPhone) {
        editor.putString(USER_PHONE, userPhone);
        editor.commit();
    }

    public void setUserCountryCode(String userCountryCode) {
        editor.putString(USER_COUNTRY_CODE, userCountryCode);
        editor.commit();
    }

    public void setIsFirstTime(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME, isFirstTime);
        editor.commit();
    }

    public String getUserCountryCode() {
        return sharedPreferences.getString(USER_COUNTRY_CODE,"");
    }

    public boolean getIsFirstTime() {
        return sharedPreferences.getBoolean(IS_FIRST_TIME,false);
    }

    public String getUserId() {
        return sharedPreferences.getString(USER_ID,"");
    }
    public String getUserPhone() {
        return sharedPreferences.getString(USER_PHONE,"");
    }
}

