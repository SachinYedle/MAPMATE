package com.example.admin1.locationsharing.pojo;

/**
 * Created by admin1 on 6/12/16.
 */

public class Contact {
    private String firstName;
    private String photo;
    private String lastName;
    private String phone;
    private boolean isAdded;
    private boolean isShared;
    private boolean isRequested;


    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAdded(boolean added) {
        isAdded = added;
    }

    public void setShared(boolean shared) {
        isShared = shared;
    }

    public void setRequested(boolean requested) {
        isRequested = requested;
    }

    public String getPhone() {
        return phone;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isAdded() {
        return isAdded;
    }

    public boolean isShared() {
        return isShared;
    }

    public boolean isRequested() {
        return isRequested;
    }
}
