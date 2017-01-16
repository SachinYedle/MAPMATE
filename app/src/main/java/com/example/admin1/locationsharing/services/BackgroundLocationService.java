package com.example.admin1.locationsharing.services;

/**
 * Created by Sachin on 20/12/16.
 */

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.content.Context;
import android.location.LocationManager;
import android.widget.Toast;

import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.mappers.UploadLocationsDataMapper;
import com.example.admin1.locationsharing.responses.LocationSendingResponse;
import com.example.admin1.locationsharing.utils.CustomLog;

import static com.example.admin1.locationsharing.utils.Constants.DISTANCE_THRESHOLD;
import static com.example.admin1.locationsharing.utils.Constants.LOCATION_DISTANCE;
import static com.example.admin1.locationsharing.utils.Constants.LOCATION_INTERVAL;

/**
 * BackgroundLocationService used for tracking user location in the background.
 *
 * @author cblack
 */
public class BackgroundLocationService extends Service implements LocationListener {
    private static final String TAG = "LocationSer";
    private LocationManager mLocationManager = null;
    private Location oldLocation = null;
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CustomLog.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        initializeLocationManager();
        requestLocationUpdates(LOCATION_INTERVAL,LOCATION_DISTANCE);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        CustomLog.e(TAG, "onCreate");
    }

    private void requestLocationUpdates(int locationTimeInterval, float locationDistance){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            CustomLog.d(TAG,"Location Request:"+getProviderName());

            mLocationManager.requestLocationUpdates(getProviderName(), locationTimeInterval,
                    locationDistance, this);

        }else {
            Toast.makeText(MyApplication.getCurrentActivityContext(),"Please grant Location permissions",Toast.LENGTH_SHORT).show();
        }
    }

    String getProviderName() {
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW); // Chose your desired power consumption level.
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // Choose your accuracy requirement.
        criteria.setSpeedRequired(true); // Chose if speed for first location fix is required.
        criteria.setAltitudeRequired(false); // Choose if you use altitude.
        criteria.setBearingRequired(false); // Choose if you use bearing.
        criteria.setCostAllowed(false); // Choose if this provider can waste money :-)

        // Provide your criteria and flag enabledOnly that tells
        // LocationManager only to return active providers.
        return locationManager.getBestProvider(criteria, true);
    }

    @Override
    public void onDestroy() {
        CustomLog.d(TAG,"OnDestroy");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.removeUpdates(this);
        }else {
            Toast.makeText(MyApplication.getCurrentActivityContext(),"Please grant Location permissions",Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeLocationManager() {
        CustomLog.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        CustomLog.e(TAG, "onLocationChanged: " + location.getLatitude() + " " + location.getLongitude() + " " + location.getAccuracy());
        //if(isBetterLocation(oldLocation,location)){
            //Toast.makeText(MyApplication.getCurrentActivityContext(),"onLocationChanged: " + location.getLatitude() + " " + location.getLongitude() + " " + location.getAccuracy(),Toast.LENGTH_SHORT).show();
            UploadLocationsDataMapper uploadLocationsDataMapper = new UploadLocationsDataMapper(MyApplication.getCurrentActivityContext());
            uploadLocationsDataMapper.sendUserLocation(onTaskCompletedListener, "" + location.getLatitude(), "" + location.getLongitude(), location.getAccuracy() + "");
            oldLocation = location;
        //}
    }

    @Override
    public void onStatusChanged(String provider, int i, Bundle bundle) {
        CustomLog.e(TAG, "onStatusChanged: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        CustomLog.e(TAG, "onProviderEnabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        CustomLog.e(TAG, "onProviderDisabled: " + provider);
    }

    private UploadLocationsDataMapper.OnTaskCompletedListener onTaskCompletedListener = new UploadLocationsDataMapper.OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted(LocationSendingResponse locationSendingResponse) {
            CustomLog.i(TAG,"Location updated");
        }

        @Override
        public void onTaskFailed(String response) {
            CustomLog.i(TAG,"Location sending error"+response);
        }
    };

    /**
     * Time difference threshold set for Three minute.
     */
        //static final int TIME_DIFFERENCE_THRESHOLD = 3 * 60 * 1000;

    /**
     * Decide if new location is better than older by following some basic criteria.
     * This algorithm can be as simple or complicated as your needs dictate it.
     * Try experimenting and get your best location strategy algorithm.
     *
     * @param oldLocation Old location used for comparison.
     * @param newLocation Newly acquired location compared to old one.
     * @return If new location is more accurate and suits your criteria more than the old one.
     */
    boolean isBetterLocation(Location oldLocation, Location newLocation) {
        // If there is no old location, of course the new location is better.
        if(oldLocation == null) {
            CustomLog.d(TAG,"Old location null");
            return true;
        }

        // check for distance change if it is more than interval distance
        float distance = oldLocation.distanceTo(newLocation);
        CustomLog.d(TAG,"distance: "+distance);
        //Toast.makeText(MyApplication.getCurrentActivityContext(),"Distance: "+distance,Toast.LENGTH_SHORT).show();
        // Check if new location is newer in time.
        boolean isNewer = newLocation.getTime() > oldLocation.getTime();

        if(isNewer && distance > DISTANCE_THRESHOLD){
            return true;
        }
        return false;
    }
}