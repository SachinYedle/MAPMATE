package com.example.admin1.locationsharing.acitivities;

import android.Manifest;
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
import android.widget.EditText;
import android.widget.Toast;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.interfaces.PositiveClick;
import com.example.admin1.locationsharing.responses.UserAuthentication;
import com.example.admin1.locationsharing.services.BackgroundLocationService;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.mappers.UserDataMapper;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.Navigator;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import com.example.admin1.locationsharing.utils.SharedPreferencesData;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 9001;
    private SignInButton googleSignInButton;
    private Button googleSignOut;
    private GoogleApiClient googleApiClient;
    private final int REQUSTED_CODE = 99;
    private final int REQUEST_AUTHORIZATION = 1002;

//    /**
//     * Global instance of the HTTP transport.
//     */
//    private static HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
//
//    /**
//     * Global instance of the JSON factory.
//     */
//    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyApplication.getInstance().setCurrentActivityContext(MainActivity.this);
        checkIsLoggedIn();
        initializeVariables();
        setUpListeners();
        setUpGoogleLoginOption();
    }

    public void checkIsLoggedIn() {
        if (!MyApplication.getInstance().sharedPreferencesData.getEmail().equals("")) {
            callToBackgroundLocationService();
            Navigator.getInstance().navigateToMapActivity();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().setCurrentActivityContext(MainActivity.this);
        checkPermission();
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
        //setDrawerLayout(MyApplication.getInstance().getCurrentActivityContext());
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
                .requestIdToken("441095881058-adjl4bl21uab4hemtra3r65uc486d3nv.apps.googleusercontent.com")
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
        } else if (requestCode == REQUEST_AUTHORIZATION) {
            //getPeoplesList(MyApplication.getInstance().sharedPreferencesData.getEmail());
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            String fullName = acct.getDisplayName();
            String token = acct.getIdToken();
            String email = acct.getEmail();
            Uri personPhoto = acct.getPhotoUrl();
            CustomLog.d("MainActivity", "url: " + personPhoto);
            MyApplication.getInstance().sharedPreferencesData.setFirstName(fullName);
            MyApplication.getInstance().sharedPreferencesData.setEmail(email);

            CustomLog.i("Main Activity", "Token: " + acct.getIdToken());
            //getPeoplesList(acct.getEmail());

            new UserDataMapper(MyApplication.getCurrentActivityContext()).getUsersAuthToken(onLoginListener, token);

            googleSignInButton.setVisibility(View.GONE);
            googleSignOut.setVisibility(View.VISIBLE);
        } else {
            MyApplication.getInstance().showToast("Something went Wrong Please try again");
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
            MyApplication.getInstance().showToast("Error :" + request);
        }
    };

    /*public void getPeoplesList(final String email) {

        final List<String> SCOPES =
                Arrays.asList(PeopleScopes.CONTACTS,PeopleScopes.PLUS_LOGIN);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {


                *//*String scope = "oauth2:"+ "<profile scope>"+"<circles scope>"+.....maybe more...
                // Replace <> part with scope URLs from Google Domain API scopes page
                String token = GoogleAuthUtil.getToken(this, accountName , scope);*//*
                // On worker thread
                GoogleAccountCredential credential =
                        GoogleAccountCredential.usingOAuth2(MainActivity.this, SCOPES);
                credential.setSelectedAccount(
                        new Account(email, "com.google"));
                People service = new People.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName("FriendLocation Sharing App" *//* whatever you like *//*)
                        .build();
                // All the person details
                //Person meProfile = service.people().get("people/me").execute();
                ListConnectionsResponse response = null;
                try {
                    response = service.people().connections()
                            .list("people/me")
                            .setRequestMaskIncludeField("person.names,person.emailAddresses")
                            .execute();
                    CustomLog.i("", "");
                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    CustomLog.e("MainActivity", "Get Peoples Error" + e);
                }
                // Print display name of connections if available.
                List<Person> connections = null;
                try {
                    connections = response.getConnections();
                } catch (NullPointerException e) {
                    CustomLog.e("MainActivity", "Get Connections" + e);
                }

                if (connections != null && connections.size() > 0) {
                    for (Person person : connections) {
                        List<EmailAddress> emailAddresses = person.getEmailAddresses();
                        List<Name> names = person.getNames();
                        if (names != null && names.size() > 0) {
                            CustomLog.d("People", "Name: " + person.getNames().get(0)
                                    .getDisplayName());
                            if (emailAddresses != null && emailAddresses.size() > 0) {
                                for (EmailAddress personEmail : emailAddresses) {
                                    CustomLog.d("People", "Email: " + personEmail.getValue());
                                }
                            }
                        } else {
                            CustomLog.i("People", "No names available for connection.");
                        }
                    }

                } else {
                    CustomLog.i("People", "No connections found.");
                }
            }
        });
        thread.start();


    }
*/
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
