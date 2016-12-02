package com.example.admin1.locationsharing.pojo;

import java.util.ArrayList;

/**
 * Created by admin1 on 2/12/16.
 */

public class UserInfo {
    private ArrayList<UserData> userData;

    public void setUserData(ArrayList<UserData> userData) {
        this.userData = userData;
    }

    public ArrayList<UserData> getUserData() {
        return userData;
    }
}
