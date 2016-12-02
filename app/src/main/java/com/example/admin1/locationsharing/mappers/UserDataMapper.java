package com.example.admin1.locationsharing.mappers;

import android.content.Context;

import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.DaoSession;
import com.example.admin1.locationsharing.db.dao.UserDataTable;
import com.example.admin1.locationsharing.db.dao.UserDataTableDao;
import com.example.admin1.locationsharing.pojo.UserData;
import com.example.admin1.locationsharing.pojo.UserInfo;
import com.example.admin1.locationsharing.services.UserDataService;
import com.example.admin1.locationsharing.utils.CustomLog;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by admin1 on 2/12/16.
 */

public class UserDataMapper {

    private Context context;
    public UserDataMapper(Context context){
        this.context = context;
    }
    public void getUserData(){
        if (MyApplication.getInstance().isConnectedToInterNet()){
            UserDataService userDataService = MyApplication.getInstance().getUserDataService();
            Call<UserInfo> call = userDataService.getUserData();
            call.enqueue(userDataCallback);
        }
    }
    private Callback<UserInfo> userDataCallback = new
            Callback<UserInfo>() {

                @Override
                public void onResponse(Call<UserInfo> call,
                                       Response<UserInfo>
                                               response) {
                    if (response.isSuccessful()) {
                        parseUserDataResponse(response.body());
                    } else {
                        CustomLog.e("UserInfo callback","user info data service error");
                    }
                }

                @Override
                public void onFailure(Call<UserInfo> call, Throwable t) {
                    CustomLog.e("UserInfo callback","user info data service error");
                }
            };
    private void parseUserDataResponse(UserInfo userInfo){
        List<UserData> userDataList = userInfo.getUserData();
        for (int i = 0; i < userDataList.size(); i++) {
               insertUserDataInDb(userDataList.get(i));
        }
    }
    private void insertUserDataInDb(UserData userData) {
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        UserDataTableDao userDataTableDao = daoSession.getUserDataTableDao();
        UserDataTable userDataTable = new UserDataTable();
        userDataTable.setName(userData.getName());
        userDataTable.setPhone(userData.getPhone());
        userDataTable.setLatitude(userData.getLatitude());
        userDataTable.setLongitude(userData.getLongitude());
        userDataTableDao.insert(userDataTable);
    }
}

