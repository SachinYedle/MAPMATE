package com.example.admin1.locationsharing.services;

import com.example.admin1.locationsharing.db.dao.UserLocations;
import com.example.admin1.locationsharing.responses.FriendRequestAcceptResponse;
import com.example.admin1.locationsharing.responses.FriendRequestResponse;
import com.example.admin1.locationsharing.responses.FriendsServiceResponse;
import com.example.admin1.locationsharing.responses.LocationSendingResponse;
import com.example.admin1.locationsharing.responses.UserLocationsResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by admin1 on 19/12/16.
 */

public interface LocationDataService {
    @FormUrlEncoded
    @POST("know_where/api/v1/user/location")
    Call<LocationSendingResponse> sendUserLocation(@Field("lat") String lat,
                                                   @Field("lon") String lon,
                                                   @Field("radius") String radius);

    @GET("know_where/api/v1/user/location")
    Call<UserLocationsResponse> getUserLocations(@Header("email") String email);


    @GET("know_where/api/v1/user/friends")
    Call<FriendsServiceResponse> getFriends();

    @FormUrlEncoded
    @POST("know_where/api/v1/user/friends")
    Call<FriendRequestResponse> sendRequest(@Field("friend_email") String email);

    @FormUrlEncoded
    @POST("know_where/api/v1/user/update_friends")
    Call<FriendRequestAcceptResponse> acceptRequest(@Field("friend_request_id") int friendRequestId, @Field("friend_id") int friendId);
}
