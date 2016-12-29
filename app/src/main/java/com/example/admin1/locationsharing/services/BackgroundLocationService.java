package com.example.admin1.locationsharing.services;

/**
 * Created by Sachin on 20/12/16.
 */

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.content.Context;
import android.location.LocationManager;

import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.mappers.UploadLocationsDataMapper;
import com.example.admin1.locationsharing.responses.LocationSendingResponse;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import static com.example.admin1.locationsharing.utils.Constants.LOCATION_DISTANCE;
import static com.example.admin1.locationsharing.utils.Constants.LOCATION_INTERVAL;

/**
 * BackgroundLocationService used for tracking user location in the background.
 *
 * @author cblack
 */
public class BackgroundLocationService extends Service {
    private static final String TAG = "LocationSer";
    private LocationManager mLocationManager = null;


    private class LocationListener implements android.location.LocationListener,UploadLocationsDataMapper.OnTaskCompletedListener,GoogleApiClient.OnConnectionFailedListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            CustomLog.d(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            CustomLog.e(TAG, "onLocationChanged: " + location.getLatitude() +" "+location.getLongitude()+" "+location.getAccuracy());
            UploadLocationsDataMapper uploadLocationsDataMapper = new UploadLocationsDataMapper(MyApplication.getCurrentActivityContext());
            uploadLocationsDataMapper.sendUserLocation(this,""+location.getLatitude(), ""+location.getLongitude(),location.getAccuracy()+"");
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            CustomLog.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            CustomLog.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            CustomLog.e(TAG, "onStatusChanged: " + provider);
        }

        @Override
        public void onTaskCompleted(LocationSendingResponse locationSendingResponse) {
            CustomLog.i(TAG,"Location updated");
        }

        @Override
        public void onTaskFailed(String response) {
            CustomLog.i(TAG,"Location sending error"+response);
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            CustomLog.e("ConnectionFailed",""+connectionResult.getErrorMessage());
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CustomLog.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        CustomLog.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            CustomLog.e(TAG, "fail to req location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            CustomLog.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            CustomLog.e(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            CustomLog.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        CustomLog.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    CustomLog.e(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        CustomLog.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}