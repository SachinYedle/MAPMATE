package com.riktam.mapmate.locationsharing.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by admin1 on 9/6/17.
 */

public class LocationSharedFriendsResponse {
    @SerializedName("data")
    @Expose
    private ArrayList<LocationSharedFriends> sharedFriends;

    @SerializedName("success")
    @Expose
    private String succes;

    public ArrayList<LocationSharedFriends> getSharedFriends() {
        return sharedFriends;
    }

    public void setSharedFriends(ArrayList<LocationSharedFriends> sharedFriends) {
        this.sharedFriends = sharedFriends;
    }

    public String getSucces() {
        return succes;
    }

    public void setSucces(String succes) {
        this.succes = succes;
    }
}
