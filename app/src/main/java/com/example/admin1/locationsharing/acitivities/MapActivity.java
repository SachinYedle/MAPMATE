package com.example.admin1.locationsharing.acitivities;

import android.Manifest;
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
import com.example.admin1.locationsharing.fragments.ContactsFragment;
import com.example.admin1.locationsharing.fragments.DrawerFragment;
import com.example.admin1.locationsharing.interfaces.PositiveClick;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.Navigator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;

public class MapActivity extends DrawerActivity implements GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMarkerClickListener {

    private GoogleApiClient googleApiClient;
    private Location location;
    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private LocationRequest locationRequest;
    private final int REQUSTED_CODE = 99;
    private SharedPreferencesData sharedPreferencesData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        MyApplication.getInstance().setCurrentActivityContext(this);
        initializeVariables();
        getMyLocation();
    }


    public void initializeVariables(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setDrawerLayout(MyApplication.getInstance().getCurrentActivityContext());
        sharedPreferencesData = new SharedPreferencesData(MyApplication.getInstance().getCurrentActivityContext());
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        DrawerFragment fragment = new DrawerFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.drawer_fragment_layout,fragment);
        fragmentTransaction.addToBackStack("DrawerLayout");
        fragmentTransaction.commit();

    }
    public void getMyLocation() {

        checkPermission();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.add:
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                ContactsFragment contactsFragment = new ContactsFragment();
                fragmentTransaction.replace(R.id.map,contactsFragment);
                fragmentTransaction.addToBackStack("contacts");
                fragmentTransaction.commit();
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
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
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

    public boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUSTED_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUSTED_CODE);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUSTED_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (googleApiClient == null) {

                            if (checkGpsStatus()) {
                                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                    buildGoogleApiClient();
                                }
                            }
                        }
                        googleMap.setMyLocationEnabled(true);
                    }

                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
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
                Navigator.navgateToSettings();
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
        Log.i("Map Activity", "onConnection failed");
    }

    @Override
    public void onLocationChanged(Location mlocation) {
        MyApplication.getInstance().showProgressDialog(getString(R.string
                .getting_friends_location), getString(R.string.please_wait));

        location = mlocation;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(latLng)
                .draggable(false));

        googleMap.setOnMarkerClickListener(this);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        MyApplication.getInstance().hideProgressDialog();
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        GoogleMap.InfoWindowAdapter infoWindowAdapter = new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
               return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                CustomLog.d("OnMarkerClick","marker click");
                View view = getLayoutInflater().inflate(R.layout.map_marker_info_window,null);
                TextView userId = (TextView)view.findViewById(R.id.user_id);
                userId.setText( sharedPreferencesData.getUserId());
                TextView userPhone = (TextView)view.findViewById(R.id.user_phone);
                userPhone.setText(sharedPreferencesData.getUserPhone());
                return view;
            }
        };
        googleMap.setInfoWindowAdapter(infoWindowAdapter);
        return false;
    }
}
