package com.example.admin1.locationsharing.utils;

import android.util.Log;
import com.example.admin1.locationsharing.BuildConfig;
/**
 * Custom Log class enables us to disable logging in the production build
 */
public class CustomLog {

    public static void v(String tag, String meesage) {
        if (!BuildConfig.IS_PRODUCTION)
            Log.v(tag, meesage);

    }

    public static void i(String tag, String meesage) {
        if (!BuildConfig.IS_PRODUCTION)
            Log.i(tag, meesage);

    }

    public static void e(String tag, String meesage) {
        if (!BuildConfig.IS_PRODUCTION)
            Log.e(tag, meesage);

    }

    public static void d(String tag, String meesage) {
        if (!BuildConfig.IS_PRODUCTION)
            Log.d(tag, meesage);

    }

    public static void d(String tag, String meesage, Exception ex) {
        if (!BuildConfig.IS_PRODUCTION)
            Log.d(tag, meesage, ex);

    }

    public static void e(String tag, String meesage, Exception ex) {
        if (!BuildConfig.IS_PRODUCTION)
            Log.e(tag, meesage, ex);

    }

}