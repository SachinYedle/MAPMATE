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

    public void getUsersAuthToken(String email){
        if (MyApplication.getInstance().isConnectedToInterNet()){
            UserDataService userDataService = MyApplication.getInstance().getUserDataService();
            Call<UserAuthentication> call = userDataService.getUserAuthToken(email);
            call.enqueue(userAuthToken);
        }else {
            PositiveClick positiveClick = new PositiveClick() {
                @Override
                public void onClick() {
                    Navigator.navgateToSettingssToStartInternet();
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
                    if (response.isSuccessful()) {
                        parseUsersAuthToken(response.body());
                        CustomLog.i("Response",""+response);
                    } else {
                        CustomLog.e("UserAutToken callback","user info data service error");
                    }

                }

                @Override
                public void onFailure(Call<UserAuthentication> call, Throwable t) {
                    CustomLog.e("UserAutToken callback","user info data service error");
                }
            };

    private void parseUsersAuthToken(UserAuthentication userAuthentication){
        UserAuthToken userAuthToken = userAuthentication.getUserAuthToken();
        SharedPreferencesData preferencesData = new SharedPreferencesData(context);
        preferencesData.setUserId(userAuthToken.getToken());
        CustomLog.d("UserDatMapper","Token: "+preferencesData.getUserId());
    }

}
