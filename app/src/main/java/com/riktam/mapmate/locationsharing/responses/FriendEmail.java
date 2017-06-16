package com.riktam.mapmate.locationsharing.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by admin1 on 14/6/17.
 */

public class FriendEmail {
    @SerializedName("friend_email")
    @Expose
    private String friendEmail;

    public String getFriendEmail() {
        return friendEmail;
    }

    public void setFriendEmail(String friendEmail) {
        this.friendEmail = friendEmail;
    }
}
