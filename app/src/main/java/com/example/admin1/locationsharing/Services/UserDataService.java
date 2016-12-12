package com.example.admin1.locationsharing.Services;

import com.example.admin1.locationsharing.responses.UserInfo;
import com.example.admin1.locationsharing.responses.UsersLastLocations;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by admin1 on 7/12/16.
 */

public interface UserDataService {
    @GET("api/json/get/E1Ef_zqzz")
    Call<UserInfo> getUserData();

    @GET("api/json/get/E1YwGQMmM")
    Call<UsersLastLocations> getUsersLastLocation();
}
