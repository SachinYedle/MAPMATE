package com.riktam.mapmate.locationsharing.mappers;

import android.content.Context;

import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.db.dao.Friends;
import com.riktam.mapmate.locationsharing.db.dao.UserLastKnownLocation;
import com.riktam.mapmate.locationsharing.db.operations.FriendsTableOperations;
import com.riktam.mapmate.locationsharing.db.operations.UserLastknownLocationOperations;
import com.riktam.mapmate.locationsharing.interfaces.PositiveClick;
import com.riktam.mapmate.locationsharing.responses.FriendRequestAcceptResponse;
import com.riktam.mapmate.locationsharing.responses.FriendRequestResponse;
import com.riktam.mapmate.locationsharing.responses.FriendsResponse;
import com.riktam.mapmate.locationsharing.responses.FriendsServiceResponse;
import com.riktam.mapmate.locationsharing.responses.LocationSharedFriends;
import com.riktam.mapmate.locationsharing.responses.LocationSharedFriendsResponse;
import com.riktam.mapmate.locationsharing.utils.Navigator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by admin1 on 9/6/17.
 */

public class LocationSharedFriendsMapper {

    private Context context;

    public interface OnTaskCompletedListener {
        void onTaskCompleted(LocationSharedFriendsResponse friendsServiceResponse);

        void onTaskFailed(String response);
    }

    private OnTaskCompletedListener onTaskCompletedListener;

    public LocationSharedFriendsMapper() {
        context = MyApplication.getCurrentActivityContext();
    }

    public void getLocationSharedFriends(OnTaskCompletedListener onTaskCompletedListener, boolean isRefreshing) {
        if (MyApplication.getInstance().isConnectedToInterNet()) {
            this.onTaskCompletedListener = onTaskCompletedListener;
            if (!isRefreshing) {
                MyApplication.getInstance().showProgressDialog(context.getString(R.string.loading_data), context.getString(R.string.please_wait));
            }
            Call<LocationSharedFriendsResponse> call = MyApplication.getInstance().retrofitApiServices.getLocationSharedFriends();
            call.enqueue(getFriendsServiceResponseCallback);
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

    private Callback<LocationSharedFriendsResponse> getFriendsServiceResponseCallback = new
            Callback<LocationSharedFriendsResponse>() {
                @Override
                public void onResponse(Call<LocationSharedFriendsResponse> call,
                                       Response<LocationSharedFriendsResponse>
                                               response) {
                    MyApplication.getInstance().hideProgressDialog();
                    if (response.code() == 401) {
                        onTaskCompletedListener.onTaskFailed("session expired");
                        Navigator.getInstance().navigateToMainActivity();
                    } else if (response.code() == 504) {
                        onTaskCompletedListener.onTaskFailed("Unknown host");
                    } else if (response.code() == 503) {
                        onTaskCompletedListener.onTaskFailed("Server down");
                    } else if (response.isSuccessful() && response.code() == 200) {
                        parseFriendsData(response.body());
                        onTaskCompletedListener.onTaskCompleted(response.body());
                    }
                }

                @Override
                public void onFailure(Call<LocationSharedFriendsResponse> call, Throwable t) {
                    MyApplication.getInstance().hideProgressDialog();
                    onTaskCompletedListener.onTaskFailed("Network error");
                }
            };

    private void parseFriendsData(LocationSharedFriendsResponse friendsServiceResponse) {
        //FriendsTableOperations.getInstance().deleteFriendsTableData();
        UserLastknownLocationOperations.getInstance().deleteUserLastKnownData();
        List<LocationSharedFriends> friendsResponseList = friendsServiceResponse.getSharedFriends();
        LocationSharedFriends friendsResponse;
        for (int i = 0; i < friendsResponseList.size(); i++) {
            friendsResponse = friendsResponseList.get(i);
            UserLastKnownLocation userLastKnownLocation = new UserLastKnownLocation();
            userLastKnownLocation.setFriend_first_name(friendsResponse.getFriendFirstName());
            userLastKnownLocation.setEmail(friendsResponse.getFriendEmail());
            userLastKnownLocation.setLatitude(friendsResponse.getLat());
            userLastKnownLocation.setLongitude(friendsResponse.getLon());
            userLastKnownLocation.setLast_known_time(friendsResponse.getLastKnownTime());
            userLastKnownLocation.setFriend_profile(friendsResponse.getFriendProfileUrl());
            UserLastknownLocationOperations.getInstance().insertUsersLastKnownLocation(userLastKnownLocation);
        }
    }
}
