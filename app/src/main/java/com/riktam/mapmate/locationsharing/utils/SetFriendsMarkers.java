package com.riktam.mapmate.locationsharing.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;

import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.db.dao.UserLastKnownLocation;
import com.riktam.mapmate.locationsharing.db.operations.UserLastknownLocationOperations;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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

    public void setFriendsLocationMarkers(final GoogleMap googleMap, boolean isFirstTime) {
        googleMap.clear();
        DrawRouteFunctionality.getInstance().setRouteVisible(false);
        boolean isPointsAddedInBuilder = false;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        final List<UserLastKnownLocation> locationList = UserLastknownLocationOperations.getInstance().getUserLastKnownLocation();
        if (locationList.size() > 0) {
            for (int i = 0; i < locationList.size(); i++) {
                if ((locationList.get(i).getLatitude() != null && locationList.get(i).getLongitude() != null)) {
                    Double latitude = Double.parseDouble(locationList.get(i).getLatitude());
                    Double longitude = Double.parseDouble(locationList.get(i).getLongitude());
                    final LatLng latLng = new LatLng(latitude, longitude);
                    final int finalPos = i;
                    Target target = new Target() {

                        @Override
                        public void onPrepareLoad(Drawable arg0) {
                            return;
                        }
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom arg1) {
                            try {
                                if(finalPos == locationList.size()){
                                    MyApplication.getInstance().hideProgressDialog();
                                }
                                Bitmap bitmapInflated = BitMapMerging.getInstance().inflateViewGetBitmap(bitmap);
                                Bitmap bitmapIcon = BitMapMerging.getInstance().mergeBitmap(bitmapInflated, BitmapFactory.decodeResource(MyApplication.getCurrentActivityContext().getResources(), R.drawable.map_marker_blue));
                                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmapIcon);
                                googleMap.addMarker(new MarkerOptions().position(latLng).draggable(false).title(locationList.get(finalPos).getEmail()).icon(icon).snippet(locationList.get(finalPos).getFriend_first_name()).visible(true));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onBitmapFailed(Drawable arg0) {
                            return;
                        }
                    };
                    Picasso.with(MyApplication.getCurrentActivityContext())
                            .load(locationList.get(i).getFriend_profile())
                            .into(target);
                    isPointsAddedInBuilder = true;
                    builder.include(latLng);

                }
            }
            if(isFirstTime && isPointsAddedInBuilder) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 48));
            }
            if(!isPointsAddedInBuilder){
                MyApplication.getInstance().showToast(MyApplication.getCurrentActivityContext().getString(R.string.no_friends_shring_location_with_you));
            }
        }else {
            MyApplication.getInstance().showToast(MyApplication.getCurrentActivityContext().getString(R.string.no_friends_shring_location_with_you));
        }
        //MyApplication.getInstance().hideProgressDialog();
    }
}
