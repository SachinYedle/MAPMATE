package com.riktam.mapmate.locationsharing.utils;

import com.riktam.mapmate.locationsharing.BuildConfig;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author revathim initiates retrofit adapter and http cache
 */
public class SetupRetrofit {

    /**
     * Initiates retrofit adapter and http cache
     */

    public static final String API_BASE_URL = BuildConfig.HOST;

    public static <S> S createService(Class<S> serviceClass) {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor).writeTimeout(10, TimeUnit.MINUTES).connectTimeout(10, TimeUnit.MINUTES).readTimeout(10, TimeUnit.MINUTES).addNetworkInterceptor(new RequestNetworkInterceptor())
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson)).client(okHttpClient).build();
        MyApplication.getInstance().retrofit = retrofit;
        return retrofit.create(serviceClass);

    }

    /**
     * checks the requests, fetches parameters and includes authorization header
     *
     * @author revathim
     */
    public static class RequestNetworkInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response originalResponse;
            String requestUrl = request.url().encodedPath();
            if (MyApplication.getInstance().isConnectedToInterNet() && !(requestUrl.contains("know_where/api/v1/auth"))) {

                request = chain.request();
                request = request.newBuilder()
                        .addHeader("auth_token", MyApplication.getInstance().sharedPreferencesData.getUserToken())
                        .build();
            } else {
                request = request.newBuilder()
                        .addHeader("Accept", "application/json")
                        .build();
            }
            originalResponse = chain.proceed(request);
            return originalResponse.newBuilder()
                    .build();
        }
    }
}