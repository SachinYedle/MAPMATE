package com.riktam.mapmate.locationsharing.acitivities;

import android.Manifest;
import android.accounts.Account;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.common.api.Scope;
import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.interfaces.PositiveClick;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.mappers.UserDataMapper;
import com.riktam.mapmate.locationsharing.responses.UserAuthentication;
import com.riktam.mapmate.locationsharing.services.BackgroundLocationService;
import com.riktam.mapmate.locationsharing.utils.CustomLog;

import com.riktam.mapmate.locationsharing.utils.Navigator;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 9001;
    private SignInButton googleSignInButton;
    private Button googleSignOut;
    private GoogleApiClient googleApiClient;
    private final int REQUSTED_CODE = 99;
    private final int REQUEST_AUTHORIZATION = 1002;
    private final int RC_REAUTHORIZE = 100;
    private Account mAuthorizedAccount;
    private String authCode;
    private static String GOOGLE_CALENDAR_API_SCOPE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyApplication.getInstance().setCurrentActivityContext(MainActivity.this);
        Fabric.with(this, new Crashlytics());
        checkIsLoggedIn();
        initializeVariables();
        setUpListeners();
        setUpGoogleLoginOption();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().setCurrentActivityContext(MainActivity.this);
        checkPermission();
    }

    public void checkIsLoggedIn() {
        if (!MyApplication.getInstance().sharedPreferencesData.getEmail().equals("")) {
            callToBackgroundLocationService();
            Navigator.getInstance().navigateToMapActivity();
            finish();
        }
    }

    public void callToBackgroundLocationService() {
        if (ActivityCompat.checkSelfPermission(MyApplication.getCurrentActivityContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startService(new Intent(this, BackgroundLocationService.class));
        }
    }

    public boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, REQUSTED_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, REQUSTED_CODE);
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
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //do whatever want to do when permission granted
                } else {
                    finish();
                }
        }
    }

    public void initializeVariables() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        googleSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        googleSignInButton.setSize(SignInButton.SIZE_STANDARD);
        googleSignOut = (Button) findViewById(R.id.btn_sign_out);
    }

    public void setUpListeners() {
        googleSignInButton.setOnClickListener(this);
        googleSignOut.setOnClickListener(this);
    }

    public void setUpGoogleLoginOption() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.request_id_token))
                .requestServerAuthCode(getString(R.string.request_id_token))
                .requestScopes(new Scope("https://www.google.com/m8/feeds"))
                .requestEmail()
                .build();

        //.requestScopes(new Scope("googleapis.com/auth/contacts.readonly"))

        googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        googleApiClient.connect();
    }

    private void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                handleGoogleSignInResult(result);

            }
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            mAuthorizedAccount = acct.getAccount();
            authCode = acct.getServerAuthCode();

            String fullName = acct.getDisplayName();
            String token = acct.getIdToken();

            String email = acct.getEmail();

            Uri personPhoto = acct.getPhotoUrl();
            CustomLog.d("MainActivity", "url: " + personPhoto);
            CustomLog.d("MainActivity", "id: " + acct.getId());
            MyApplication.getInstance().sharedPreferencesData.setFirstName(fullName);
            MyApplication.getInstance().sharedPreferencesData.setEmail(email);

            CustomLog.i("Main Activity", "Token: " + acct.getIdToken());

            new UserDataMapper(MyApplication.getCurrentActivityContext()).getUsersAuthToken(onLoginListener, token, acct.getId());

            googleSignInButton.setVisibility(View.GONE);
            googleSignOut.setVisibility(View.VISIBLE);
        } else {
            MyApplication.getInstance().showToast(getString(R.string.something_went_wrong));
            CustomLog.e("Login", "Unsuccesfull");
        }
    }

    private UserDataMapper.OnLoginListener onLoginListener = new UserDataMapper.OnLoginListener() {
                @Override
        public void onTaskCompleted(UserAuthentication userAuthenticationResponse) {
            callToBackgroundLocationService();
            Navigator.getInstance().navigateToMapActivity();
            finish();
        }

        @Override
        public void onTaskFailed(String request) {
            MyApplication.getInstance().showToast(request);
        }
    };
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                if (MyApplication.getInstance().isConnectedToInterNet()) {
                    signInWithGoogle();
                } else {
                    PositiveClick positiveClick = new PositiveClick() {
                        @Override
                        public void onClick() {
                            Navigator.getInstance().navgateToSettingssToStartInternet();
                        }
                    };
                    MyApplication.getInstance().showAlertWithPositiveNegativeButton(getString(R.string
                            .enable_data_header), getString(R.string
                            .enable_data_message), getString(R.string.cancel), getString(R.string
                            .enable_data), positiveClick);
                }
                break;
            case R.id.btn_sign_out:
                signOut();
                break;

        }
    }

    private void signOut() {

        if (googleApiClient.isConnected()) {
            revokeAccess();
            Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            googleSignInButton.setVisibility(View.VISIBLE);
                            googleSignOut.setVisibility(View.GONE);
                        }
                    });
        }

    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        CustomLog.d("Google Access", "Revoked");
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        CustomLog.e("MainActivity", "OnConnectionFailed");
    }
}
