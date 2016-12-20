package com.example.admin1.locationsharing.acitivities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Contacts;
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
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

import com.example.admin1.locationsharing.utils.SharedPreferencesData;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

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
        checkIsLoggedIn();
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
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_CONTACTS}, REQUSTED_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_CONTACTS}, REQUSTED_CODE);
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
                            Manifest.permission.READ_CONTACTS)
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
                .requestEmail()
                .build();
        //.requestScopes(new Scope("googleapis.com/auth/contacts.readonly"))
        googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
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

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                handleGoogleSignInResult(result);
            }
        }
    }

    public void getGoogleContacts(){



        googleApiClient.disconnect();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN)) // "https://www.googleapis.com/auth/plus.login"
                .requestScopes(new Scope(Scopes.PLUS_ME)) // "https://www.googleapis.com/auth/plus.me"
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        googleApiClient.connect();

    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d("MainActivity", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            String fullName = acct.getDisplayName();
            String token = acct.getId();
            CustomLog.i("google Tokenid ","token: "+token + "Acc"+acct.getIdToken());
            String email = acct.getEmail();

            //List<Contacts.People> peopleList = acct.getGrantedScopes();
            new UserDataMapper(MyApplication.getCurrentActivityContext()).getUsersAuthToken(email);
            callToBackgroundLocationService();

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
