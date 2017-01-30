package com.example.admin1.locationsharing.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by admin1 on 13/1/17.
 */

public class FriendsResponse {
    @SerializedName("friend_email")
    @Expose
    private String friendEmail;
    @SerializedName("friend_id")
    @Expose
    private String friendId;
    @SerializedName("friend_first_name")
    @Expose
    private String friendFirstName;

    @SerializedName("friend_request_id")
    @Expose
    private String friendRequestId;

    @SerializedName("location")
    @Expose
    private FriendLocation friendLocation;

    @SerializedName("requester_id")
    @Expose
    private String requesterId;
    @SerializedName("status")
    @Expose
    private int status;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public void setFriendEmail(String friendEmail) {
        this.friendEmail = friendEmail;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public void setFriendRequestId(String friendRequestId) {
        this.friendRequestId = friendRequestId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getFriendEmail() {
        return friendEmail;
    }

    public int getStatus() {
        return status;
    }

    public String getFriendId() {
        return friendId;
    }

    public String getFriendRequestId() {
        return friendRequestId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setFriendFirstName(String friendFirstName) {
        this.friendFirstName = friendFirstName;
    }

    public String getFriendFirstName() {
        return friendFirstName;
    }

    public void setFriendLocation(FriendLocation friendLocation) {
        this.friendLocation = friendLocation;
    }

    public FriendLocation getFriendLocation() {
        return friendLocation;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
