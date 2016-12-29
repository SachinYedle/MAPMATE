package com.example.admin1.locationsharing.acitivities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.example.GetGoogleCirclesList;
import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.db.dao.UserLastKnownLocation;
import com.example.admin1.locationsharing.db.dao.operations.UserLastknownLocationOperations;
import com.example.admin1.locationsharing.services.BackgroundLocationService;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.mappers.UserDataMapper;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.Navigator;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
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

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 9001;
    private CallbackManager callbackManager;
    private SignInButton googleSignInButton;
    private Button googleSignOut;
    private EditText phoneEditText;
    private GoogleApiClient googleApiClient;
    private final int REQUSTED_CODE = 99;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFacebookAuth();
        setContentView(R.layout.activity_main);
        MyApplication.getInstance().setCurrentActivityContext(MainActivity.this);

        checkPermission();

        Intent intent = new Intent(MainActivity.this,LocationActivity.class);
        startActivity(intent);

        //checkIsLoggedIn();
        checkPermission();
        initializeVariables();
        setUpListeners();
        setUpGoogleLoginOption();
    }

    public void checkIsLoggedIn(){
        SharedPreferencesData preferencesData = new SharedPreferencesData(MyApplication.getCurrentActivityContext());
        if(!preferencesData.getUserId().equals("")){
            Navigator.navigateToMapActivity();
        }
    }
    public void initializeFacebookAuth(){
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        AppEventsLogger.activateApp(this);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        accessToken.setCurrentAccessToken(null);
        Profile.getCurrentProfile().setCurrentProfile(null);
        LoginManager.getInstance().logOut();
    }

    public void callToBackgroundLocationService(){
        if(ActivityCompat.checkSelfPermission(MyApplication.getCurrentActivityContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startService(new Intent(this, BackgroundLocationService.class));
        }
    }
    public boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, REQUSTED_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, REQUSTED_CODE);
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
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MyApplication.getCurrentActivityContext(),"Permissions granted",Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(this, "Permissions denied", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }
    public void initializeVariables(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //setDrawerLayout(MyApplication.getInstance().getCurrentActivityContext());
       /* LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        FacebookCallback<LoginResult> facebookCallback = getFacebookCallback();
        loginButton.registerCallback(callbackManager,facebookCallback);*/
        googleSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        googleSignInButton.setSize(SignInButton.SIZE_STANDARD);
        googleSignOut = (Button)findViewById(R.id.btn_sign_out);
        /*Button signIn = (Button)findViewById(R.id.sign_in);
        signIn.setOnClickListener(this);
        phoneEditText = (EditText)findViewById(R.id.phone);
        sharedPreferencesData = new SharedPreferencesData(MainActivity.this);
        String authToken = sharedPreferencesData.getUserId();
        if(!authToken.equals("")){
            CustomLog.i("Token",sharedPreferencesData.getUserId());
            Navigator.navigateToMapActivity();
        }*/
    }

    public void setUpListeners(){
        googleSignInButton.setOnClickListener(this);
        googleSignOut.setOnClickListener(this);
    }

    public void setUpGoogleLoginOption(){

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("441095881058-adjl4bl21uab4hemtra3r65uc486d3nv.apps.googleusercontent.com")
                .requestEmail()
                .build();

        //.requestScopes(new Scope("googleapis.com/auth/contacts.readonly"))

        googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .enableAutoManage(this,this)
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
        callbackManager.onActivityResult(requestCode, resultCode, data);
        CustomLog.e("MainActivity","Activity res");
        if (requestCode == RC_SIGN_IN) {

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            CustomLog.e("MainActivity","Activity res1"+result.isSuccess());
            if (result.isSuccess()) {
                CustomLog.e("MainActivity","Activity res2");
                handleGoogleSignInResult(result);

            }
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d("MainActivity", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            String fullName = acct.getDisplayName();
            String token = acct.getEmail();
            CustomLog.i("MainActivity","token: "+acct.getIdToken());
            String email = acct.getEmail();
            try {
                GetGoogleCirclesList.setUp(acct.getIdToken());
            } catch (IOException e) {
                CustomLog.e("MainActivity","people Api Error"+e);
            }


            //List<Contacts.People> peopleList = acct.getGrantedScopes();
            new UserDataMapper(MyApplication.getCurrentActivityContext()).getUsersAuthToken(email);
            callToBackgroundLocationService();

            UserLastKnownLocation lastloc = new UserLastKnownLocation(null,"Sachin","df8b0f40b0344111a195a3bc46182599","17.4107389","78.4149535","2016-12-27 06:29:39");
            UserLastknownLocationOperations.insertUsersLastKnownLocation(MyApplication.getCurrentActivityContext(),lastloc);
            lastloc = new UserLastKnownLocation(null,"Sunand","95dac4dfeb1f44b6887a4316cc4f5331","17.5307389","78.5449535","2016-12-27 06:29:39");
            UserLastknownLocationOperations.insertUsersLastKnownLocation(MyApplication.getCurrentActivityContext(),lastloc);
            lastloc = new UserLastKnownLocation(null,"Venky","ec22012fc9704663a6b558da37f7d01a","17.4907389","78.5949535","2016-12-27 06:29:39");
            UserLastknownLocationOperations.insertUsersLastKnownLocation(MyApplication.getCurrentActivityContext(),lastloc);
            lastloc = new UserLastKnownLocation(null,"riktam","6b0ecda7ca6c46a7b268a713c93fa61b","17.5107389","78.3949535","2016-12-27 06:29:39");
            UserLastknownLocationOperations.insertUsersLastKnownLocation(MyApplication.getCurrentActivityContext(),lastloc);

            finish();
            Navigator.navigateToMapActivity();
            CustomLog.i("Name & Email",fullName + "&" +email);
            googleSignInButton.setVisibility(View.GONE);
            googleSignOut.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(MyApplication.getCurrentActivityContext(),"Something went Wrong Please try again",Toast.LENGTH_SHORT).show();
            CustomLog.e("Login","Unsuccesfull");
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sign_in_button:
                signInWithGoogle();
                break;
            case R.id.btn_sign_out:
                signOut();
                break;
            case R.id.sign_in:
                //singInUserWithPhone();
                break;
        }
    }
    private void signOut() {
        if( googleApiClient.isConnected()){
            Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            googleSignInButton.setVisibility(View.VISIBLE);
                            googleSignOut.setVisibility(View.GONE);
                            revokeAccess();
                        }
                    });
        }

    }
    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        CustomLog.d("Google Access","Revoked");
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        CustomLog.e("MainActivity","OnConnectionFailed");
    }




    /*public FacebookCallback<LoginResult> getFacebookCallback(){
        FacebookCallback<LoginResult> facebookCallback =  new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(MyApplication.getInstance().getCurrentActivityContext(),"Login Successfull",Toast.LENGTH_SHORT).show();
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        CustomLog.d("Response", response.toString() + "\njson" + object.toString());
                        try {
                            String email = (String) object.get("email");
                            Toast.makeText(MyApplication.getInstance().getCurrentActivityContext(), email, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                //CustomLog.e("OnCancel","Login Canceled");
                Toast.makeText(MyApplication.getCurrentActivityContext(),"Login cancelled",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(MyApplication.getCurrentActivityContext(),"Something went Wrong Please try again",Toast.LENGTH_SHORT).show();
                //CustomLog.e("OnError",error.toString()+"error");
            }
        };
        return facebookCallback;
    }*/


    /*public void singInUserWithPhone(){
        String userPhone = phoneEditText.getText().toString();

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        String countryCode = telephonyManager.getNetworkCountryIso();
        CustomLog.i("MainActivity",countryCode+"");
        new UserDataMapper(MyApplication.getCurrentActivityContext()).getUsersAuthToken(userPhone);

        if(sharedPreferencesData.getUserId().equals("")){
            phoneEditText.setError("email not Valid");
        }
        else{
            CustomLog.i("Token ",""+sharedPreferencesData.getUserId());
            sharedPreferencesData.setUserPhone(userPhone);
            //sharedPreferencesData.setUserCountryCode(countryCode);
            //Navigator.navigateToMapActivity();
        }
    }*/
    /*public static boolean isValidPhoneNumber(String phone) {
        if (phone.length() < 6 || phone.length() > 13 || phone.equals("")) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(phone).matches();
        }
    }*/
}
