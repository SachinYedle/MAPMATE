package com.riktam.mapmate.locationsharing.mappers;

import android.content.Context;
import android.widget.Toast;

import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.db.dao.DaoSession;
import com.riktam.mapmate.locationsharing.db.dao.UserLocations;
import com.riktam.mapmate.locationsharing.db.dao.UserLocationsDao;
import com.riktam.mapmate.locationsharing.db.operations.FriendsTableOperations;
import com.riktam.mapmate.locationsharing.db.operations.UserLastknownLocationOperations;
import com.riktam.mapmate.locationsharing.db.operations.UserLocationsOperations;
import com.riktam.mapmate.locationsharing.interfaces.PositiveClick;
import com.riktam.mapmate.locationsharing.responses.UserLocationData;
import com.riktam.mapmate.locationsharing.responses.UserLocationsResponse;
import com.riktam.mapmate.locationsharing.utils.CustomLog;
import com.riktam.mapmate.locationsharing.utils.Navigator;

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
    public void getLocations(OnTaskCompletedListener onTaskCompletedListener,String friendId){
        if (MyApplication.getInstance().isConnectedToInterNet()){
            this.onTaskCompletedListener = onTaskCompletedListener;
            MyApplication.getInstance().showProgressDialog(context.getString(R.string.loading_data),context.getString(R.string.please_wait));
            Call<UserLocationsResponse> call = MyApplication.getInstance().retrofitApiServices.getUserLocations(Integer.parseInt(friendId));
            call.enqueue(UserLocationsResponse);
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
    private Callback<UserLocationsResponse> UserLocationsResponse = new
            Callback<UserLocationsResponse>() {

                @Override
                public void onResponse(Call<UserLocationsResponse> call,
                                       Response<UserLocationsResponse>
                                               response) {
                    MyApplication.getInstance().hideProgressDialog();
                    if (response.code() == 401) {
                        onTaskCompletedListener.onTaskFailed("session expired");
                        Navigator.getInstance().navigateToMainActivity();
                    }if (response.code() == 404) {
                        onTaskCompletedListener.onTaskFailed("page Not Found");
                    }else if (response.code() == 504) {
                        onTaskCompletedListener.onTaskFailed("Unknown host");
                    } else if (response.code() == 503) {
                        onTaskCompletedListener.onTaskFailed("Server down");
                    } else if (response.isSuccessful() && response.code() == 200  && response.body().getSuccess().equalsIgnoreCase("true")) {
                        parseUserLocationsResponse(response.body() );
                        onTaskCompletedListener.onTaskCompleted(response.body());
                    }else {
                        onTaskCompletedListener.onTaskFailed(response.body().getMessage());
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
        DeleteQuery<UserLocations> deleteQuery = userLocationsDao.queryBuilder()
                .where(UserLocationsDao.Properties.Email.eq(MyApplication.getInstance().sharedPreferencesData.getSelectedUserEmail()))
                .buildDelete();
        deleteQuery.executeDeleteWithoutDetachingEntities();

        if (userLocationsList.size() <= 0) {
            Toast.makeText(MyApplication.getCurrentActivityContext(),"No Locations are available",Toast.LENGTH_SHORT).show();
        }
        for (int i = 0; i<userLocationsList.size(); i++){
            UserLocationData userLocations = userLocationsList.get(i);
            CustomLog.i("ParseData",userLocations.getLat());
            UserLocationsOperations.getInstance().addUserLocations(userLocations);
        }
    }
    public interface OnTaskCompletedListener {
        void onTaskCompleted(UserLocationsResponse userLocationsResponse);
        void onTaskFailed(String response);
    }
}
