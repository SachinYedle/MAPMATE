package com.example.admin1.locationsharing.mappers;

import android.content.Context;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.DaoSession;
import com.example.admin1.locationsharing.db.dao.UserLocations;
import com.example.admin1.locationsharing.db.dao.UserLocationsDao;
import com.example.admin1.locationsharing.db.dao.operations.UserLocationsOperations;
import com.example.admin1.locationsharing.interfaces.PositiveClick;
import com.example.admin1.locationsharing.responses.LocationSendingResponse;
import com.example.admin1.locationsharing.responses.UserLocationData;
import com.example.admin1.locationsharing.responses.UserLocationsResponse;
import com.example.admin1.locationsharing.services.LocationDataService;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.Navigator;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;

import java.util.List;

import de.greenrobot.dao.query.DeleteQuery;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by admin1 on 27/12/16.
 */

public class DownloadLocationsDataMapper {
    private Context context;
    private OnTaskCompletedListener onTaskCompletedListener;
    public DownloadLocationsDataMapper(Context context){
        this.context = context;
    }
    public void getLocations(OnTaskCompletedListener onTaskCompletedListener,String token){
        if (MyApplication.getInstance().isConnectedToInterNet()){
            this.onTaskCompletedListener = onTaskCompletedListener;
            MyApplication.getInstance().showProgressDialog(context.getString(R.string.loading_data),context.getString(R.string.please_wait));
            LocationDataService locationDataService = MyApplication.getInstance().getLocationDataService(token);
            Call<UserLocationsResponse> call = locationDataService.getUserLocations();
            call.enqueue(UserLocationsResponse);
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
    private Callback<UserLocationsResponse> UserLocationsResponse = new
            Callback<UserLocationsResponse>() {

                @Override
                public void onResponse(Call<UserLocationsResponse> call,
                                       Response<UserLocationsResponse>
                                               response) {
                    MyApplication.getInstance().hideProgressDialog();
                    if (response.code() == 401) {
                        onTaskCompletedListener.onTaskFailed("session expired");
                    } else if (response.code() == 504) {
                        onTaskCompletedListener.onTaskFailed("Unknown host");
                    } else if (response.code() == 503) {
                        onTaskCompletedListener.onTaskFailed("Server down");
                    } else if (response.isSuccessful()) {
                        parseUserLocationsResponse(response.body());
                        onTaskCompletedListener.onTaskCompleted(response.body());
                    }
                }

                @Override
                public void onFailure(Call<UserLocationsResponse> call, Throwable t) {
                    MyApplication.getInstance().hideProgressDialog();
                    onTaskCompletedListener.onTaskFailed("Network error");
                    CustomLog.e("UserAutToken callback","user info data service error");
                }
            };
    private void parseUserLocationsResponse(UserLocationsResponse userLocationsResponse){
        List<UserLocationData> userLocationsList = userLocationsResponse.getUserLocationDataList();

        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        UserLocationsDao userLocationsDao = daoSession.getUserLocationsDao();
        SharedPreferencesData preferencesData = new SharedPreferencesData(context);
        DeleteQuery<UserLocations> deleteQuery = userLocationsDao.queryBuilder().where(UserLocationsDao.Properties.Token.eq(preferencesData.getSelectedUserToken()))
                .buildDelete();
        deleteQuery.executeDeleteWithoutDetachingEntities();

        for (int i = 0; i<userLocationsList.size(); i++){
            UserLocationData userLocations = userLocationsList.get(i);
            CustomLog.i("ParseData",userLocations.getLat());
            UserLocationsOperations.addUserLocations(MyApplication.getCurrentActivityContext(),userLocations);
        }

    }
    public interface OnTaskCompletedListener {
        void onTaskCompleted(UserLocationsResponse userLocationsResponse);
        void onTaskFailed(String response);
    }
}
