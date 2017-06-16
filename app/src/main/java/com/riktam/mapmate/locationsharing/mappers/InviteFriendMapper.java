package com.riktam.mapmate.locationsharing.mappers;

import android.content.Context;
import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.responses.MailInviteResponse;
import com.riktam.mapmate.locationsharing.utils.Navigator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by admin1 on 15/6/17.
 */

public class InviteFriendMapper {

    private Context context;

    private OnInviteListener onInviteListener;

    public interface OnInviteListener {
        void onTaskCompleted(MailInviteResponse checkIfregisteredResponse);

        void onTaskFailed(String response);
    }

    public InviteFriendMapper() {
        context = MyApplication.getCurrentActivityContext();
    }

    public void inviteFriend(OnInviteListener onInviteListener, String email) {
        if (MyApplication.getInstance().isConnectedToInterNet()) {
            this.onInviteListener = onInviteListener;
            MyApplication.getInstance().showProgressDialog(context.getString(R.string.loading_data), context.getString(R.string.please_wait));
            Call<MailInviteResponse> call = MyApplication.getInstance().retrofitApiServices.inviteFriend(email);
            call.enqueue(checkIfRegisteredCallback);
        }
    }

    private Callback<MailInviteResponse> checkIfRegisteredCallback = new
            Callback<MailInviteResponse>() {
                @Override
                public void onResponse(Call<MailInviteResponse> call,
                                       Response<MailInviteResponse>
                                               response) {
                    MyApplication.getInstance().hideProgressDialog();
                    if (response.code() == 401) {
                        onInviteListener.onTaskFailed("session expired");
                        Navigator.getInstance().navigateToMainActivity();
                    } else if (response.code() == 504) {
                        onInviteListener.onTaskFailed("Unknown host");
                    } else if (response.code() == 503) {
                        onInviteListener.onTaskFailed("Server down");
                    } else if (response.isSuccessful() && response.code() == 200 && response.body().isSuccess()) {
                        onInviteListener.onTaskCompleted(response.body());
                    }else {
                        onInviteListener.onTaskFailed("Requested email hasn't registered yet");
                    }
                }

                @Override
                public void onFailure(Call<MailInviteResponse> call, Throwable t) {
                    MyApplication.getInstance().hideProgressDialog();
                    onInviteListener.onTaskFailed("Network error");
                }
            };
}
