package com.example.admin1.locationsharing.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.params.StreamConfigurationMap;

/**
 * Created by admin1 on 30/11/16.
 */

public class SharedPreferencesData {

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;

    private final String PREF_NAME = "com.example.admin1.locationsharing";
    // Preferences can only be accessed by the application
    private final int PRIVATE_MODE = 0;

    //Variable Names saved in preferences
    private final String USER_TOKEN = "userToken";
    private final String IS_FIRST_TIME = "isFirstTime";
    private final String EMAIL = "email";
    private final String SELECTED_USER_EMAIL = "selectedUserEmail";
    private final String FIRST_NAME = "firstName";
    private final String LAST_NAME = "lastName";
    private final String ID = "id";

    public void setId(int id) {
        editor.putInt(ID, id);
        editor.commit();
    }

    public int getId() {
        return sharedPreferences.getInt(ID, 0);
    }

    public SharedPreferencesData(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    public void setUserToken(String userToken) {
        editor.putString(USER_TOKEN, userToken);
        editor.commit();
    }

    public void setEmail(String email) {
        editor.putString(EMAIL, email);
        editor.commit();
    }

    public void setIsFirstTime(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME, isFirstTime);
        editor.commit();
    }

    public void setSelectedUserEmail(String selectedUserEmail) {
        editor.putString(SELECTED_USER_EMAIL, selectedUserEmail);
        editor.commit();
    }

    public void setFirstName(String firstName) {
        editor.putString(FIRST_NAME, firstName);
        editor.commit();
    }

    public void setLastName(String lastName) {
        editor.putString(LAST_NAME, lastName);
        editor.commit();
    }

    public String getEmail() {
        return sharedPreferences.getString(EMAIL, "");
    }

    public String getUserToken() {
        return sharedPreferences.getString(USER_TOKEN, "");
    }

    public String getSelectedUserEmail() {
        return sharedPreferences.getString(SELECTED_USER_EMAIL, "");
    }
    public boolean getIsFirstTime() {
        return sharedPreferences.getBoolean(IS_FIRST_TIME,false);
    }

    public String getFirstName() {
        return sharedPreferences.getString(FIRST_NAME,"");
    }

    public String getLastName() {
        return sharedPreferences.getString(LAST_NAME,"");
    }
}

