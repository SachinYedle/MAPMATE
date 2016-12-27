package com.example.admin1.locationsharing.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by admin1 on 27/12/16.
 */

public class UserLocationsResponse {
    @SerializedName("success")
    @Expose
    private String success;

    @SerializedName("data")
    @Expose
    private List<UserLocationData> UserLocationDataList;

    public void setSuccess(String success) {
        this.success = success;
    }

    public void setUserLocationDataList(List<UserLocationData> UserLocationDataList) {
        this.UserLocationDataList = UserLocationDataList;
    }

    public List<UserLocationData> getUserLocationDataList() {
        return UserLocationDataList;
    }

    public String getSuccess() {
        return success;
    }
}
