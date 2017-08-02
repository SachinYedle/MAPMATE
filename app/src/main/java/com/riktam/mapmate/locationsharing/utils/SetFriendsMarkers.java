package com.riktam.mapmate.locationsharing.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;
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

import java.util.ArrayList;
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

    public void setFriendsLocationMarkers(final GoogleMap googleMap, final boolean isFirstTime) {
        googleMap.clear();
        boolean isPointsAddedInBuilder = false;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        final List<UserLastKnownLocation> locationList = UserLastknownLocationOperations.getInstance().getUserLastKnownLocation();
        final ArrayList<Marker> markersList = new ArrayList<>();
        ArrayList<String> profileUrl = new ArrayList<>();
        if (locationList.size() > 0) {
            for (int i = 0; i < locationList.size(); i++) {
                if (((locationList.get(i).getLatitude() != null && !locationList.get(i).getLatitude().equalsIgnoreCase(""))
                        && (locationList.get(i).getLongitude() != null && !locationList.get(i).getLongitude().equalsIgnoreCase("")))) {
                    Double latitude = Double.parseDouble(locationList.get(i).getLatitude());
                    Double longitude = Double.parseDouble(locationList.get(i).getLongitude());
                    final LatLng latLng = new LatLng(latitude, longitude);
                    Bitmap dummyProfile = BitmapFactory.decodeResource(MyApplication.getCurrentActivityContext().getResources(), R.drawable.profile_icon);
                    Bitmap bitmapInflated = BitMapMerging.getInstance().inflateViewGetBitmap(dummyProfile);
                    Bitmap bitmapIcon = BitMapMerging.getInstance().mergeBitmap(bitmapInflated, BitmapFactory.decodeResource(MyApplication.getCurrentActivityContext().getResources(), R.drawable.map_marker_blue));
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmapIcon);
                    final Marker mapMarker = googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .icon(icon)
                            .draggable(false)
                            .title(locationList.get(i).getEmail())
                            .snippet(locationList.get(i).getFriend_first_name())
                            .visible(true));
                    markersList.add(mapMarker);
                    profileUrl.add(locationList.get(i).getFriend_profile());

                    isPointsAddedInBuilder = true;
                    builder.include(latLng);
                }
                setIconToMarkers(markersList, profileUrl);
            }

            if (isFirstTime && isPointsAddedInBuilder) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 30));
            }
            if (!isPointsAddedInBuilder) {
                MyApplication.getInstance().showToast(MyApplication.getCurrentActivityContext().getString(R.string.no_friend_is_sharing_location_with_you));
            }
        } else {
            MyApplication.getInstance().showToast(MyApplication.getCurrentActivityContext().getString(R.string.no_friend_is_sharing_location_with_you));
        }
    }

    private void setIconToMarkers(final ArrayList<Marker> markersList, ArrayList<String> profileUrl) {
        for (int i = 0; i < markersList.size(); i++) {
            final int POSITION = i;
            Picasso.with(MyApplication.getCurrentActivityContext())
                    .load(profileUrl.get(i))
                    .error(MyApplication.getCurrentActivityContext().getResources().getDrawable(
                            R.drawable.small_red_marker))
                    .into(new com.squareup.picasso.Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            Bitmap bitmapInflated = BitMapMerging.getInstance().inflateViewGetBitmap(bitmap);
                            Bitmap bitmapIcon = BitMapMerging.getInstance().mergeBitmap(bitmapInflated, BitmapFactory.decodeResource(MyApplication.getCurrentActivityContext().getResources(), R.drawable.map_marker_blue));
                            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmapIcon);
                            markersList.get(POSITION).setIcon(icon);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            Log.d("On bitmap dl failed", "bitmap dl failed");
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            Log.d("On bitmap dl failed", "bitmap dl failed");
                        }
                    });
        }
    }
}
