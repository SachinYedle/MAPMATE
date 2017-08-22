package com.riktam.mapmate.locationsharing.acitivities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.db.dao.Friends;
import com.riktam.mapmate.locationsharing.db.operations.FriendsTableOperations;
import com.riktam.mapmate.locationsharing.fragments.DrawerFragment;
import com.riktam.mapmate.locationsharing.mappers.DownloadLocationsDataMapper;
import com.riktam.mapmate.locationsharing.responses.UserLocationsResponse;
import com.riktam.mapmate.locationsharing.utils.CustomLog;
import com.riktam.mapmate.locationsharing.utils.DrawRouteFunctionality;
import com.riktam.mapmate.locationsharing.utils.Navigator;


import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class FriendsRouteActivity extends DrawerActivity implements OnMapReadyCallback,
        View.OnClickListener{

    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private SmoothProgressBar mapProgressBar;
    private FloatingActionButton floatingActionButton;

//    private static final String LOG_TAG = "PlacesAPIActivity";
//    private static final int GOOGLE_API_CLIENT_ID = 0;
//    private GoogleApiClient mGoogleApiClient;
//    private static final int PERMISSION_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_route);
        MyApplication.getInstance().setCurrentActivityContext(FriendsRouteActivity.this);
        initializeVariables();
        setUpToolbar();

        if (ActivityCompat.checkSelfPermission(MyApplication.getCurrentActivityContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getMyLocation();
        } else {
            finish();
            MyApplication.getInstance().showToast(getString(R.string.please_grant_location_permissions));
        }


//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addApi(Places.PLACE_DETECTION_API)
//                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
//                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //mGoogleApiClient.connect();

    }

    private void setUpToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setDrawerLayout(MyApplication.getCurrentActivityContext());
        getSupportActionBar().setTitle(getString(R.string.app_name));
    }

    public void getMyLocation() {
        mapFragment.getMapAsync(this);
    }

    private void initializeVariables(){
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.friends_route_map);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.friend_route_add_friend_floatingActionButton);
        floatingActionButton.setOnClickListener(this);

        mapProgressBar = (SmoothProgressBar) findViewById(R.id.friends_route_progressBar);

        DrawerFragment fragment = new DrawerFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.drawer_fragment_layout, fragment);
        fragmentTransaction.addToBackStack("DrawerLayout");
        fragmentTransaction.commit();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String email = bundle.getString("email");
            MyApplication.getInstance().sharedPreferencesData.setSelectedUserEmail(email);
            drawRoute(email);
        }
    }

    public void drawRoute(String email) {
        if (email.equalsIgnoreCase(MyApplication.getInstance().sharedPreferencesData.getEmail())) {
            DownloadLocationsDataMapper dataMapper = new DownloadLocationsDataMapper(MyApplication.getCurrentActivityContext());
            dataMapper.getLocations(onTaskCompletedListener, "" + MyApplication.getInstance().sharedPreferencesData.getId());
            getSupportActionBar().setTitle(MyApplication.getInstance().sharedPreferencesData.getFirstName());
        } else {
            DownloadLocationsDataMapper dataMapper = new DownloadLocationsDataMapper(MyApplication.getCurrentActivityContext());
            Friends friends = FriendsTableOperations.getInstance().getFriendWithEmail(email).get(0);
            dataMapper.getLocations(onTaskCompletedListener, friends.getFriend_id());
            getSupportActionBar().setTitle(friends.getFriend_first_name());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.friend_route_add_friend_floatingActionButton:
                Navigator.getInstance().navigateToFriendsActivity("add friend");
                break;
        }
    }

    private DownloadLocationsDataMapper.OnTaskCompletedListener onTaskCompletedListener = new DownloadLocationsDataMapper.OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted(UserLocationsResponse userLocationsResponse) {
            mapProgressBar.setVisibility(View.GONE);
            String email = MyApplication.getInstance().sharedPreferencesData.getSelectedUserEmail();
            CustomLog.i("Map Activity", "Download Locations" + userLocationsResponse.getSuccess() + ":" + email);
            googleMap.setOnInfoWindowClickListener(null);
            DrawRouteFunctionality.getInstance().drawRouteOfSelectedUser(email, googleMap);
        }

        @Override
        public void onTaskFailed(String response) {
            MyApplication.getInstance().showToast(response);
        }
    };

    @Override
    public void onBackPressed() {
        if (isDrawerOpen()) {
            closeDrawer();
        }else {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String activity = bundle.getString("activity");
                if(activity != null && activity.equalsIgnoreCase("friends")){
                    Navigator.getInstance().navigateToFriendsActivity();
                    finish();
                }else {
                    Navigator.getInstance().navigateToMapActivity();
                    finish();
                }
            }else {
                Navigator.getInstance().navigateToMapActivity();
                finish();
            }
        }
    }

//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
//                + connectionResult.getErrorCode());
//
//        Toast.makeText(this,
//                "Google Places API connection failed with error code:" +
//                        connectionResult.getErrorCode(),
//                Toast.LENGTH_LONG).show();
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case PERMISSION_REQUEST_CODE:
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    callPlaceDetectionApi();
//                }
//                break;
//        }
//    }
//
//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//        if (mGoogleApiClient.isConnected()) {
//            if (ContextCompat.checkSelfPermission(this,
//                    Manifest.permission.ACCESS_FINE_LOCATION)
//                    != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                        PERMISSION_REQUEST_CODE);
//            } else {
//                callPlaceDetectionApi();
//            }
//
//        }
//    }
//    private void callPlaceDetectionApi() throws SecurityException {
//        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
//                .getCurrentPlace(mGoogleApiClient, null);
//        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
//            @Override
//            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
//                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
//                    Log.i(LOG_TAG, String.format("Place '%s' with " +
//                                    "likelihood: %g",
//                            placeLikelihood.getPlace().getName(),
//                            placeLikelihood.getLikelihood()));
//                }
//                likelyPlaces.release();
//            }
//        });
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        Log.e(LOG_TAG, "Google susspended");
//    }
}
