package com.example.admin1.locationsharing.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by admin1 on 13/1/17.
 */

public class FriendsServiceResponse {
    @SerializedName("success")
    @Expose
    private boolean success;
    @SerializedName("data")
    @Expose
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

