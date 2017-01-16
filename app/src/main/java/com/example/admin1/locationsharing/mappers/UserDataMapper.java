package com.example.admin1.locationsharing.mappers;

import android.content.Context;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.services.UserDataService;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.interfaces.PositiveClick;
import com.example.admin1.locationsharing.responses.UserAuthToken;
import com.example.admin1.locationsharing.responses.UserAuthentication;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.Navigator;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;

import java.io.IOException;

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

    public void getUsersAuthToken(String accessToken){
        if (MyApplication.getInstance().isConnectedToInterNet()){
            UserDataService userDataService = MyApplication.getInstance().getUserDataService();
            Call<UserAuthentication> call = userDataService.getUserAuthToken(accessToken);
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
                        CustomLog.e("User Authentication","Session Expired");
                    } else if (response.code() == 504) {
                        CustomLog.e("User Authentication","Unknown Host");
                    } else if (response.code() == 503) {
                        CustomLog.e("User Authentication","Server down");
                    }
                    if (response.isSuccessful()) {
                        parseUsersAuthToken(response.body());
                        CustomLog.i("Response",""+response);
                    } else {
                        CustomLog.e("UserAutToken callback","user info data service error");
                    }

                }

                @Override
                public void onFailure(Call<UserAuthentication> call, Throwable t) {
                    MyApplication.getInstance().hideProgressDialog();
                    CustomLog.e("UserAutToken callback","user info data service error");
                }
            };

    private void parseUsersAuthToken(UserAuthentication userAuthentication){
        UserAuthToken userAuthToken = userAuthentication.getUserAuthToken();
        SharedPreferencesData preferencesData = new SharedPreferencesData(context);
        preferencesData.setUserToken(userAuthToken.getToken());
        CustomLog.d("UserDatMapper","Token: "+preferencesData.getUserToken());
    }

}
