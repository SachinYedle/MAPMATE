package com.riktam.mapmate.locationsharing.mappers;

import android.content.Context;

import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.db.operations.FriendsTableOperations;
import com.riktam.mapmate.locationsharing.db.operations.UserLastknownLocationOperations;
import com.riktam.mapmate.locationsharing.interfaces.PositiveClick;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.responses.LocationSendingResponse;
import com.riktam.mapmate.locationsharing.utils.Navigator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by admin1 on 19/12/16.
 */

public class UploadLocationsDataMapper {
    private Context context;
    private OnTaskCompletedListener onTaskCompletedListener;

    public UploadLocationsDataMapper(Context context) {
        this.context = context;
    }

    public void sendUserLocation(OnTaskCompletedListener onTaskCompletedListener, String lat, String lon, String radius) {


        if (MyApplication.getInstance().isConnectedToInterNet()) {
            this.onTaskCompletedListener = onTaskCompletedListener;
            Call<LocationSendingResponse> call = MyApplication.getInstance().retrofitApiServices.sendUserLocation(lat, lon, radius);
            call.enqueue(locationSendingResponseCallback);
        } else {
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

    private Callback<LocationSendingResponse> locationSendingResponseCallback = new
            Callback<LocationSendingResponse>() {

                @Override
                public void onResponse(Call<LocationSendingResponse> call,
                                       Response<LocationSendingResponse>
                                               response) {
                    if (response.code() == 401) {
                        onTaskCompletedListener.onTaskFailed("session expired");
                        Navigator.getInstance().navigateToMainActivity();
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
