package com.example.admin1.locationsharing.acitivities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.app.MyApplication;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.admin1.locationsharing.utils.SharedPreferencesData;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int RC_SIGN_IN = 9001;
    private CallbackManager callbackManager;
    private SignInButton googleSignInButton;
    private Button googleSignOut;
    private EditText phoneEditText;
    private GoogleApiClient googleApiClient;
    private SharedPreferencesData sharedPreferencesData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        AppEventsLogger.activateApp(this);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        accessToken.setCurrentAccessToken(null);
        Profile.getCurrentProfile().setCurrentProfile(null);
        LoginManager.getInstance().logOut();

        setContentView(R.layout.activity_main);

        MyApplication.getInstance().setCurrentActivityContext(this);
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        FacebookCallback<LoginResult> facebookCallback = getFacebookCallback();

        loginButton.registerCallback(callbackManager,facebookCallback);


        googleSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        googleSignInButton.setSize(SignInButton.SIZE_STANDARD);
        googleSignInButton.setOnClickListener(this);

        googleSignOut = (Button)findViewById(R.id.btn_sign_out);
        googleSignOut.setOnClickListener(this);

        Button signIn = (Button)findViewById(R.id.sign_in);
        signIn.setOnClickListener(this);
        phoneEditText = (EditText)findViewById(R.id.phone);

        sharedPreferencesData = new SharedPreferencesData(MainActivity.this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        googleApiClient.connect();

    }

    public FacebookCallback<LoginResult> getFacebookCallback(){
        FacebookCallback<LoginResult> facebookCallback =  new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(MainActivity.this,"Login Successfull",Toast.LENGTH_SHORT).show();
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d("Parth", response.toString() + "\njson" + object.toString());
                        try {
                            String email = (String) object.get("email");
                            Toast.makeText(MainActivity.this, email, Toast.LENGTH_SHORT).show();
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
                Log.e("OnCancel","Login Canceled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("OnError",error.toString()+"error");
            }
        };
        return facebookCallback;
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
                handleSignInResult(result);
            }
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("MainActivity", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();

            String fullName = acct.getDisplayName();
            String email = acct.getEmail();

            Log.i("Name & Email",fullName + "&" +email);
            googleSignInButton.setVisibility(View.GONE);
            googleSignOut.setVisibility(View.VISIBLE);
        } else {
            Log.e("Login","Unsuccesfull");
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
                singInUserWithPhone();
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
                        Log.d("Google Access","Revoked");
                    }
                });
    }

    public void singInUserWithPhone(){
        String userPhone = phoneEditText.getText().toString();

        /*TelephonyManager tm = (TelephonyManager)getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        String countryCode = tm.getNetworkCountryIso();*/

        if(!isValidPhoneNumber(userPhone)){
            phoneEditText.setError("Phone not Valid");
        }
        else{
            sharedPreferencesData.setUserId(userPhone);
            sharedPreferencesData.setUserPhone(userPhone);
            Intent intent = new Intent(MainActivity.this,MapActivity.class);
            startActivity(intent);
        }
    }
    public static boolean isValidPhoneNumber(String phone) {
        if (phone.length() < 6 || phone.length() > 13 || phone == null) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(phone).matches();
        }
    }
}
