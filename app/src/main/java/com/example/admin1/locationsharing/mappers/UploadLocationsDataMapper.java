package com.example.admin1.locationsharing.mappers;

import android.content.Context;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.interfaces.PositiveClick;
import com.example.admin1.locationsharing.services.LocationDataService;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.responses.LocationSendingResponse;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.Navigator;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by admin1 on 19/12/16.
 */

public class UploadLocationsDataMapper {
    private Context context;
    private OnTaskCompletedListener onTaskCompletedListener;
    public UploadLocationsDataMapper(Context context){
        this.context = context;
    }

    public void sendUserLocation(OnTaskCompletedListener onTaskCompletedListener,String lat,String lon,String radius){

        SharedPreferencesData preferencesData = new SharedPreferencesData(context);
        String token = preferencesData.getUserToken();

        if (MyApplication.getInstance().isConnectedToInterNet()){
            this.onTaskCompletedListener = onTaskCompletedListener;
            LocationDataService locationDataService = MyApplication.getInstance().getLocationDataService(token);
            Call<LocationSendingResponse> call = locationDataService.sendUserLocation(lat,lon,radius);
            call.enqueue(locationSendingResponseCallback);
        }
    }
    private Callback<LocationSendingResponse> locationSendingResponseCallback = new
            Callback<LocationSendingResponse>() {

                @Override
                public void onResponse(Call<LocationSendingResponse> call,
                                       Response<LocationSendingResponse>
                                               response) {
                    if (response.code() == 401) {
                        onTaskCompletedListener.onTaskFailed("session expired");
                    } else if (response.code() == 504) {
                        onTaskCompletedListener.onTaskFailed("Unknown host");
                    } else if (response.code() == 503) {
                        onTaskCompletedListener.onTaskFailed("Server down");
                    } else if (response.isSuccessful()) {
                        onTaskCompletedListener.onTaskCompleted(response.body());
                    }
                }

                @Override
                public void onFailure(Call<LocationSendingResponse> call, Throwable t) {
                    onTaskCompletedListener.onTaskFailed("Network error");
                }
            };

    public interface OnTaskCompletedListener {
        void onTaskCompleted(LocationSendingResponse locationSendingResponse);
        void onTaskFailed(String response);
    }
}
