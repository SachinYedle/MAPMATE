package com.riktam.mapmate.locationsharing.retrofitservices;

import com.riktam.mapmate.locationsharing.responses.CheckIfregisteredResponse;
import com.riktam.mapmate.locationsharing.responses.FriendRequestAcceptResponse;
import com.riktam.mapmate.locationsharing.responses.FriendRequestResponse;
import com.riktam.mapmate.locationsharing.responses.FriendsServiceResponse;
import com.riktam.mapmate.locationsharing.responses.LocationSendingResponse;
import com.riktam.mapmate.locationsharing.responses.LocationSharedFriends;
import com.riktam.mapmate.locationsharing.responses.LocationSharedFriendsResponse;
import com.riktam.mapmate.locationsharing.responses.MailInviteResponse;
import com.riktam.mapmate.locationsharing.responses.SharingStatusResponse;
import com.riktam.mapmate.locationsharing.responses.UserAuthentication;
import com.riktam.mapmate.locationsharing.responses.UserLocationsResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by admin1 on 19/12/16.
 */

public interface RetrofitApiServices {

    @FormUrlEncoded
    @POST("know_where/api/v1/auth")
    Call<UserAuthentication> getUserAuthToken(@Field("access_token") String access_token,
                                              @Field("google_id") String google_id);

    @FormUrlEncoded
    @POST("know_where/api/v1/user/location")
    Call<LocationSendingResponse> sendUserLocation(@Field("lat") String lat,
                                                   @Field("lon") String lon,
                                                   @Field("radius") String radius);

    @GET("know_where/api/v1/user/friend_location")
    Call<UserLocationsResponse> getUserLocations(@Query("friend_id") int friendId);


    @GET("know_where/api/v1/user/friends")
    Call<FriendsServiceResponse> getFriends();

    @GET("know_where/api/v1/user/whoissharing")
    Call<LocationSharedFriendsResponse> getLocationSharedFriends();

    @FormUrlEncoded
    @POST("know_where/api/v1/user/friends")
    Call<FriendRequestResponse> sendRequest(@Field("friend_email") String email);

    @FormUrlEncoded
    @POST("know_where/api/v1/user/update_friends")
    Call<FriendRequestAcceptResponse> acceptRequest(@Field("friend_request_id") int friendRequestId, @Field("friend_id") int friendId);

    @FormUrlEncoded
    @POST("know_where/api/v1/user/toggle_sharing")
    Call<SharingStatusResponse> sendSharingStatus(@Field("sharing") int sharing,
                                                  @Field("friend_id") int friendId);

    @GET("know_where/api/v1/check_register")
    Call<CheckIfregisteredResponse> checkIfRegistered(@Query("friend_email") String email);

    @GET("know_where/api/v1/invite_mail")
    Call<MailInviteResponse> inviteFriend(@Query("friend_email") String email);
}
