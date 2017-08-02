package com.riktam.mapmate.locationsharing.utils;

import android.graphics.Color;

import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.db.dao.UserLastKnownLocation;
import com.riktam.mapmate.locationsharing.db.dao.UserLocations;
import com.riktam.mapmate.locationsharing.db.operations.UserLocationsOperations;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin1 on 20/2/17.
 */

public class DrawRouteFunctionality {

    private static DrawRouteFunctionality instance;

    public static DrawRouteFunctionality getInstance() {
        if (instance == null) {
            instance = new DrawRouteFunctionality();
        }
        return instance;
    }

    public void drawRouteOfSelectedUser(String email, GoogleMap googleMap) {
        MyApplication.getInstance().hideProgressDialog();
        MyApplication.getInstance().showProgressDialog("Drawing path", "please wait...");
        googleMap.clear();
        googleMap.setOnInfoWindowClickListener(null);
        ArrayList<LatLng> points = new ArrayList<LatLng>();
        PolylineOptions lineOptions = new PolylineOptions();
        List<UserLocations> locations = UserLocationsOperations.getInstance().getUserLocations(email);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < locations.size(); i++) {
            double lat = Double.parseDouble(locations.get(i).getLatitude());
            double lng = Double.parseDouble(locations.get(i).getLongitude());
            LatLng position = new LatLng(lat, lng);
            CustomLog.i("Data", lat + " " + lng + " " + locations.get(i).getEmail());
            String title = "Updated: "+TimeInAgoFormat.getInstance().timeInAgoFormat(locations.get(i).getUpdated_time());
            String snippet = "Duration: "+ TimeInAgoFormat.getInstance().getTimeDifference(locations.get(i).getCreated_time(),locations.get(i).getUpdated_time());
            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.small_blue_dot)));
            builder.include(position);
            points.add(position);
        }
        if (points.size() > 0) {
            lineOptions.addAll(points);
            lineOptions.width(7);
            lineOptions.color(Color.RED);
            googleMap.addPolyline(lineOptions);
            addStartAndEndMarkers(googleMap, locations);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 15));
        }
        MyApplication.getInstance().hideProgressDialog();
    }

    private void addStartAndEndMarkers(GoogleMap googleMap, List<UserLocations> locations) {
        double lat = Double.parseDouble(locations.get(0).getLatitude());
        double lng = Double.parseDouble(locations.get(0).getLongitude());
        LatLng position = new LatLng(lat, lng);
        String titile = "Stopped : " + TimeInAgoFormat.getInstance().timeInAgoFormat(locations.get(0).getUpdated_time());
        String snippet = "Duration: "+ TimeInAgoFormat.getInstance().getTimeDifference(locations.get(0).getCreated_time(),locations.get(0).getUpdated_time());
        googleMap.setOnInfoWindowClickListener(null);
        googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(titile)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.small_red_marker)));
    }
}
