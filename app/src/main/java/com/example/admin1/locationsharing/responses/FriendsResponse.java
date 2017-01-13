package com.example.admin1.locationsharing.responses;

/**
 * Created by admin1 on 13/1/17.
 */

public class FriendsResponse {
    private String friendEmail;
    private String friendId;
    private String friendRequestId;
    private String requesterId;
    private int status;

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
}
