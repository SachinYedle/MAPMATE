package com.example.admin1.locationsharing.responses;

import java.util.ArrayList;

/**
 * Created by admin1 on 8/12/16.
 */

public class UsersLastLocations {
    private ArrayList<UsersLast30MinLocations> usersLast30MinLocations;

    public void setUsersLast30MinLocations(ArrayList<UsersLast30MinLocations> usersLast30MinLocations) {
        this.usersLast30MinLocations = usersLast30MinLocations;
    }

    public ArrayList<UsersLast30MinLocations> getUsersLast30MinLocations() {
        return usersLast30MinLocations;
    }
}
