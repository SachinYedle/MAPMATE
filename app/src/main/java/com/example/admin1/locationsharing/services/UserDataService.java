package com.example.admin1.locationsharing.services;

import com.example.admin1.locationsharing.responses.UserAuthentication;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by admin1 on 7/12/16.
 */

public interface UserDataService {

    @FormUrlEncoded
    @POST("know_where/api/v1/auth")
    Call<UserAuthentication> getUserAuthToken(@Field("email") String email);
}
