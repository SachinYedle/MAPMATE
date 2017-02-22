package com.example.admin1.locationsharing.utils;

import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.UserLocations;
import com.example.admin1.locationsharing.db.operations.UserLocationsOperations;
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
    private boolean isRouteVisible;
    private static DrawRouteFunctionality instance;
    public void setRouteVisible(boolean routeVisible) {
        isRouteVisible = routeVisible;
    }

    public boolean isRouteVisible() {
        return isRouteVisible;
    }

    public static DrawRouteFunctionality getInstance() {
        if (instance == null){
            instance = new DrawRouteFunctionality();
        }
        return instance;
    }

    public void drawRouteOfSelectedUser(String email, GoogleMap googleMap, Menu menu) {
        MyApplication.getInstance().showProgressDialog("Drawing path", "please wait...");
        googleMap.clear();
        setRouteVisible(true);
        MenuItem item = menu.findItem(R.id.map_menu_item_my_location);
        item.setVisible(false);
        googleMap.setOnInfoWindowClickListener(null);
        ArrayList<LatLng> points = new ArrayList<LatLng>();
        PolylineOptions lineOptions = new PolylineOptions();
        List<UserLocations> locations = UserLocationsOperations.getInstance()
                .getUserLocations(email);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < locations.size(); i++) {
            double lat = Double.parseDouble(locations.get(i).getLatitude());
            double lng = Double.parseDouble(locations.get(i).getLongitude());
            LatLng position = new LatLng(lat, lng);
            CustomLog.i("Data", lat + " " + lng + " " + locations.get(i).getEmail());
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
        double lat = Double.parseDouble(locations.get(locations.size() - 1).getLatitude());
        double lng = Double.parseDouble(locations.get(locations.size() - 1).getLongitude());
        LatLng position = new LatLng(lat, lng);
        googleMap.setOnInfoWindowClickListener(null);
        googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Start At: " + TimeInAgoFormat.getInstance().timeInAgoFormat(locations.get(locations.size() - 1).getTime()))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
        lat = Double.parseDouble(locations.get(0).getLatitude());
        lng = Double.parseDouble(locations.get(0).getLongitude());
        position = new LatLng(lat, lng);
        googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title("End At: " + TimeInAgoFormat.getInstance().timeInAgoFormat(locations.get(0).getTime()))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
    }

}
