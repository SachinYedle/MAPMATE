package com.riktam.mapmate.locationsharing.pojo;

/**
 * Created by admin1 on 13/1/17.
 */

public class FriendsData {
    private String friendsEmail;
    private String friendFirstName;
    private String status;
    private int sharing;
    private String friendProfileUrl;

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

    public void setFriendFirstName(String friendFirstName) {
        this.friendFirstName = friendFirstName;
    }

    public String getFriendFirstName() {
        return friendFirstName;
    }

    public void setFriendsEmail(String friendsEmail) {
        this.friendsEmail = friendsEmail;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFriendsEmail() {
        return friendsEmail;
    }

    public String getStatus() {
        return status;
    }
}
