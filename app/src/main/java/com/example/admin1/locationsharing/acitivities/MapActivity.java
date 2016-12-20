package com.example.admin1.locationsharing.acitivities;

import android.Manifest;
import android.graphics.Color;
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
import com.example.admin1.locationsharing.db.dao.operations.UsersLast30MinLocationsOperation;
import com.example.admin1.locationsharing.fragments.DrawerFragment;
import com.example.admin1.locationsharing.interfaces.PositiveClick;
import com.example.admin1.locationsharing.mappers.LocationDataMapper;
import com.example.admin1.locationsharing.mappers.UserDataMapper;
import com.example.admin1.locationsharing.responses.LocationSendingResponse;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.Navigator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends DrawerActivity implements GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMarkerClickListener,LocationSource {

    private GoogleApiClient googleApiClient;
    private Location location;
    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private LocationRequest locationRequest;
    private SharedPreferencesData sharedPreferencesData;
    private LatLng myLatLngLocation;
    private static long back_pressed;
    private OnLocationChangedListener mMapLocationListener = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        MyApplication.getInstance().setCurrentActivityContext(MapActivity.this);
        initializeVariables();

        if(ActivityCompat.checkSelfPermission(MyApplication.getCurrentActivityContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
            getMyLocation();

        }
    }


    public void initializeVariables(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

        if (!checkGpsStatus()) {
            Toast.makeText(this, "please enable gps location..", Toast.LENGTH_SHORT).show();
            return;
        }

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mgoogleMap) {

        googleMap = mgoogleMap;
                if (googleMap == null)
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                if (ActivityCompat.checkSelfPermission(MyApplication.getInstance().getCurrentActivityContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    buildGoogleApiClient();

                    googleMap.setMyLocationEnabled(true);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    public LatLng getMyLatLngLocation() {
        return myLatLngLocation;
    }

    public void setMyLatLngLocation(LatLng myLatLngLocation) {
        this.myLatLngLocation = myLatLngLocation;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.add:
                FragmentManager fragmentManager = getSupportFragmentManager();
                Navigator.navigateToContactsFragment(fragmentManager);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
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





    public boolean checkGpsStatus() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        } else {
            showGPSDisabledAlert();
        }
        return false;
    }

    private void showGPSDisabledAlert() {
        PositiveClick positiveClick = new PositiveClick() {
            @Override
            public void onClick() {
                Navigator.navgateToSettingsToStartGPS();
            }
        };
        MyApplication.getInstance().showAlertWithPositiveNegativeButton(getString(R.string
                .enable_gps_header), getString(R.string
                .enable_gps_message), getString(R.string.cancel), getString(R.string
                .enable_gps), positiveClick);
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
        if(mMapLocationListener!=null){
            mMapLocationListener.onLocationChanged(mlocation);
        }
        location = mlocation;
        CustomLog.d("MapActivity","Radius"+location.getAccuracy());
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(latLng)
                .draggable(false));


        googleMap.setOnMarkerClickListener(this);
        setMyLatLngLocation(latLng);

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                CustomLog.d("Click","InfoWindow Click");
                getLast30MinLocations(marker);
            }
        });
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }
    private LocationDataMapper.OnTaskCompletedListener onTaskCompletedListener = new LocationDataMapper.OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted(LocationSendingResponse locationSendingResponse) {
            Toast.makeText(MyApplication.getCurrentActivityContext(),"Location sent to server: "+locationSendingResponse.getSucces(),Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onTaskFailed(String response) {
            Toast.makeText(MyApplication.getCurrentActivityContext(),"Location sending"+response,Toast.LENGTH_SHORT).show();
        }
    };
    public void getLast30MinLocations(Marker marker){
        CustomLog.d("Marker id","ID"+marker.getTitle());
        MyApplication.getInstance().showProgressDialog(getString(R.string.loading_data),getString(R.string.please_wait));
        UserDataMapper userDataMapper = new UserDataMapper(MyApplication.getCurrentActivityContext());
        //userDataMapper.getUsersLast30MinLocations();
        MyApplication.getInstance().hideProgressDialog();

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
                        .draggable(false).title(""+locationList.get(i).getPhone()));
                builder.include(latLng);
            }
            LatLng latLng = getMyLatLngLocation();
            builder.include(latLng);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 48));
        }

    }

    public void drawRouteOfSelectedUser(){

        googleMap.clear();

        ArrayList<LatLng> points = new ArrayList<LatLng>();
        PolylineOptions lineOptions = new PolylineOptions();
        List<UserLocations> locations = UsersLast30MinLocationsOperation.getUsersLast30MinLocations(MyApplication.getCurrentActivityContext());
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < locations.size(); i++) {

            double lat = Double.parseDouble(locations.get(i).getLatitude());
            double lng = Double.parseDouble(locations.get(i).getLongitude());
            LatLng position = new LatLng(lat, lng);

            builder.include(position);
            points.add(position);
        }
        if(points.size() > 0){
            lineOptions.addAll(points);
            lineOptions.width(10);
            lineOptions.color(Color.RED);
            googleMap.addPolyline(lineOptions);
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
                    userId.setText(userData.get(0).getPhone());
                    userPhone.setText(userData.get(0).getName());
                }
                else {
                    userId.setText( sharedPreferencesData.getUserId());
                    userPhone.setText(sharedPreferencesData.getUserPhone());
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
}
