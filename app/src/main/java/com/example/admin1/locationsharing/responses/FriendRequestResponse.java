package com.example.admin1.locationsharing.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by admin1 on 16/1/17.
 */

public class FriendRequestResponse {
    @SerializedName("success")
    @Expose
    private boolean success;
    @SerializedName("msg")
    @Expose
    String data;

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }
}
