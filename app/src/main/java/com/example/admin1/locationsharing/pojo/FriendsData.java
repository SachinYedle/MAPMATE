package com.example.admin1.locationsharing.pojo;

/**
 * Created by admin1 on 13/1/17.
 */

public class FriendsData {
    private String friendsEmail;
    private String friendFirstName;
    private String status;

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
