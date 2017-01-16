package com.example.admin1.locationsharing.acitivities;

import android.Manifest;
import android.accounts.Account;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.db.dao.UserLastKnownLocation;
import com.example.admin1.locationsharing.db.dao.operations.UserLastknownLocationOperations;
import com.example.admin1.locationsharing.services.BackgroundLocationService;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.mappers.UserDataMapper;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.Navigator;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

import com.example.admin1.locationsharing.utils.SharedPreferencesData;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.people.v1.People;
import com.google.api.services.people.v1.PeopleScopes;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Gender;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static java.security.AccessController.getContext;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 9001;
    private SignInButton googleSignInButton;
    private Button googleSignOut;
    private EditText phoneEditText;
    private GoogleApiClient googleApiClient;
    private final int REQUSTED_CODE = 99;
    private final int REQUEST_AUTHORIZATION = 1002;

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

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
        SharedPreferencesData preferencesData = new SharedPreferencesData(MyApplication.getCurrentActivityContext());
        if (!preferencesData.getEmail().equals("")) {

            Navigator.getInstance().navigateToMapActivity();
        }
    }

    public void fillUserDataToDB(){
        UserLastknownLocationOperations.deleteTableData(MyApplication.getCurrentActivityContext());
        UserLastKnownLocation lastloc = new UserLastKnownLocation(null, "Sachin", "640beedae0c841a395fb2abca53df01c", "17.4107389", "78.4149535", "2016-12-27 06:29:39");
        UserLastknownLocationOperations.insertUsersLastKnownLocation(MyApplication.getCurrentActivityContext(), lastloc);
        lastloc = new UserLastKnownLocation(null, "Sunand", "fbf5ea9008ce4ad3ab7fe5fcc20453e7", "17.5307389", "78.5449535", "2016-12-27 06:29:39");
        UserLastknownLocationOperations.insertUsersLastKnownLocation(MyApplication.getCurrentActivityContext(), lastloc);
        lastloc = new UserLastKnownLocation(null, "Venky", "ec22012fc9704663a6b558da37f7d01a", "17.4907389", "78.5949535", "2016-12-27 06:29:39");
        UserLastknownLocationOperations.insertUsersLastKnownLocation(MyApplication.getCurrentActivityContext(), lastloc);
        lastloc = new UserLastKnownLocation(null, "riktam", "dbf3178155d944dd9f609e93de30b00c", "17.5107389", "78.3949535", "2016-12-27 06:29:39");
        UserLastknownLocationOperations.insertUsersLastKnownLocation(MyApplication.getCurrentActivityContext(), lastloc);
    }
    @Override
    protected void onResume() {
        super.onResume();
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
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MyApplication.getCurrentActivityContext(), "Permissions granted", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Permissions denied", Toast.LENGTH_LONG).show();
                }
                return;
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
        CustomLog.e("MainActivity", "Activity res");
        if (requestCode == RC_SIGN_IN) {
            //revokeAccess();
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            CustomLog.e("MainActivity", "Activity res1" + result.isSuccess());
            if (result.isSuccess()) {
                CustomLog.e("MainActivity", "Activity res2");
                handleGoogleSignInResult(result);

            }
        } else if (requestCode == REQUEST_AUTHORIZATION) {
            SharedPreferencesData preferencesData = new SharedPreferencesData(MyApplication.getCurrentActivityContext());
            getPeoplesList(preferencesData.getEmail());
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d("MainActivity", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            SharedPreferencesData preferencesData = new SharedPreferencesData(MyApplication.getCurrentActivityContext());
            GoogleSignInAccount acct = result.getSignInAccount();
            String fullName = acct.getDisplayName();
            String token = acct.getIdToken();
            //preferencesData.setUserToken(token);
            CustomLog.i("MainActivity", "token: " + acct.getIdToken());
            String email = acct.getEmail();
            preferencesData.setEmail(email);

            getPeoplesList(acct.getEmail());

            new UserDataMapper(MyApplication.getCurrentActivityContext()).getUsersAuthToken(token);
            callToBackgroundLocationService();

            finish();
            Navigator.getInstance().navigateToMapActivity();

            CustomLog.i("Name & Email", fullName + "&" + email);
            googleSignInButton.setVisibility(View.GONE);
            googleSignOut.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(MyApplication.getCurrentActivityContext(), "Something went Wrong Please try again", Toast.LENGTH_SHORT).show();
            CustomLog.e("Login", "Unsuccesfull");
        }
    }

    public void getPeoplesList(final String email) {

        final List<String> SCOPES =
                Arrays.asList(PeopleScopes.CONTACTS,PeopleScopes.PLUS_LOGIN);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {


                /*String scope = "oauth2:"+ "<profile scope>"+"<circles scope>"+.....maybe more...
                // Replace <> part with scope URLs from Google Domain API scopes page
                String token = GoogleAuthUtil.getToken(this, accountName , scope);*/
                // On worker thread
                GoogleAccountCredential credential =
                        GoogleAccountCredential.usingOAuth2(MainActivity.this, SCOPES);
                credential.setSelectedAccount(
                        new Account(email, "com.google"));
                People service = new People.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName("Location Sharing App" /* whatever you like */)
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signInWithGoogle();
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
