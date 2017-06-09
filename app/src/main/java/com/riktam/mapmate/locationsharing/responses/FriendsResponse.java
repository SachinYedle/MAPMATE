package com.riktam.mapmate.locationsharing.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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

    @SerializedName("lat")
    @Expose
    private String lat;

    @SerializedName("lon")
    @Expose
    private String lon;

    @SerializedName("requester_id")
    @Expose
    private String requesterId;

    @SerializedName("last_known_time")
    @Expose
    private String lastKnownTime;

    @SerializedName("status")
    @Expose
    private int status;

    @SerializedName("sharing")
    @Expose
    private int sharing;

    @SerializedName("friend_profile_url")
    @Expose
    private String friendProfileUrl;

    @SerializedName("updated_at")
    @Expose
    private String updatedAt;


    public String getFriendProfileUrl() {
        return friendProfileUrl;
    }

    public void setFriendProfileUrl(String friendProfileUrl) {
        this.friendProfileUrl = friendProfileUrl;
    }

    public int getSharing() {
        return sharing;
    }

    public void setSharing(int sharing) {
        this.sharing = sharing;
    }

    public String getLastKnownTime() {
        return lastKnownTime;
    }

    public void setLastKnownTime(String lastKnownTime) {
        this.lastKnownTime = lastKnownTime;
    }


    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

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

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
