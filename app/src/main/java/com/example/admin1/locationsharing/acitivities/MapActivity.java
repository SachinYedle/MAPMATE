package com.example.admin1.locationsharing.acitivities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.UserLastKnownLocation;
import com.example.admin1.locationsharing.db.operations.FriendsTableOperations;
import com.example.admin1.locationsharing.db.operations.UserLastknownLocationOperations;
import com.example.admin1.locationsharing.fragments.DrawerFragment;
import com.example.admin1.locationsharing.mappers.DownloadLocationsDataMapper;
import com.example.admin1.locationsharing.mappers.FriendsDataMapper;
import com.example.admin1.locationsharing.responses.FriendsServiceResponse;
import com.example.admin1.locationsharing.responses.UserLocationsResponse;
import com.example.admin1.locationsharing.utils.Constants;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.DrawRouteFunctionality;
import com.example.admin1.locationsharing.utils.Navigator;
import com.example.admin1.locationsharing.utils.SetFriendsMarkers;
import com.example.admin1.locationsharing.utils.TimeInAgoFormat;
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
import com.example.admin1.locationsharing.utils.SharedPreferencesData;

import java.util.List;

public class MapActivity extends DrawerActivity implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLoadedCallback, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private GoogleApiClient googleApiClient;
    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private static long back_pressed;
    private static final String BROADCAST_ACTION = "android.location.PROVIDERS_CHANGED";
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private Menu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        MyApplication.getInstance().setCurrentActivityContext(MapActivity.this);
        initializeVariables();

        if (ActivityCompat.checkSelfPermission(MyApplication.getCurrentActivityContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getMyLocation();
        } else {
            finish();
            MyApplication.getInstance().showToast("Please grant Location permissions");
        }
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String email = bundle.getString("email");
            DownloadLocationsDataMapper dataMapper = new DownloadLocationsDataMapper(MyApplication.getCurrentActivityContext());
            dataMapper.getLocations(onTaskCompletedListener, FriendsTableOperations.getInstance().getFriendId(email));
        } else {
            /** get friends data */
            new FriendsDataMapper().getFriends(onGetFriendsDataListener);
        }
    }

    public void initializeVariables() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("KnowWhere");
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

    public void getMyLocation() {
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map_menu_item_add:
                Navigator.getInstance().navigateToFriendsActivity();
                finish();
                break;
            case R.id.map_menu_item_my_location:
                DownloadLocationsDataMapper dataMapper = new DownloadLocationsDataMapper(MyApplication.getCurrentActivityContext());
                dataMapper.getLocations(onTaskCompletedListener, MyApplication.getInstance().sharedPreferencesData.getId() + "");
        }
        return super.onOptionsItemSelected(item);
    }


    FriendsDataMapper.OnTaskCompletedListener onGetFriendsDataListener = new FriendsDataMapper.OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted(FriendsServiceResponse friendsServiceResponse) {
            setFriendsMarker();
        }

        @Override
        public void onTaskFailed(String response) {
            MyApplication.getInstance().showToast("Getting Friends data : " + response);
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
        registerReceiver(gpsLocationReceiver, new IntentFilter(BROADCAST_ACTION));//Register broadcast receiver to check the status of GPS
    }

    @Override
    protected void onStop() {
        if (googleApiClient != null) {
            //LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
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
            DrawRouteFunctionality.getInstance().drawRouteOfSelectedUser(email,googleMap,menu);
        }

        @Override
        public void onTaskFailed(String response) {
            MyApplication.getInstance().showToast("FriendLocation download Failed " + response);
        }
    };

    public void getUserLocations(Marker marker) {
        String email = null;
        if (marker.getTitle() != null) {
            String[] tokenArrray = marker.getTitle().split(" ");
            email = tokenArrray[tokenArrray.length - 1];
        }
        MyApplication.getInstance().sharedPreferencesData.setSelectedUserEmail(email);
        CustomLog.d("Marker id", "Email:" + email);
        DownloadLocationsDataMapper dataMapper = new DownloadLocationsDataMapper(MyApplication.getCurrentActivityContext());
        dataMapper.getLocations(onTaskCompletedListener, FriendsTableOperations.getInstance().getFriendId(email));
    }

    private void setFriendsMarker(){
        SetFriendsMarkers.getInstance().setFriendsLocationMarkers(googleMap,menu,this);
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
                        String dateString = userData.get(0).getTime();
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
        }
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            MyApplication.getInstance().showToast("Press once again to exit!");
            back_pressed = System.currentTimeMillis();
        }
    }

    @Override
    public void onMapLoaded() {
        MyApplication.getInstance().hideProgressDialog();
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);
    }

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
            MyApplication.getInstance().showToast("Please grant FriendLocation permissions");
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
        locationRequest.setInterval(Constants.LOCATION_INTERVAL/2);
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
        getUserLocations(marker);
    }
}