package com.example.admin1.locationsharing.acitivities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.UserLastKnownLocation;
import com.example.admin1.locationsharing.db.dao.UserLocations;
import com.example.admin1.locationsharing.db.dao.operations.UserLastknownLocationOperations;
import com.example.admin1.locationsharing.db.dao.operations.UserLocationsOperations;
import com.example.admin1.locationsharing.fragments.DrawerFragment;
import com.example.admin1.locationsharing.interfaces.PositiveClick;
import com.example.admin1.locationsharing.mappers.DownloadLocationsDataMapper;
import com.example.admin1.locationsharing.mappers.FriendsDataMapper;
import com.example.admin1.locationsharing.mappers.UploadLocationsDataMapper;
import com.example.admin1.locationsharing.mappers.UserDataMapper;
import com.example.admin1.locationsharing.responses.FriendsServiceResponse;
import com.example.admin1.locationsharing.responses.LocationSendingResponse;
import com.example.admin1.locationsharing.responses.UserLocationsResponse;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.Navigator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends DrawerActivity implements GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMarkerClickListener,LocationSource,GoogleMap.OnMapLoadedCallback,OnMapReadyCallback {

    private GoogleApiClient googleApiClient;
    private Location location;
    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private LocationRequest locationRequest;
    private SharedPreferencesData sharedPreferencesData;
    private static long back_pressed;
    private OnLocationChangedListener mMapLocationListener = null;
    private static final String BROADCAST_ACTION = "android.location.PROVIDERS_CHANGED";
    private static final int REQUEST_CHECK_SETTINGS = 0x1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        MyApplication.getInstance().setCurrentActivityContext(MapActivity.this);
        initializeVariables();

        if(ActivityCompat.checkSelfPermission(MyApplication.getCurrentActivityContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getMyLocation();
        }else {
            Toast.makeText(MyApplication.getCurrentActivityContext(),"Please grant Location permissions",Toast.LENGTH_SHORT).show();
        }
        /** get friends data */
        new FriendsDataMapper().getFriends(onGetFriendsDataListener);
    }

    public void initializeVariables(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MyApplication.getInstance().showProgressDialog(getString(R.string.loading_data),getString(R.string.please_wait));
        setDrawerLayout(MyApplication.getCurrentActivityContext());
        sharedPreferencesData = new SharedPreferencesData(MyApplication.getCurrentActivityContext());
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        DrawerFragment fragment = new DrawerFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.drawer_fragment_layout,fragment);
        fragmentTransaction.addToBackStack("DrawerLayout");
        fragmentTransaction.commit();
    }
    public void getMyLocation() {
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.add:
                Navigator.getInstance().navigateToFriendsActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    FriendsDataMapper.OnTaskCompletedListener onGetFriendsDataListener = new FriendsDataMapper.OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted(FriendsServiceResponse friendsServiceResponse) {
            Toast.makeText(MyApplication.getCurrentActivityContext(),"Friends data Addeed: " +
                    friendsServiceResponse.isSuccess(),Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onTaskFailed(String response) {
            Toast.makeText(MyApplication.getCurrentActivityContext(),"Getting Friends data : "+response,Toast.LENGTH_SHORT).show();
        }
    };
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationManager locationManager = (LocationManager) MyApplication.getCurrentActivityContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            CustomLog.e("About GPS", "GPS is Enabled in your device");
        } else {
            //If GPS turned OFF show Location Dialog
            showLocationSettingDialog();
            CustomLog.e("About GPS", "GPS is Disabled in your device");
        }
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    public void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Map Activity", "onConnection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        CustomLog.i("Map Activity", "onConnection failed");
    }


    @Override
    public void onLocationChanged(Location mlocation) {
        MyApplication.getInstance().hideProgressDialog();
        if(mMapLocationListener!=null){
            mMapLocationListener.onLocationChanged(mlocation);
        }
        location = mlocation;
        CustomLog.d("MapActivity","Radius"+location.getAccuracy());
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        /*googleMap.addMarker(new MarkerOptions().position(latLng)
                .draggable(false));
        */
    }

    private DownloadLocationsDataMapper.OnTaskCompletedListener onTaskCompletedListener = new DownloadLocationsDataMapper.OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted(UserLocationsResponse userLocationsResponse) {
            SharedPreferencesData preferencesData = new SharedPreferencesData(MyApplication.getCurrentActivityContext());
            String token = preferencesData.getSelectedUserEmail();
            CustomLog.i("Map Activity","Download Locations"+userLocationsResponse.getSuccess()+":"+token);
            drawRouteOfSelectedUser(token);
        }

        @Override
        public void onTaskFailed(String response) {
            Toast.makeText(MyApplication.getCurrentActivityContext(),"Location download Failed "+response,Toast.LENGTH_SHORT).show();
        }
    };
    public void getUserLocations(Marker marker){
        String token = null;
        if(marker.getTitle() !=null ){
            String [] tokenArrray = marker.getTitle().split(" ");
            token = tokenArrray[tokenArrray.length -1];
        }

        SharedPreferencesData preferencesData = new SharedPreferencesData(MyApplication.getCurrentActivityContext());
        preferencesData.setSelectedUserEmail(token);

        CustomLog.d("Marker id","ID:"+ token);
        DownloadLocationsDataMapper dataMapper = new DownloadLocationsDataMapper(MyApplication.getCurrentActivityContext());

        dataMapper.getLocations(onTaskCompletedListener,token);
    }

    public void setFriendsLocationMarkers(){
        MyApplication.getInstance().hideProgressDialog();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        List<UserLastKnownLocation> locationList = UserLastknownLocationOperations.getUserLastKnownLocation(MyApplication.getCurrentActivityContext());
        if(locationList.size() >= 0){
            for (int i=0;i<locationList.size();i++){
                Double latitude = Double.parseDouble(locationList.get(i).getLatitude());
                Double longitude = Double.parseDouble(locationList.get(i).getLongitude());

                LatLng latLng = new LatLng(latitude, longitude);
                googleMap.addMarker(new MarkerOptions().position(latLng)
                        .draggable(false).title(""+locationList.get(i).getToken()));
                builder.include(latLng);
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 48));
        }
    }

    public void drawRouteOfSelectedUser(String token){
        googleMap.clear();

        CustomLog.i("Map Activity","draw route");
        ArrayList<LatLng> points = new ArrayList<LatLng>();
        PolylineOptions lineOptions = new PolylineOptions();
        List<UserLocations> locations = UserLocationsOperations.getUserLocations(MyApplication.getCurrentActivityContext(),token);
        CustomLog.i("Map Activity","draw route size"+locations.size());

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < locations.size(); i++) {
            double lat = Double.parseDouble(locations.get(i).getLatitude());
            double lng = Double.parseDouble(locations.get(i).getLongitude());
            LatLng position = new LatLng(lat, lng);
            CustomLog.i("Data",lat+" "+lng+" "+locations.get(i).getToken());
            builder.include(position);
            points.add(position);
        }
        if(points.size() > 0){
            lineOptions.addAll(points);
            lineOptions.width(7);
            lineOptions.color(Color.RED);
            googleMap.addPolyline(lineOptions);
            double lat = Double.parseDouble(locations.get(locations.size()-1).getLatitude());
            double lng = Double.parseDouble(locations.get(locations.size()-1).getLongitude());
            LatLng position = new LatLng(lat, lng);
            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title("Start")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
            lat = Double.parseDouble(locations.get(0).getLatitude());
            lng = Double.parseDouble(locations.get(0).getLongitude());
            position = new LatLng(lat, lng);
            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title("End")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 48));
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        String latitude = ""+marker.getPosition().latitude;
        String  longitude = ""+marker.getPosition().longitude;

        final List<UserLastKnownLocation> userData = UserLastknownLocationOperations
                .getUserWithLatLng(MyApplication.getCurrentActivityContext(), latitude,longitude);

        GoogleMap.InfoWindowAdapter infoWindowAdapter = new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                CustomLog.d("OnMarkerClick","marker click"+marker.getTitle());
                View view = getLayoutInflater().inflate(R.layout.map_marker_info_window,null);
                TextView userId = (TextView)view.findViewById(R.id.user_id);
                TextView userPhone = (TextView)view.findViewById(R.id.user_phone);
                if(userData.size() > 0){
                    userId.setText(userData.get(0).getToken());
                    userPhone.setText(userData.get(0).getName());
                }
                else {
                    userId.setText( sharedPreferencesData.getFirstName());
                    userPhone.setText(sharedPreferencesData.getEmail());
                }
                return view;
            }
        };
        googleMap.setInfoWindowAdapter(infoWindowAdapter);
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            if (back_pressed + 2000 > System.currentTimeMillis()){
                super.onBackPressed();
            }
            else {
                finish();
                Navigator.getInstance().navigateToMapActivity();
                Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_SHORT).show();
                back_pressed = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mMapLocationListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mMapLocationListener = null;
    }

    @Override
    public void onMapLoaded() {
        MyApplication.getInstance().hideProgressDialog();
        googleMap.setOnMarkerClickListener(this);
        //setFriendsLocationMarkers();
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                CustomLog.d("Click","InfoWindow Click");
                getUserLocations(marker);
            }
        });
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (googleMap == null) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        if (ActivityCompat.checkSelfPermission(MyApplication.getCurrentActivityContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMapLoadedCallback(this);
        }else {
            Toast.makeText(MyApplication.getCurrentActivityContext(),"Please grant Location permissions",Toast.LENGTH_SHORT).show();
        }
    }

    /* Broadcast receiver to check status of GPS */
    private BroadcastReceiver gpsLocationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            //If Action is Location
            if (intent.getAction().matches(BROADCAST_ACTION)) {
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                //Check if GPS/locations is turned ON or OFF
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    CustomLog.e("About GPS", "GPS is Enabled in your device");
                } else {
                    //If GPS turned OFF show Location Dialog
                    showLocationSettingDialog();
                    CustomLog.e("About GPS", "GPS is Disabled in your device");
                }
            }
        }
    };

    /* Show Location Access Dialog */
    private void showLocationSettingDialog() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);//Setting priotity of Location request to high
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);//5 sec Time interval for location update
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
                        // Location settings are not satisfied. But could be fixed by showing the user
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
                        // Location settings are not satisfied. However, we have no way to fix the
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
}