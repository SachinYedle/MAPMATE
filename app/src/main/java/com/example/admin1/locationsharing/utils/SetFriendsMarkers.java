package com.example.admin1.locationsharing.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.MenuItem;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.UserLastKnownLocation;
import com.example.admin1.locationsharing.db.operations.UserLastknownLocationOperations;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * Created by admin1 on 20/2/17.
 */

public class SetFriendsMarkers {

    private static SetFriendsMarkers instance;

    public static SetFriendsMarkers getInstance() {
        if (instance == null) {
            instance = new SetFriendsMarkers();
        }
        return instance;
    }

    public void setFriendsLocationMarkers(GoogleMap googleMap, Menu menu) {
        googleMap.clear();
        DrawRouteFunctionality.getInstance().setRouteVisible(false);
        MenuItem item = menu.findItem(R.id.map_menu_item_my_location);
        item.setVisible(true);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        List<UserLastKnownLocation> locationList = UserLastknownLocationOperations.getInstance().getUserLastKnownLocation();
        if (locationList.size() > 0) {
            for (int i = 0; i < locationList.size(); i++) {
                if (locationList.get(i).getLatitude() != null && locationList.get(i).getLongitude() != null) {
                    Double latitude = Double.parseDouble(locationList.get(i).getLatitude());
                    Double longitude = Double.parseDouble(locationList.get(i).getLongitude());

                    LatLng latLng = new LatLng(latitude, longitude);
                    Bitmap bitmapInflated = BitMapMerging.getInstance().inflateViewGetBitmap(locationList.get(i).getFriend_first_name());
                    Bitmap bitmapIcon = BitMapMerging.getInstance().mergeBitmap(bitmapInflated, BitmapFactory.decodeResource(MyApplication.getCurrentActivityContext().getResources(), R.drawable.map_marker_blue));
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmapIcon);
                    googleMap.addMarker(new MarkerOptions().position(latLng).draggable(false)
                            .title(locationList.get(i).getEmail())
                            .icon(icon)
                            .snippet(locationList.get(i).getFriend_first_name()).visible(true));
                    builder.include(latLng);

                }
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 48));
        }else {
            MyApplication.getInstance().showToast("You have no friends..Please add friends");
        }
        MyApplication.getInstance().hideProgressDialog();
    }
}
