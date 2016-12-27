package com.example.admin1.locationsharing.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by admin1 on 27/12/16.
 */

public class UserLocationData {
    @SerializedName("created_at")
    @Expose
    private String time;

    @SerializedName("lat")
    @Expose
    private String lat;

    @SerializedName("lon")
    @Expose
    private String lon;

    @SerializedName("radius")
    @Expose
    private String radius;

    public void setTime(String time) {
        this.time = time;
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

    public String getTime() {
        return time;
    }
}
