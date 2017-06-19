package com.riktam.mapmate.locationsharing.mappers;

import android.content.Context;

import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.db.operations.FriendsTableOperations;
import com.riktam.mapmate.locationsharing.db.operations.UserLastknownLocationOperations;
import com.riktam.mapmate.locationsharing.interfaces.PositiveClick;
import com.riktam.mapmate.locationsharing.responses.UserAuthToken;
import com.riktam.mapmate.locationsharing.responses.UserAuthentication;
import com.riktam.mapmate.locationsharing.utils.CustomLog;
import com.riktam.mapmate.locationsharing.utils.Navigator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by admin1 on 7/12/16.
 */
public class UserDataMapper {

    private Context context;
    public UserDataMapper(Context context){
        this.context = context;
    }

    public interface OnLoginListener {
        void onTaskCompleted(UserAuthentication userAuthenticationResponse);

        void onTaskFailed(String request);
    }

    private OnLoginListener onLoginListener;
    public void getUsersAuthToken(OnLoginListener onLoginListener, String accessToken, String googleId){
        this.onLoginListener = onLoginListener;
        if (MyApplication.getInstance().isConnectedToInterNet()){
            MyApplication.getInstance().showProgressDialog(context.getString(R.string.please_wait),context.getString(R.string.logging_in));
            Call<UserAuthentication> call = MyApplication.getInstance().retrofitApiServices.getUserAuthToken(accessToken, googleId);
            call.enqueue(userAuthToken);
        }else {
            PositiveClick positiveClick = new PositiveClick() {
                @Override
                public void onClick() {
                    Navigator.getInstance().navgateToSettingssToStartInternet();
                }
            };
            MyApplication.getInstance().showAlertWithPositiveNegativeButton(context.getString(R.string
                    .enable_data_header), context.getString(R.string
                    .enable_data_message), context.getString(R.string.cancel), context.getString(R.string
                    .enable_data), positiveClick);
        }
    }
    private Callback<UserAuthentication> userAuthToken = new
            Callback<UserAuthentication>() {

                @Override
                public void onResponse(Call<UserAuthentication> call,
                                       Response<UserAuthentication>
                                               response) {
                    MyApplication.getInstance().hideProgressDialog();
                    if (response.code() == 401) {
                        onLoginListener.onTaskFailed("Session Expired");
                        Navigator.getInstance().navigateToMainActivity();
                    } else if (response.code() == 504) {
                        onLoginListener.onTaskFailed("Unknown Host");

                    } else if (response.code() == 503) {
                        onLoginListener.onTaskFailed("Server down");
                    }
                    if (response.isSuccessful()) {
                        parseUsersAuthToken(response.body());
                        onLoginListener.onTaskCompleted(response.body());
                    } else {
                        CustomLog.e("UserAutToken callback","user info data service error");
                    }

                }

                @Override
                public void onFailure(Call<UserAuthentication> call, Throwable t) {
                    MyApplication.getInstance().hideProgressDialog();
                    onLoginListener.onTaskFailed("Nextwork Error");
                    CustomLog.e("UserAutToken callback","Network error");
                }
            };

    private void parseUsersAuthToken(UserAuthentication userAuthentication){
        UserAuthToken userAuthToken = userAuthentication.getUserAuthToken();

        MyApplication.getInstance().sharedPreferencesData.setUserToken(userAuthToken.getToken());
        MyApplication.getInstance().sharedPreferencesData.setFirstName(userAuthToken.getFirstName());
        MyApplication.getInstance().sharedPreferencesData.setLastName(userAuthToken.getLastName());
        MyApplication.getInstance().sharedPreferencesData.setId(userAuthToken.getId());

        CustomLog.d("UserDatMapper","Token: "+MyApplication.getInstance().sharedPreferencesData.getUserToken());
    }

}
