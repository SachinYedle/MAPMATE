package com.example.admin1.locationsharing.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by admin1 on 16/12/16.
 */

public class UserAuthentication {
    @SerializedName("success")
    @Expose
    private String succesMsg;

    @SerializedName("data")
    @Expose
    private UserAuthToken userAuthToken;


    public void setSuccesMsg(String succesMsg) {
        this.succesMsg = succesMsg;
    }

    public void setUserAuthToken(UserAuthToken userAuthToken) {
        this.userAuthToken = userAuthToken;
    }

    public String getSuccesMsg() {
        return succesMsg;
    }

    public UserAuthToken getUserAuthToken() {
        return userAuthToken;
    }
}
