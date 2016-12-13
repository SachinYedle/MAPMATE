package com.example.admin1.locationsharing.mappers;

import android.content.Context;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.Services.UserDataService;
import com.example.admin1.locationsharing.acitivities.MapActivity;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.DaoSession;
import com.example.admin1.locationsharing.db.dao.UserLastKnownLocation;
import com.example.admin1.locationsharing.db.dao.UserLastKnownLocationDao;
import com.example.admin1.locationsharing.db.dao.UserLocations;
import com.example.admin1.locationsharing.db.dao.UserLocationsDao;
import com.example.admin1.locationsharing.db.dao.operations.UserLastknownLocationOperations;
import com.example.admin1.locationsharing.db.dao.operations.UsersLast30MinLocationsOperation;
import com.example.admin1.locationsharing.interfaces.PositiveClick;
import com.example.admin1.locationsharing.responses.UserData;
import com.example.admin1.locationsharing.responses.UserInfo;
import com.example.admin1.locationsharing.responses.UsersLast30MinLocations;
import com.example.admin1.locationsharing.responses.UsersLastLocations;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.Navigator;

import java.util.ArrayList;
import java.util.List;

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

    public void getUserLastKnownLocation(){
        if (MyApplication.getInstance().isConnectedToInterNet()){
            UserDataService userDataService = MyApplication.getInstance().getUserDataService();
            Call<UserInfo> call = userDataService.getUserData();
            call.enqueue(userDataCallback);
        }else{
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

    public void getUsersLast30MinLocations(){
        if (MyApplication.getInstance().isConnectedToInterNet()){
            UserDataService userDataService = MyApplication.getInstance().getUserDataService();
            Call<UsersLastLocations> call = userDataService.getUsersLastLocation();
            call.enqueue(userLast30MinLocationCallback);
        }
    }

    private Callback<UsersLastLocations> userLast30MinLocationCallback = new
            Callback<UsersLastLocations>() {

                @Override
                public void onResponse(Call<UsersLastLocations> call,
                                       Response<UsersLastLocations>
                                               response) {
                    if (response.isSuccessful()) {
                        parseUsersLastLocationsDataResponse(response.body());
                        CustomLog.i("Response",""+response);
                    } else {
                        CustomLog.e("UserInfo callback","user info data service error");
                    }

                }

                @Override
                public void onFailure(Call<UsersLastLocations> call, Throwable t) {
                    CustomLog.e("UserInfo callback","user info data service error");
                }
            };

    private Callback<UserInfo> userDataCallback = new
            Callback<UserInfo>() {
                @Override
                public void onResponse(Call<UserInfo> call,
                                       Response<UserInfo>
                                               response) {
                    if (response.isSuccessful()) {
                        parseUserDataResponse(response.body());
                        CustomLog.i("Response",""+response);
                    } else {
                        CustomLog.e("UserInfo callback","user info data service error");
                        MyApplication.getInstance().hideProgressDialog();
                        Toast.makeText(MyApplication.getCurrentActivityContext(),"Something wnt wrong try again..",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UserInfo> call, Throwable t) {
                    CustomLog.e("UserInfo callback","user info data service error");
                    MyApplication.getInstance().hideProgressDialog();
                    Toast.makeText(MyApplication.getCurrentActivityContext(),"Something wnt wrong try again..",Toast.LENGTH_SHORT).show();
                }
            };
    private void parseUserDataResponse(UserInfo userInfo){
        ArrayList<UserData> userDataList = userInfo.getUserDataArrayList();
        for (int i = 0; i < userDataList.size(); i++) {
            List<UserLastKnownLocation> userLastKnownLocation = UserLastknownLocationOperations.getUserLastKnownLocation(context,userDataList.get(i).getPhone());
            if(userLastKnownLocation.size()> 0){
                updateUserDataInDb(userDataList.get(i));
            }
            else {
                insertUserDataInDb(userDataList.get(i));
            }
        }
        ((MapActivity)context).setFriendsLocationMarkers();
    }
    private void parseUsersLastLocationsDataResponse(UsersLastLocations usersLastLocations){
        ArrayList<UsersLast30MinLocations> last30MinLocationsArrayList = usersLastLocations.getUsersLast30MinLocations();
        List<UserLocations> userLocations = UsersLast30MinLocationsOperation.getUsersLast30MinLocations(context,last30MinLocationsArrayList.get(0).getPhone());
        for(int i = 0; i < userLocations.size(); i++){
            UsersLast30MinLocationsOperation.deleteUsersLast30MinLocations(context,userLocations.get(i).getId());
        }

        for (int i = 0; i < last30MinLocationsArrayList.size(); i++) {
            insertLast30MinLocationsInDb(last30MinLocationsArrayList.get(i));
        }
        ((MapActivity)context).drawRouteOfSelectedUser();
    }

    private void insertLast30MinLocationsInDb(UsersLast30MinLocations last30MinLocations){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        UserLocationsDao userLocationsDao = daoSession.getUserLocationsDao();
        UserLocations userLocations = new UserLocations();
        userLocations.setName(last30MinLocations.getName());
        userLocations.setLongitude(last30MinLocations.getLongitude());
        userLocations.setLatitude(last30MinLocations.getLatitude());
        userLocations.setTime(last30MinLocations.getTime());
        userLocations.setPhone(last30MinLocations.getPhone());
        userLocationsDao.insert(userLocations);

    }
    private void insertUserDataInDb(UserData userData) {
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        UserLastKnownLocationDao userLastKnownLocationDao = daoSession.getUserLastKnownLocationDao();
        UserLastKnownLocation userDataTable = new UserLastKnownLocation();
        userDataTable.setName(userData.getName());
        userDataTable.setPhone(userData.getPhone());
        userDataTable.setLatitude(userData.getLatitude());
        userDataTable.setLongitude(userData.getLongitude());
        userLastKnownLocationDao.insert(userDataTable);
    }
    private void updateUserDataInDb(UserData userData) {
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        UserLastKnownLocationDao userLastKnownLocationDao = daoSession.getUserLastKnownLocationDao();
        UserLastKnownLocation userDataTable = new UserLastKnownLocation();
        userDataTable.setName(userData.getName());
        userDataTable.setPhone(userData.getPhone());
        userDataTable.setLatitude(userData.getLatitude());
        userDataTable.setLongitude(userData.getLongitude());
        userLastKnownLocationDao.update(userDataTable);
    }

}
