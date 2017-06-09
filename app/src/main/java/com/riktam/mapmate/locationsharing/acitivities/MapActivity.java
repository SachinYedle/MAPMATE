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
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.db.dao.UserLastKnownLocation;
import com.riktam.mapmate.locationsharing.db.operations.FriendsTableOperations;
import com.riktam.mapmate.locationsharing.db.operations.UserLastknownLocationOperations;
import com.riktam.mapmate.locationsharing.fragments.DrawerFragment;
import com.riktam.mapmate.locationsharing.mappers.DownloadLocationsDataMapper;
import com.riktam.mapmate.locationsharing.mappers.FriendsDataMapper;
import com.riktam.mapmate.locationsharing.mappers.LocationSharedFriendsMapper;
import com.riktam.mapmate.locationsharing.responses.FriendsServiceResponse;
import com.riktam.mapmate.locationsharing.responses.LocationSharedFriendsResponse;
import com.riktam.mapmate.locationsharing.responses.UserLocationsResponse;
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

import java.util.List;

public class MapActivity extends DrawerActivity implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLoadedCallback, OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener {

    private GoogleApiClient googleApiClient;
    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private static long back_pressed;
    private static final int MENU_REFRESH = 1;
    protected MenuItem refreshItem = null;
    private static final String BROADCAST_ACTION = "android.location.PROVIDERS_CHANGED";
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private Menu menu;
    private final static int INTERVAL = 1000 * 60; //2 minutes
    private boolean isFirsTime = true;
    private boolean isZoomLevelset = false;
    Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        MyApplication.getInstance().setCurrentActivityContext(MapActivity.this);

        initializeVariables();
        getLocationAndSetZoomLevel();

        new FriendsDataMapper().getFriends(onGetFriendsDataListener,true);

        if (ActivityCompat.checkSelfPermission(MyApplication.getCurrentActivityContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getMyLocation();
        } else {
            finish();
            MyApplication.getInstance().showToast(getString(R.string.please_grant_location_permissions));
        }

    }

    private void drawRoute(String email) {
        stopRepeatingTask();
        if (refreshItem != null) {
            refreshItem.setVisible(false);
        }
        DrawRouteFunctionality.getInstance().setRouteVisible(true);
        DownloadLocationsDataMapper dataMapper = new DownloadLocationsDataMapper(MyApplication.getCurrentActivityContext());
        dataMapper.getLocations(onTaskCompletedListener, FriendsTableOperations.getInstance().getFriendId(email));
    }

    public void initializeVariables() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MyApplication.getInstance().showProgressDialog(getString(R.string.loading_data), getString(R.string.please_wait));
        setDrawerLayout(MyApplication.getCurrentActivityContext());
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        DrawerFragment fragment = new DrawerFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.drawer_fragment_layout, fragment);
        fragmentTransaction.addToBackStack("DrawerLayout");
        fragmentTransaction.commit();


    }

    private void getLocationAndSetZoomLevel(){
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    LatLng position = new LatLng(location.getLatitude(),location.getLongitude());
                    builder.include(position);
                    if(googleMap !=null && !isZoomLevelset && !DrawRouteFunctionality.getInstance().isRouteVisible()) {
                        isZoomLevelset = true;
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 48));
                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 200, null);
                    }
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }

    }
    public void getMyLocation() {
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_toolbar_menu, menu);
        menu.add(Menu.NONE, MENU_REFRESH, Menu.NONE, "Refresh data")
                .setIcon(R.drawable.ic_action_refresh)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        setRefreshItem(menu.findItem(MENU_REFRESH));
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    protected void setRefreshItem(MenuItem item) {
        refreshItem = item;
    }

    protected void stopRefresh() {
        if (refreshItem != null) {
            refreshItem.setVisible(false);
            refreshItem.setActionView(null);
        }
    }

    protected void startRefresh() {
        if (refreshItem != null) {
            refreshItem.setVisible(true);
            refreshItem.setActionView(R.layout.indeterminate_progress_action);
        }
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
            new LocationSharedFriendsMapper().getLocationSharedFriends(onGetSharedLocationFriends, true);
            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map_menu_item_add:
                Navigator.getInstance().navigateToFriendsActivity();
                finish();
                break;
            case R.id.map_menu_item_my_location:
                drawRoute(MyApplication.getInstance().sharedPreferencesData.getEmail());
//                DrawRouteFunctionality.getInstance().setRouteVisible(true);
//                DownloadLocationsDataMapper dataMapper = new DownloadLocationsDataMapper(MyApplication.getCurrentActivityContext());
//                dataMapper.getLocations(onTaskCompletedListener, MyApplication.getInstance().sharedPreferencesData.getId() + "");
                break;
//            case R.id.map_menu_item_refresh:
//                new FriendsDataMapper().getFriends(onGetFriendsDataListener,false);
//                break;
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
        if (!DrawRouteFunctionality.getInstance().isRouteVisible()) {
            startRepeatingTask();
        }
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
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    private DownloadLocationsDataMapper.OnTaskCompletedListener onTaskCompletedListener = new DownloadLocationsDataMapper.OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted(UserLocationsResponse userLocationsResponse) {
            String email = MyApplication.getInstance().sharedPreferencesData.getSelectedUserEmail();
            CustomLog.i("Map Activity", "Download Locations" + userLocationsResponse.getSuccess() + ":" + email);
            googleMap.setOnInfoWindowClickListener(null);
            DrawRouteFunctionality.getInstance().drawRouteOfSelectedUser(email, googleMap, menu);
        }

        @Override
        public void onTaskFailed(String response) {
            MyApplication.getInstance().showToast(response);
        }
    };

    public void getUserLocations(Marker marker) {
        String email = null;
        if (marker.getTitle() != null) {
            String[] tokenArrray = marker.getTitle().split(" ");
            email = tokenArrray[tokenArrray.length - 1];
        }
        DrawRouteFunctionality.getInstance().setRouteVisible(true);
        MyApplication.getInstance().sharedPreferencesData.setSelectedUserEmail(email);
        CustomLog.d("Marker id", "Email:" + email);
        drawRoute(email);
    }

    private void setFriendsMarker() {
        googleMap.setOnInfoWindowClickListener(this);
        SetFriendsMarkers.getInstance().setFriendsLocationMarkers(googleMap, menu, isFirsTime);
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
                if (DrawRouteFunctionality.getInstance().isRouteVisible()) {
                    return null;
                } else {
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
            }
        };
        googleMap.setInfoWindowAdapter(infoWindowAdapter);
        return false;
    }

    @Override
    public void onBackPressed() {
        if (DrawRouteFunctionality.getInstance().isRouteVisible()) {
            setFriendsMarker();
        }else if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            MyApplication.getInstance().showToast(getResources().getString(R.string.press_once_again_to_exit));
            back_pressed = System.currentTimeMillis();
        }
    }

    @Override
    public void onMapLoaded() {
        MyApplication.getInstance().hideProgressDialog();
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String email = MyApplication.getInstance().sharedPreferencesData.getSelectedUserEmail();
            googleMap.setOnInfoWindowClickListener(null);
            DrawRouteFunctionality.getInstance().drawRouteOfSelectedUser(email, googleMap, menu);
        } else {
            /** get friends data */
            DrawRouteFunctionality.getInstance().setRouteVisible(false);
            new LocationSharedFriendsMapper().getLocationSharedFriends(onGetSharedLocationFriends, true);
        }
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
        builder.setAlwaysShow(true); //this is the key ingredient to show dialog always when GPS is off

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
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
        });
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
}