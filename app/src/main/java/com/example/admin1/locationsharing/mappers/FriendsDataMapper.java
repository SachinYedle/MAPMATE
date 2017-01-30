package com.example.admin1.locationsharing.mappers;

import android.content.Context;

import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.Friends;
import com.example.admin1.locationsharing.db.dao.UserLastKnownLocation;
import com.example.admin1.locationsharing.db.dao.operations.FriendsTableOperations;
import com.example.admin1.locationsharing.db.dao.operations.UserLastknownLocationOperations;
import com.example.admin1.locationsharing.responses.FriendRequestAcceptResponse;
import com.example.admin1.locationsharing.responses.FriendRequestResponse;
import com.example.admin1.locationsharing.responses.FriendsResponse;
import com.example.admin1.locationsharing.responses.FriendsServiceResponse;
import com.example.admin1.locationsharing.services.LocationDataService;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;

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
    public FriendsDataMapper(){
        context = MyApplication.getCurrentActivityContext();
    }

    public void getFriends(OnTaskCompletedListener onTaskCompletedListener){
        SharedPreferencesData preferencesData = new SharedPreferencesData(context);
        String token = preferencesData.getUserToken();
        if (MyApplication.getInstance().isConnectedToInterNet()){
            this.onTaskCompletedListener = onTaskCompletedListener;
            LocationDataService locationDataService = MyApplication.getInstance().getLocationDataService(token);
            MyApplication.getInstance().showProgressDialog("Loading friends data","please wait");
            Call<FriendsServiceResponse> call = locationDataService.getFriends();
            call.enqueue(getFriendsServiceResponseCallback);
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

    private void parseFriendsData(FriendsServiceResponse friendsServiceResponse){
        FriendsTableOperations.getInstance().deleteFriendsTableData();
        UserLastknownLocationOperations.getInstance().deleteUserLastKnownData();
        List<FriendsResponse> friendsResponseList = friendsServiceResponse.getFriendsResponseList();
        FriendsResponse friendsResponse;
        for (int i = 0; i < friendsResponseList.size(); i++){
            friendsResponse = friendsResponseList.get(i);
            Friends friends = new Friends();
            friends.setFriend_email(friendsResponse.getFriendEmail());
            friends.setFriend_first_name(friendsResponse.getFriendFirstName());
            friends.setFriend_id(friendsResponse.getFriendId());
            friends.setStatus(""+friendsResponse.getStatus());
            friends.setFriend_request_id(Integer.parseInt(friendsResponse.getFriendRequestId()));
            friends.setRequester_id(friendsResponse.getRequesterId());
            FriendsTableOperations.getInstance().insertFriends(friends);
            if(friendsResponse.getStatus() == 1){
                UserLastKnownLocation userLastKnownLocation = new UserLastKnownLocation ();
                userLastKnownLocation.setFriend_first_name(friendsResponse.getFriendFirstName());
                userLastKnownLocation.setEmail(friendsResponse.getFriendEmail());
                userLastKnownLocation.setLatitude(friendsResponse.getFriendLocation().getLat());
                userLastKnownLocation.setLongitude(friendsResponse.getFriendLocation().getLon());
                userLastKnownLocation.setTime(friendsResponse.getUpdatedAt());
                UserLastknownLocationOperations.getInstance().insertUsersLastKnownLocation(userLastKnownLocation);
            }
        }
    }
    public void acceptFriendRequest(OnRequestAcceptedListener onRequestAcceptedListener,int friendRequestId, int friendId){
        SharedPreferencesData preferencesData = new SharedPreferencesData(context);
        String token = preferencesData.getUserToken();
        if (MyApplication.getInstance().isConnectedToInterNet()){
            this.onRequestAcceptedListener = onRequestAcceptedListener;
            LocationDataService locationDataService = MyApplication.getInstance().getLocationDataService(token);
            Call<FriendRequestAcceptResponse> call = locationDataService.acceptRequest(friendRequestId,friendId);
            call.enqueue(acceptRequestCallback);
        }
    }

    public void sendRequest(OnRequestSentListener onRequestSentListener,String email){
        SharedPreferencesData preferencesData = new SharedPreferencesData(context);
        String token = preferencesData.getUserToken();
        if (MyApplication.getInstance().isConnectedToInterNet()){
            this.onRequestSentListener = onRequestSentListener;
            LocationDataService locationDataService = MyApplication.getInstance().getLocationDataService(token);
            Call<FriendRequestResponse> call = locationDataService.sendRequest(email);
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
