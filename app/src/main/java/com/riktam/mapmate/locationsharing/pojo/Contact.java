package com.riktam.mapmate.locationsharing.pojo;

/**
 * Created by admin1 on 8/6/17.
 */
public class Contact {

    private int id;
    private String name;
    private String email;
    private String photoUrl;

    public Contact() {
    }

    public Contact(String name, String email, String photoUrl) {
        super();
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Contact [id=" + id + ", name=" + name + ", email=" + email + ", name=" + name + "]";
    }

}