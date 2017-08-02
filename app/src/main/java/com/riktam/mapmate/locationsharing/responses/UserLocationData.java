package com.riktam.mapmate.locationsharing.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by admin1 on 27/12/16.
 */

public class UserLocationData {
    @SerializedName("created_at")
    @Expose
    private String createdTime;

    @SerializedName("updated_at")
    @Expose
    private String updatedTime;


    @SerializedName("friend_id")
    @Expose
    private String friendId;

    @SerializedName("lat")
    @Expose
    private String lat;

    @SerializedName("lon")
    @Expose
    private String lon;

    @SerializedName("radius")
    @Expose
    private String radius;

    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("user_id")
    @Expose
    private String userId;

    @SerializedName("sharing")
    @Expose
    private int sharing;

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public int getSharing() {
        return sharing;
    }

    public void setSharing(int sharing) {
        this.sharing = sharing;
    }


    public void setRadius(String radius) {
        this.radius = radius;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getRadius() {
        return radius;
    }

    public String getLon() {
        return lon;
    }

    public String getLat() {
        return lat;
    }


    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
