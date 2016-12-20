package com.example.admin1.locationsharing.services;

import com.example.admin1.locationsharing.responses.LocationSendingResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
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
    Call<LocationSendingResponse> sendUserLocation();
}
