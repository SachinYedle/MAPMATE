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
import com.riktam.mapmate.locationsharing.utils.Navigator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by admin1 on 13/1/17.
 */

public class FriendsDataMapper {
    private Context context;

    public interface OnTaskCompletedListener {
        void onTaskCompleted(FriendsServiceResponse friendsServiceResponse);

        void onTaskFailed(String response);
    }

    public interface OnRequestSentListener {
        void onTaskCompleted(FriendRequestResponse friendRequestResponse);

        void onTaskFailed(String response);
    }

    public interface OnRequestAcceptedListener {
        void onTaskCompleted(FriendRequestAcceptResponse friendRequestAcceptResponse);

        void onTaskFailed(String response);
    }

    private OnTaskCompletedListener onTaskCompletedListener;
    private OnRequestAcceptedListener onRequestAcceptedListener;
    private OnRequestSentListener onRequestSentListener;

    public FriendsDataMapper() {
        context = MyApplication.getCurrentActivityContext();
    }

    public void getFriends(OnTaskCompletedListener onTaskCompletedListener, boolean isRefreshing) {
        String token = MyApplication.getInstance().sharedPreferencesData.getUserToken();
        if (MyApplication.getInstance().isConnectedToInterNet()) {
            this.onTaskCompletedListener = onTaskCompletedListener;
            if (!isRefreshing) {
                MyApplication.getInstance().showProgressDialog(context.getString(R.string.loading_data), context.getString(R.string.please_wait));
            }
            Call<FriendsServiceResponse> call = MyApplication.getInstance().retrofitApiServices.getFriends();
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

    private Callback<FriendsServiceResponse> getFriendsServiceResponseCallback = new
            Callback<FriendsServiceResponse>() {
                @Override
                public void onResponse(Call<FriendsServiceResponse> call,
                                       Response<FriendsServiceResponse>
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
                public void onFailure(Call<FriendsServiceResponse> call, Throwable t) {
                    MyApplication.getInstance().hideProgressDialog();
                    onTaskCompletedListener.onTaskFailed("Network error");
                }
            };

    private void parseFriendsData(FriendsServiceResponse friendsServiceResponse) {
        FriendsTableOperations.getInstance().deleteFriendsTableData();
        // UserLastknownLocationOperations.getInstance().deleteUserLastKnownData();
        List<FriendsResponse> friendsResponseList = friendsServiceResponse.getFriendsResponseList();
        FriendsResponse friendsResponse;
        for (int i = 0; i < friendsResponseList.size(); i++) {
            friendsResponse = friendsResponseList.get(i);
            Friends friends = new Friends();
            friends.setFriend_email(friendsResponse.getFriendEmail());
            friends.setFriend_first_name(friendsResponse.getFriendFirstName());
            friends.setFriend_id(friendsResponse.getFriendId());
            friends.setStatus("" + friendsResponse.getStatus());
            friends.setFriend_request_id(Integer.parseInt(friendsResponse.getFriendRequestId()));
            friends.setRequester_id(friendsResponse.getRequesterId());
            friends.setSharing(friendsResponse.getSharing());
            friends.setFriend_profile_url(friendsResponse.getFriendProfileUrl());
            FriendsTableOperations.getInstance().insertFriends(friends);
            if (friendsResponse.getStatus() == 1) {
                UserLastKnownLocation userLastKnownLocation = new UserLastKnownLocation();
                userLastKnownLocation.setFriend_first_name(friendsResponse.getFriendFirstName());
                userLastKnownLocation.setEmail(friendsResponse.getFriendEmail());
                userLastKnownLocation.setLatitude(friendsResponse.getLat());
                userLastKnownLocation.setLongitude(friendsResponse.getLon());
                userLastKnownLocation.setTime(friendsResponse.getUpdatedAt());
                userLastKnownLocation.setLast_known_time(friendsResponse.getLastKnownTime());
                userLastKnownLocation.setSharing(friendsResponse.getSharing());
                userLastKnownLocation.setFriend_profile(friendsResponse.getFriendProfileUrl());
                //UserLastknownLocationOperations.getInstance().insertUsersLastKnownLocation(userLastKnownLocation);
            }
        }
    }

    public void acceptFriendRequest(OnRequestAcceptedListener onRequestAcceptedListener, int friendRequestId, int friendId) {
        String token = MyApplication.getInstance().sharedPreferencesData.getUserToken();
        if (MyApplication.getInstance().isConnectedToInterNet()) {
            this.onRequestAcceptedListener = onRequestAcceptedListener;

            Call<FriendRequestAcceptResponse> call = MyApplication.getInstance().retrofitApiServices.acceptRequest(friendRequestId, friendId);
            call.enqueue(acceptRequestCallback);
        }
    }

    public void sendRequest(OnRequestSentListener onRequestSentListener, String email) {
        String token = MyApplication.getInstance().sharedPreferencesData.getUserToken();
        if (MyApplication.getInstance().isConnectedToInterNet()) {
            this.onRequestSentListener = onRequestSentListener;
            Call<FriendRequestResponse> call = MyApplication.getInstance().retrofitApiServices.sendRequest(email);
            call.enqueue(sendRequestCallback);
        }
    }

    private Callback<FriendRequestAcceptResponse> acceptRequestCallback = new
            Callback<FriendRequestAcceptResponse>() {
                @Override
                public void onResponse(Call<FriendRequestAcceptResponse> call,
                                       Response<FriendRequestAcceptResponse>
                                               response) {
                    MyApplication.getInstance().hideProgressDialog();
                    if (response.code() == 401) {
                        onRequestAcceptedListener.onTaskFailed("session expired");
                        Navigator.getInstance().navigateToMainActivity();
                    } else if (response.code() == 504) {
                        onRequestAcceptedListener.onTaskFailed("Unknown host");
                    } else if (response.code() == 503) {
                        onRequestAcceptedListener.onTaskFailed("Server down");
                    } else if (response.isSuccessful() && response.code() == 200) {
                        onRequestAcceptedListener.onTaskCompleted(response.body());
                    }
                }

                @Override
                public void onFailure(Call<FriendRequestAcceptResponse> call, Throwable t) {
                    MyApplication.getInstance().hideProgressDialog();
                    onRequestAcceptedListener.onTaskFailed("Network error");
                }
            };

    private Callback<FriendRequestResponse> sendRequestCallback = new
            Callback<FriendRequestResponse>() {
                @Override
                public void onResponse(Call<FriendRequestResponse> call,
                                       Response<FriendRequestResponse>
                                               response) {
                    MyApplication.getInstance().hideProgressDialog();
                    if (response.code() == 401) {
                        onRequestSentListener.onTaskFailed("session expired");
                        Navigator.getInstance().navigateToMainActivity();
                    } else if (response.code() == 504) {
                        onRequestSentListener.onTaskFailed("Unknown host");
                    } else if (response.code() == 503) {
                        onRequestSentListener.onTaskFailed("Server down");
                    } else if (response.isSuccessful() && response.code() == 200) {
                        onRequestSentListener.onTaskCompleted(response.body());
                    }
                }

                @Override
                public void onFailure(Call<FriendRequestResponse> call, Throwable t) {
                    MyApplication.getInstance().hideProgressDialog();
                    onRequestSentListener.onTaskFailed("Network error");
                }
            };
}
