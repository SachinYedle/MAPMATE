package com.example.admin1.locationsharing.services;

/**
 * Created by Sachin on 20/12/16.
 */

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.interfaces.PositiveClick;
import com.example.admin1.locationsharing.mappers.UploadLocationsDataMapper;
import com.example.admin1.locationsharing.responses.LocationSendingResponse;
import com.example.admin1.locationsharing.retrofitservices.LocationDataService;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.Navigator;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.admin1.locationsharing.utils.Constants.LOCATION_INTERVAL;

/**
 * BackgroundLocationService used for tracking user location in the background.
 *
 * @author cblack
 */
public class BackgroundLocationService extends Service implements LocationListener, ConnectionCallbacks {
    private static final String TAG = "LocationService";
    private LocationRequest locationRequest;
    private Location oldLocation = null;
    private GoogleApiClient googleApiClient;
    private SharedPreferencesData sharedPreferencesData;


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
        sharedPreferencesData = new SharedPreferencesData();
        buildGoogleApiClient();
        googleApiClient.connect();
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        CustomLog.i(TAG, "Building GoogleApiClient");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        CustomLog.i(TAG, "createlocationRequest");
        locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_INTERVAL / 2);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void startLocationUpdates() {
        CustomLog.i(TAG, "StartLocationUpdates");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    protected void stopLocationUpdates() {
        CustomLog.i(TAG, "StopLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    public void onDestroy() {
        CustomLog.i(TAG, "onDestroy");
        googleApiClient.disconnect();
        stopLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        CustomLog.e(TAG, "onLocationChanged: " + location.getLatitude() + " " + location.getLongitude() + " " + location.getAccuracy());
        UploadLocationsDataMapper uploadLocationsDataMapper = new UploadLocationsDataMapper(MyApplication.getCurrentActivityContext());
        uploadLocationsDataMapper.sendUserLocation("" + location.getLatitude(), "" + location.getLongitude(), location.getAccuracy() + "");
        oldLocation = location;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        CustomLog.i(TAG, "onConnected");
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }


    public class UploadLocationsDataMapper {
        private Context context;

        public UploadLocationsDataMapper(Context context) {
            this.context = context;
        }

        public void sendUserLocation( String lat, String lon, String radius) {

            String token = sharedPreferencesData.getUserToken();

            if (MyApplication.getInstance().isConnectedToInterNet()) {
                LocationDataService locationDataService = MyApplication.getInstance().getLocationDataService(token);
                Call<LocationSendingResponse> call = locationDataService.sendUserLocation(lat, lon, radius);
                call.enqueue(locationSendingResponseCallback);
            }
        }

        private Callback<LocationSendingResponse> locationSendingResponseCallback = new
                Callback<LocationSendingResponse>() {

                    @Override
                    public void onResponse(Call<LocationSendingResponse> call,
                                           Response<LocationSendingResponse>
                                                   response) {
                        if(response.isSuccessful()){
                            CustomLog.d("LocationService","Updated");
                        }else {
                            CustomLog.d("LocationService","NotUpdated");
                        }

                    }

                    @Override
                    public void onFailure(Call<LocationSendingResponse> call, Throwable t) {
                        CustomLog.d("LocationService","NotUpdated");
                    }
                };
    }
}