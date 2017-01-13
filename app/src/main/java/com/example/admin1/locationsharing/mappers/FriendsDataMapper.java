package com.example.admin1.locationsharing.mappers;

import android.content.Context;

import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.Friends;
import com.example.admin1.locationsharing.db.dao.operations.FriendsTableOperations;
import com.example.admin1.locationsharing.responses.FriendsResponse;
import com.example.admin1.locationsharing.responses.FriendsServiceResponse;
import com.example.admin1.locationsharing.services.LocationDataService;
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

    private OnTaskCompletedListener onTaskCompletedListener;
    public FriendsDataMapper(){
        context = MyApplication.getCurrentActivityContext();
    }

    public void getFriends(OnTaskCompletedListener onTaskCompletedListener){
        SharedPreferencesData preferencesData = new SharedPreferencesData(context);
        String token = preferencesData.getUserId();
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
                    } else if (response.isSuccessful()) {
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
        List<FriendsResponse> friendsResponseList = friendsServiceResponse.getFriendsResponseList();
        FriendsResponse friendsResponse;
        for (int i = 0; i < friendsResponseList.size(); i++){
            friendsResponse = friendsResponseList.get(i);
            Friends friends = new Friends();
            friends.setFriend_email(friendsResponse.getFriendEmail());
            friends.setFriend_id(friendsResponse.getFriendId());
            friends.setStatus(""+friendsResponse.getStatus());
            friends.setFriend_request_id(Integer.parseInt(friendsResponse.getFriendRequestId()));
            friends.setRequester_id(friendsResponse.getRequesterId());
            FriendsTableOperations.getInstance().insertFriends(friends);
        }
    }
    public void acceptFriendRequest(OnTaskCompletedListener onTaskCompletedListener,int friendRequestId, int friendId){
        SharedPreferencesData preferencesData = new SharedPreferencesData(context);
        String token = preferencesData.getUserId();
        if (MyApplication.getInstance().isConnectedToInterNet()){
            this.onTaskCompletedListener = onTaskCompletedListener;
            LocationDataService locationDataService = MyApplication.getInstance().getLocationDataService(token);
            Call<FriendsServiceResponse> call = locationDataService.acceptRequest(friendRequestId,friendId);
            call.enqueue(acceptRequestCallback);
        }
    }

    public void checkIfEmailRegistered(OnTaskCompletedListener onTaskCompletedListener,String email){
        SharedPreferencesData preferencesData = new SharedPreferencesData(context);
        String token = preferencesData.getUserId();
        if (MyApplication.getInstance().isConnectedToInterNet()){
            this.onTaskCompletedListener = onTaskCompletedListener;
            LocationDataService locationDataService = MyApplication.getInstance().getLocationDataService(token);
            Call<FriendsServiceResponse> call = locationDataService.checkIfEmailExists(email);
            call.enqueue(checkIfExistCallback);
        }
    }

    private Callback<FriendsServiceResponse> acceptRequestCallback = new
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
                    } else if (response.isSuccessful()) {
                        onTaskCompletedListener.onTaskCompleted(response.body());
                    }
                }
                @Override
                public void onFailure(Call<FriendsServiceResponse> call, Throwable t) {
                    MyApplication.getInstance().hideProgressDialog();
                    onTaskCompletedListener.onTaskFailed("Network error");
                }
            };

    private Callback<FriendsServiceResponse> checkIfExistCallback = new
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
                    } else if (response.isSuccessful()) {
                        onTaskCompletedListener.onTaskCompleted(response.body());
                    }
                }
                @Override
                public void onFailure(Call<FriendsServiceResponse> call, Throwable t) {
                    MyApplication.getInstance().hideProgressDialog();
                    onTaskCompletedListener.onTaskFailed("Network error");
                }
            };
}
