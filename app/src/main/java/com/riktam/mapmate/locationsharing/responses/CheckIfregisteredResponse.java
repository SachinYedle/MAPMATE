package com.riktam.mapmate.locationsharing.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by admin1 on 14/6/17.
 */

public class CheckIfregisteredResponse {
    @SerializedName("data")
    @Expose
    private FriendEmail data;

    @SerializedName("success")
    @Expose
    private boolean success;

    public FriendEmail getData() {
        return data;
    }

    public void setData(FriendEmail data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
