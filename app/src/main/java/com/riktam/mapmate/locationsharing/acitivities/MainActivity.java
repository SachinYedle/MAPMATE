package com.riktam.mapmate.locationsharing.acitivities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.Link;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.Name;
import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.db.dao.UserGmailFriends;
import com.riktam.mapmate.locationsharing.db.operations.GmailFriendsOperations;
import com.riktam.mapmate.locationsharing.db.operations.UserLocationsOperations;
import com.riktam.mapmate.locationsharing.interfaces.PositiveClick;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.mappers.UserDataMapper;
import com.riktam.mapmate.locationsharing.pojo.Contact;
import com.riktam.mapmate.locationsharing.responses.UserAuthentication;
import com.riktam.mapmate.locationsharing.services.BackgroundLocationService;
import com.riktam.mapmate.locationsharing.utils.Constants;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 9001;
    private ImageView googleSignInButton;
    private GoogleApiClient googleApiClient;
    private final int REQUSTED_CODE = 99;
    private String authCode;
    private String accessToken;
    private String client_id;
    private String client_secret;
    private String access_token_url;
    private String token;
    private String googleId;

    public MainActivity() throws IOException {
    }

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
        googleSignInButton = (ImageView) findViewById(R.id.sign_in_button);
       // googleSignOut = (Button) findViewById(R.id.btn_sign_out);
    }

    public void setUpListeners() {
        googleSignInButton.setOnClickListener(this);
        //googleSignOut.setOnClickListener(this);
    }

    public void setUpGoogleLoginOption() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestServerAuthCode(getString(R.string.server_client_id))
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
            authCode = acct.getServerAuthCode();

            new GetAccessToken(this).execute();

            CustomLog.d("Auth code", "" + authCode);
            String fullName = acct.getDisplayName();
            token = acct.getIdToken();
            googleId = acct.getId();
            String email = acct.getEmail();

            Uri personPhoto = acct.getPhotoUrl();
            CustomLog.d("MainActivity", "url: " + personPhoto);
            CustomLog.d("MainActivity", "id: " + acct.getId());
            MyApplication.getInstance().sharedPreferencesData.setFirstName(fullName);
            MyApplication.getInstance().sharedPreferencesData.setEmail(email);
            MyApplication.getInstance().sharedPreferencesData.setProfilePic(personPhoto.toString());

            CustomLog.i("Main Activity", "Token: " + acct.getIdToken());

            //googleSignInButton.setVisibility(View.GONE);
            //googleSignOut.setVisibility(View.VISIBLE);
        } else {
            MyApplication.getInstance().showToast(getString(R.string.something_went_wrong));
            CustomLog.e("Login", "Unsuccesfull");
        }
    }

    private  void login(){
        new UserDataMapper(MyApplication.getCurrentActivityContext()).getUsersAuthToken(onLoginListener, token, googleId);
    }
    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("client_secret.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void readJsonFromAssets() {
        try {

            JSONObject main = new JSONObject(loadJSONFromAsset());
            JSONObject web = main.getJSONObject("web");
            client_id = web.getString("client_id");
            client_secret = web.getString("client_secret");
            access_token_url = "https://www.googleapis.com/oauth2/v4/token";

        } catch (JSONException e) {
            CustomLog.e("Json parse exception", " " + e.getLocalizedMessage());
        }
    }

    private class GetAccessToken extends
            AsyncTask<String, String, GoogleTokenResponse> {

        private ProgressDialog pDialog;
        private Context context;

        GoogleTokenResponse tokenResponse = null;

        public GetAccessToken(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected GoogleTokenResponse doInBackground(String... args) {
            try {
                readJsonFromAssets();
                tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                        new NetHttpTransport(),
                        JacksonFactory.getDefaultInstance(),
                        access_token_url, client_id, client_secret, authCode, "")
                        // Specify the same redirect URI that you use with your web
                        // app. If you don't have a web version of your app, you can
                        // specify an empty string.
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return tokenResponse;
        }

        @Override
        protected void onPostExecute(GoogleTokenResponse googleContacts) {
            accessToken = googleContacts.getAccessToken();
            new GetGoogleContacts(MainActivity.this).execute(accessToken);
            CustomLog.d("Token", googleContacts.getAccessToken());
        }

    }

    private class GetGoogleContacts extends
            AsyncTask<String, String, List<ContactEntry>> {

        private Context context;

        public GetGoogleContacts(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MyApplication.getInstance().showProgressDialog(getString(R.string.loading_google_contacts));
        }

        @Override
        protected List<ContactEntry> doInBackground(String... args) {
            String accessToken = args[0];
            ContactsService contactsService = new ContactsService(
                    Constants.APP);
            contactsService.setHeader("Authorization", "Bearer " + accessToken);
            contactsService.setHeader("GData-Version", "3.0");
            List<ContactEntry> contactEntries = null;
            try {
                URL feedUrl = new URL(Constants.CONTACTS_URL);
                ContactFeed resultFeed = contactsService.getFeed(feedUrl,
                        ContactFeed.class);
                contactEntries = resultFeed.getEntries();
            } catch (Exception e) {
                MyApplication.getInstance().hideProgressDialog();
            }
            return contactEntries;
        }

        @Override
        protected void onPostExecute(List<ContactEntry> googleContacts) {
            if (null != googleContacts && googleContacts.size() > 0) {
                GmailFriendsOperations.getInstance().deleteFriendsTableData();
                for (ContactEntry contactEntry : googleContacts) {
                    String name = "";
                    String email = "";

                    if (contactEntry.hasName()) {
                        Name tmpName = contactEntry.getName();
                        if (tmpName.hasFullName()) {
                            name = tmpName.getFullName().getValue();
                        } else {
                            if (tmpName.hasGivenName()) {
                                name = tmpName.getGivenName().getValue();
                                if (tmpName.getGivenName().hasYomi()) {
                                    name += " ("
                                            + tmpName.getGivenName().getYomi()
                                            + ")";
                                }
                                if (tmpName.hasFamilyName()) {
                                    name += tmpName.getFamilyName().getValue();
                                    if (tmpName.getFamilyName().hasYomi()) {
                                        name += " ("
                                                + tmpName.getFamilyName()
                                                .getYomi() + ")";
                                    }
                                }
                            }
                        }
                    }
                    List<Email> emails = contactEntry.getEmailAddresses();
                    if (null != emails && emails.size() > 0) {
                        Email tempEmail = (Email) emails.get(0);
                        email = tempEmail.getAddress();
                    }
                    Link photoLink = contactEntry.getContactPhotoLink();
                    String photoLinkHref = null;
                    //System.out.println("Photo Link: " + photoLinkHref);
                    Contact contact = new Contact(name, email, photoLinkHref);
                    if (email != "" && name != "" && !MyApplication.getInstance().sharedPreferencesData.getEmail().equalsIgnoreCase(email)) {
                        insertGmailFriend(contact);
                    }
                    CustomLog.d("Contact", "Name: " + contact.getName() + " Email: " + contact.getEmail() + "Photo: " + photoLinkHref);
                }
                login();

            } else {
                Log.e("Contacts error", "No Contact Found.");
                login();
                Toast.makeText(context, "No Contact Found.", Toast.LENGTH_SHORT)
                        .show();
            }
            MyApplication.getInstance().hideProgressDialog();
        }

    }

    private void insertGmailFriend(Contact contact) {
        UserGmailFriends friend = new UserGmailFriends();
        friend.setEmail(contact.getEmail());
        friend.setName(contact.getName());
        friend.setProfilePicUrl(contact.getPhotoUrl());
        GmailFriendsOperations.getInstance().insertFriends(friend);
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
//            case R.id.btn_sign_out:
//                signOut();
//                break;

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
                            //googleSignOut.setVisibility(View.GONE);
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
