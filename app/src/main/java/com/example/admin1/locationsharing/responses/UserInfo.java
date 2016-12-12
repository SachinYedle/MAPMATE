package com.example.admin1.locationsharing.responses;

import java.util.ArrayList;

/**
 * Created by admin1 on 7/12/16.
 */

public class UserInfo {
    private ArrayList<UserData> userData;

    public void setUserDataArrayList(ArrayList<UserData> userDataArrayList) {
        this.userData = userDataArrayList;
    }

    public ArrayList<UserData> getUserDataArrayList() {
        return userData;
    }
}
