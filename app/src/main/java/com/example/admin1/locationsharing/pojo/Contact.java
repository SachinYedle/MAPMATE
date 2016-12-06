package com.example.admin1.locationsharing.pojo;

/**
 * Created by admin1 on 6/12/16.
 */

public class Contact {
    private String name;
    private String phone;
    private boolean selected;

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return selected;
    }
}
