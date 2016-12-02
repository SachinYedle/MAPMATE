package com.example.admin1.locationsharing.services;

import com.example.admin1.locationsharing.pojo.UserInfo;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by admin1 on 2/12/16.
 */

public interface UserDataService {
    @GET("api/json/get/E1Ef_zqzz")
    Call<UserInfo> getUserData();
}
