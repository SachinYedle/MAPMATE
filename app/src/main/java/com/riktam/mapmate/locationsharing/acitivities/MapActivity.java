package com.riktam.mapmate.locationsharing.acitivities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.db.dao.Friends;
import com.riktam.mapmate.locationsharing.db.dao.UserLastKnownLocation;
import com.riktam.mapmate.locationsharing.db.operations.FriendsTableOperations;
import com.riktam.mapmate.locationsharing.db.operations.UserLastknownLocationOperations;
import com.riktam.mapmate.locationsharing.fragments.DrawerFragment;
import com.riktam.mapmate.locationsharing.mappers.DownloadLocationsDataMapper;
import com.riktam.mapmate.locationsharing.mappers.FriendsDataMapper;
import com.riktam.mapmate.locationsharing.mappers.LocationSharedFriendsMapper;
import com.riktam.mapmate.locationsharing.pojo.FriendsData;
import com.riktam.mapmate.locationsharing.responses.FriendsServiceResponse;
import com.riktam.mapmate.locationsharing.responses.LocationSharedFriendsResponse;
import com.riktam.mapmate.locationsharing.responses.UserLocationsResponse;
import com.riktam.mapmate.locationsharing.services.BackgroundLocationService;
import com.riktam.mapmate.locationsharing.utils.Constants;
import com.riktam.mapmate.locationsharing.utils.CustomLog;
import com.riktam.mapmate.locationsharing.utils.DrawRouteFunctionality;
import com.riktam.mapmate.locationsharing.utils.Navigator;
import com.riktam.mapmate.locationsharing.utils.SetFriendsMarkers;
import com.riktam.mapmate.locationsharing.utils.TimeInAgoFormat;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

import static com.riktam.mapmate.locationsharing.utils.Constants.LOCATION_INTERVAL;

public class MapActivity extends DrawerActivity implements View.OnClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLoadedCallback, OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener {

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private static long back_pressed;
    private static final String BROADCAST_ACTION = "android.location.PROVIDERS_CHANGED";
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private final static int INTERVAL = 1000 * 30; //1 minutes
    private boolean isFirsTime = true;
    private SmoothProgressBar mapProgressBar;
    private Handler mHandler;
    private FloatingActionButton floatingActionButton;
    private Double latitude;
    private Double longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        MyApplication.getInstance().setCurrentActivityContext(MapActivity.this);
        initializeVariables();
        setUpToolbar();
        mHandler = new Handler();

        new FriendsDataMapper().getFriends(onGetFriendsDataListener, true);

        if (ActivityCompat.checkSelfPermission(MyApplication.getCurrentActivityContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getMyLocation();
        } else {
            finish();
            MyApplication.getInstance().showToast(getString(R.string.please_grant_location_permissions));
        }
    }


    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setDrawerLayout(MyApplication.getCurrentActivityContext());
        getSupportActionBar().setTitle(getString(R.string.app_name));

    }

    public void initializeVariables() {
        MyApplication.getInstance().showProgressDialog(getString(R.string.loading_data), getString(R.string.please_wait));
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.map_add_friend_floatingActionButton);
        floatingActionButton.setOnClickListener(this);

        mapProgressBar = (SmoothProgressBar) findViewById(R.id.map_progressBar);

        DrawerFragment fragment = new DrawerFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.drawer_fragment_layout, fragment);
        fragmentTransaction.addToBackStack("DrawerLayout");
        fragmentTransaction.commit();
    }

    public void getMyLocation() {
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected void stopRefresh() {
        mapProgressBar.setVisibility(View.GONE);
    }

    protected void startRefresh() {
        mapProgressBar.setVisibility(View.VISIBLE);
    }

    void startRepeatingTask() {
        mHandlerTask.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mHandlerTask);
    }

    Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            startRefresh();
            refresh();
            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };

    public void refresh() {
        new LocationSharedFriendsMapper().getLocationSharedFriends(onGetSharedLocationFriends, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map_menu_item_search:
                Navigator.getInstance().navigateToFriendsActivity("addFriend");
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    FriendsDataMapper.OnTaskCompletedListener onGetFriendsDataListener = new FriendsDataMapper.OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted(FriendsServiceResponse friendsServiceResponse) {
            stopRefresh();
        }

        @Override
        public void onTaskFailed(String response) {
            MyApplication.getInstance().showToast(response);
            stopRefresh();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().setCurrentActivityContext(MapActivity.this);
        startRepeatingTask();
        registerReceiver(gpsLocationReceiver, new IntentFilter(BROADCAST_ACTION));//Register broadcast receiver to check the status of GPS
    }

    @Override
    protected void onStop() {
        if (googleApiClient != null) {
            //LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
        stopRepeatingTask();
        super.onStop();
    }

    public void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

        createLocationRequest();
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_INTERVAL / 2);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    public void getUserLocations(Marker marker) {
        String email = null;
        if (marker.getTitle() != null) {
            String[] tokenArrray = marker.getTitle().split(" ");
            email = tokenArrray[tokenArrray.length - 1];
        }
        MyApplication.getInstance().sharedPreferencesData.setSelectedUserEmail(email);
        Navigator.getInstance().navigateToFriendsRouteActivity(email, "map");
        finish();
    }

    private void setFriendsMarker() {
        googleMap.setOnInfoWindowClickListener(this);
        SetFriendsMarkers.getInstance().setFriendsLocationMarkers(googleMap, isFirsTime);
        isFirsTime = false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String email = null;
        if (marker.getTitle() != null) {
            String[] tokenArrray = marker.getTitle().split(" ");
            email = tokenArrray[tokenArrray.length - 1];
        }

        final List<UserLastKnownLocation> userData = UserLastknownLocationOperations.getInstance()
                .getUserLastKnownLocation(email);
        GoogleMap.InfoWindowAdapter infoWindowAdapter = new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View view = getLayoutInflater().inflate(R.layout.map_marker_info_window, null);
                TextView userId = (TextView) view.findViewById(R.id.user_id);
                TextView userPhone = (TextView) view.findViewById(R.id.user_phone);
                if (userData.size() > 0) {
                    userId.setText(userData.get(0).getFriend_first_name());
                    String dateString = userData.get(0).getLast_known_time();
                    userPhone.setText(TimeInAgoFormat.getInstance().timeInAgoFormat(dateString));
                } else {
                    userId.setText(MyApplication.getInstance().sharedPreferencesData.getFirstName());
                    userPhone.setText(MyApplication.getInstance().sharedPreferencesData.getEmail());
                }
                return view;
            }
        };
        googleMap.setInfoWindowAdapter(infoWindowAdapter);
        return false;
    }

    @Override
    public void onBackPressed() {
        if (isDrawerOpen()) {
            closeDrawer();
        } else if (back_pressed + 2000 > System.currentTimeMillis()) {
            finish();
        } else {
            MyApplication.getInstance().showToast(getResources().getString(R.string.press_once_again_to_exit));
            back_pressed = System.currentTimeMillis();
        }
    }

    @Override
    public void onMapLoaded() {
        buildGoogleApiClient();
        MyApplication.getInstance().hideProgressDialog();
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);

        new LocationSharedFriendsMapper().getLocationSharedFriends(onGetSharedLocationFriends, true);
    }

    LocationSharedFriendsMapper.OnTaskCompletedListener onGetSharedLocationFriends = new LocationSharedFriendsMapper.OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted(LocationSharedFriendsResponse friendsServiceResponse) {
            setFriendsMarker();
            stopRefresh();
        }

        @Override
        public void onTaskFailed(String response) {
            MyApplication.getInstance().showToast(response);
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(MyApplication.getCurrentActivityContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            buildGoogleApiClient();
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMapLoadedCallback(this);

        } else {
            finish();
            MyApplication.getInstance().showToast(getResources().getString(R.string.please_grant_location_permissions));
        }
    }

    /* Broadcast receiver to check status of GPS */
    private BroadcastReceiver gpsLocationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches(BROADCAST_ACTION)) {
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    CustomLog.e("About GPS", "GPS is Enabled in your device");
                } else {
                    showLocationSettingDialog();
                    CustomLog.e("About GPS", "GPS is Disabled in your device");
                }
            }
        }
    };


    /* Show FriendLocation Access Dialog */
    private void showLocationSettingDialog() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);//Setting priotity of FriendLocation request to high
        locationRequest.setInterval(Constants.LOCATION_INTERVAL / 2);
        locationRequest.setFastestInterval(Constants.LOCATION_INTERVAL);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        //builder.setAlwaysShow(true); //this is the key ingredient to show dialog always when GPS is off

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(locationRequestCallback);
    }

    private ResultCallback<LocationSettingsResult> locationRequestCallback = new
            ResultCallback<LocationSettingsResult>() {

                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                            callToBackgroundLocationService();
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // FriendLocation settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(MapActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // FriendLocation settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            };

    public void callToBackgroundLocationService() {
        if (ActivityCompat.checkSelfPermission(MyApplication.getCurrentActivityContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startService(new Intent(this, BackgroundLocationService.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        CustomLog.e("Settings", "Result OK");
                        break;
                    case RESULT_CANCELED:
                        finish();
                        CustomLog.e("Settings", "Result Cancel");
                        break;
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gpsLocationReceiver != null)
            unregisterReceiver(gpsLocationReceiver);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (!(marker.getTitle().contains(getString(R.string.ago)) || marker.getTitle().contains(getString(R.string.now)))) {
            getUserLocations(marker);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.map_add_friend_floatingActionButton:
                Navigator.getInstance().navigateToFriendsActivity("add friend");
                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        showLocationSettingDialog();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        setMapZoomLevel();
    }

    private void setMapZoomLevel() {
        List<UserLastKnownLocation> locationList = UserLastknownLocationOperations
                .getInstance().getUserLastKnownLocation();
        ArrayList<LatLng> locations = getUsersInsideTheRadius(locationList);

        if (locationList.size() > locations.size() && locations.size() > 0) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (int i = 0; i < locations.size(); i++) {
                builder.include(locations.get(i));
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 30));
        }
    }

    private ArrayList<LatLng> getUsersInsideTheRadius(List<UserLastKnownLocation> locationList) {

        ArrayList<LatLng> locations = new ArrayList<>();

        for (int i = 0; i < locationList.size(); i++) {
            if(((locationList.get(i).getLatitude() != null && !locationList.get(i).getLatitude().equalsIgnoreCase(""))
                    && (locationList.get(i).getLongitude() != null && !locationList.get(i).getLongitude().equalsIgnoreCase("")))) {

                Double latitude2 = Double.parseDouble(locationList.get(i).getLatitude());
                Double longitude2 = Double.parseDouble(locationList.get(i).getLongitude());
                if (distance(latitude, longitude, latitude2, longitude2) < 25) {
                    locations.add(new LatLng(latitude2, longitude2));
                }
            }
        }
        return locations;
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344; // for KM
        return (dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

}