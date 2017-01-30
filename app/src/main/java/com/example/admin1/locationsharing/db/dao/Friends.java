package com.example.admin1.locationsharing.db.dao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "FRIENDS".
 */
public class Friends {

    private Long id;
    private String friend_email;
    private String friend_first_name;
    private String friend_id;
    private Integer friend_request_id;
    private String requester_id;
    private String status;

    public Friends() {
    }

    public Friends(Long id) {
        this.id = id;
    }

    public Friends(Long id, String friend_email, String friend_first_name, String friend_id, Integer friend_request_id, String requester_id, String status) {
        this.id = id;
        this.friend_email = friend_email;
        this.friend_first_name = friend_first_name;
        this.friend_id = friend_id;
        this.friend_request_id = friend_request_id;
        this.requester_id = requester_id;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFriend_email() {
        return friend_email;
    }

    public void setFriend_email(String friend_email) {
        this.friend_email = friend_email;
    }

    public String getFriend_first_name() {
        return friend_first_name;
    }

    public void setFriend_first_name(String friend_first_name) {
        this.friend_first_name = friend_first_name;
    }

    public String getFriend_id() {
        return friend_id;
    }

    public void setFriend_id(String friend_id) {
        this.friend_id = friend_id;
    }

    public Integer getFriend_request_id() {
        return friend_request_id;
    }

    public void setFriend_request_id(Integer friend_request_id) {
        this.friend_request_id = friend_request_id;
    }

    public String getRequester_id() {
        return requester_id;
    }

    public void setRequester_id(String requester_id) {
        this.requester_id = requester_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
