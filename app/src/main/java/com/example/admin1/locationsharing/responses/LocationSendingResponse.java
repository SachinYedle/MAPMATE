package com.example.admin1.locationsharing.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by admin1 on 19/12/16.
 */

public class LocationSendingResponse {
    @SerializedName("data")
    @Expose
    private String data;

    @SerializedName("success")
    @Expose
    private String succes;

    public void setSucces(String succes) {
        this.succes = succes;
    }

    public String getSucces() {
        return succes;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
}
