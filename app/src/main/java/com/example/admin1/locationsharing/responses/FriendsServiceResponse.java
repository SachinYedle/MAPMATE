package com.example.admin1.locationsharing.responses;

import java.util.ArrayList;

/**
 * Created by admin1 on 13/1/17.
 */

public class FriendsServiceResponse {
    private boolean success;
    private ArrayList<FriendsResponse> friendsResponseList;

    public void setFriendsResponseList(ArrayList<FriendsResponse> friendsResponseList) {
        this.friendsResponseList = friendsResponseList;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ArrayList<FriendsResponse> getFriendsResponseList() {
        return friendsResponseList;
    }

    public boolean isSuccess() {
        return success;
    }
}

