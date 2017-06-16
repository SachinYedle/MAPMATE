package com.riktam.mapmate.locationsharing.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by admin1 on 15/6/17.
 */

public class MailInviteResponse {
    @SerializedName("data")
    @Expose
    private String data;

    @SerializedName("success")
    @Expose
    private boolean success;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
