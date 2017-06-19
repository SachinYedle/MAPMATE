package com.riktam.mapmate.locationsharing.mappers;

import android.content.Context;

import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.responses.CheckIfregisteredResponse;
import com.riktam.mapmate.locationsharing.utils.Navigator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by admin1 on 14/6/17.
 */

public class CheckIfUserRegisteredMapper {

    private Context context;

    private OnTaskCompletedListener onTaskCompletedListener;

    public interface OnTaskCompletedListener {
        void onTaskCompleted(CheckIfregisteredResponse checkIfregisteredResponse);

        void onTaskFailed(String response);
    }

    public CheckIfUserRegisteredMapper() {
        context = MyApplication.getCurrentActivityContext();
    }

    public void checkIfRegistered(OnTaskCompletedListener onTaskCompletedListener, String email) {
        if (MyApplication.getInstance().isConnectedToInterNet()) {
            this.onTaskCompletedListener = onTaskCompletedListener;
            MyApplication.getInstance().showProgressDialog(context.getString(R.string.please_wait));
            Call<CheckIfregisteredResponse> call = MyApplication.getInstance().retrofitApiServices.checkIfRegistered(email);
            call.enqueue(checkIfRegisteredCallback);
        }
    }

    private Callback<CheckIfregisteredResponse> checkIfRegisteredCallback = new
            Callback<CheckIfregisteredResponse>() {
                @Override
                public void onResponse(Call<CheckIfregisteredResponse> call,
                                       Response<CheckIfregisteredResponse>
                                               response) {
                    MyApplication.getInstance().hideProgressDialog();
                    if (response.code() == 401) {
                        onTaskCompletedListener.onTaskFailed("session expired");
                        Navigator.getInstance().navigateToMainActivity();
                    } else if (response.code() == 504) {
                        onTaskCompletedListener.onTaskFailed("Unknown host");
                    } else if (response.code() == 503) {
                        onTaskCompletedListener.onTaskFailed("Server down");
                    } else if (response.isSuccessful() && response.code() == 200 && response.body().isSuccess()) {
                        onTaskCompletedListener.onTaskCompleted(response.body());
                    }else {
                        onTaskCompletedListener.onTaskFailed("Requested email hasn't registered yet");
                    }
                }

                @Override
                public void onFailure(Call<CheckIfregisteredResponse> call, Throwable t) {
                    MyApplication.getInstance().hideProgressDialog();
                    onTaskCompletedListener.onTaskFailed("Network error");
                }
            };
}
