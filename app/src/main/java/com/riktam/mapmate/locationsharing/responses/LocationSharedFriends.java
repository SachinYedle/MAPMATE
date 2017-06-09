package com.riktam.mapmate.locationsharing.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by admin1 on 9/6/17.
 */

public class LocationSharedFriends {

    @SerializedName("friend_email")
    @Expose
    private String friendEmail;
    @SerializedName("friend_first_name")
    @Expose
    private String friendFirstName;

    @SerializedName("lat")
    @Expose
    private String lat;

    @SerializedName("lon")
    @Expose
    private String lon;

    @SerializedName("last_known_time")
    @Expose
    private String lastKnownTime;


    @SerializedName("friend_profile_url")
    @Expose
    private String friendProfileUrl;

    public String getFriendEmail() {
        return friendEmail;
    }

    public void setFriendEmail(String friendEmail) {
        this.friendEmail = friendEmail;
    }

    public String getFriendFirstName() {
        return friendFirstName;
    }

    public void setFriendFirstName(String friendFirstName) {
        this.friendFirstName = friendFirstName;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLastKnownTime() {
        return lastKnownTime;
    }

    public void setLastKnownTime(String lastKnownTime) {
        this.lastKnownTime = lastKnownTime;
    }

    public String getFriendProfileUrl() {
        return friendProfileUrl;
    }

    public void setFriendProfileUrl(String friendProfileUrl) {
        this.friendProfileUrl = friendProfileUrl;
    }
}
