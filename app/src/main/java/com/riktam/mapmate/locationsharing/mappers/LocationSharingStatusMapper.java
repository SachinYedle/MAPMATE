package com.riktam.mapmate.locationsharing.mappers;

import android.content.Context;

import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.db.operations.FriendsTableOperations;
import com.riktam.mapmate.locationsharing.db.operations.UserLastknownLocationOperations;
import com.riktam.mapmate.locationsharing.interfaces.PositiveClick;
import com.riktam.mapmate.locationsharing.responses.SharingStatusResponse;
import com.riktam.mapmate.locationsharing.utils.Navigator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by admin1 on 6/6/17.
 */

public class LocationSharingStatusMapper {

    private Context context;

    public LocationSharingStatusMapper(Context context) {
        this.context = context;
    }

    private OnStatusUpdatedListener onStatusUpdatedListener;

    public interface OnStatusUpdatedListener {
        void onTaskCompleted(SharingStatusResponse sharingStatusResponse);

        void onTaskFailed(String request);
    }

    public void updateLocationSharingStatus(OnStatusUpdatedListener onStatusUpdatedListener, int sharing, int friendId) {
        this.onStatusUpdatedListener = onStatusUpdatedListener;
        if (MyApplication.getInstance().isConnectedToInterNet()) {
            //MyApplication.getInstance().showProgressDialog(context.getString(R.string.loading), context.getString(R.string.please_wait));
            Call<SharingStatusResponse> call = MyApplication.getInstance().retrofitApiServices.sendSharingStatus(sharing, friendId);
            call.enqueue(shareStatusCallback);
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

    private Callback<SharingStatusResponse> shareStatusCallback = new
            Callback<SharingStatusResponse>() {

                @Override
                public void onResponse(Call<SharingStatusResponse> call,
                                       Response<SharingStatusResponse>
                                               response) {
                    MyApplication.getInstance().hideProgressDialog();
                    if (response.code() == 401) {
                        onStatusUpdatedListener.onTaskFailed("Session Expired");
                        Navigator.getInstance().navigateToMainActivity();
                    } else if (response.isSuccessful()) {
                        onStatusUpdatedListener.onTaskCompleted(response.body());
                    } else {
                        onStatusUpdatedListener.onTaskFailed(context.getString(R.string.status_update_failed));
                    }

                }

                @Override
                public void onFailure(Call<SharingStatusResponse> call, Throwable t) {
                    MyApplication.getInstance().hideProgressDialog();
                    onStatusUpdatedListener.onTaskFailed("status update failed");
                }
            };
}
