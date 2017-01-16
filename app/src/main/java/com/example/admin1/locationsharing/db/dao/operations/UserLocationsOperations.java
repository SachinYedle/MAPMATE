package com.example.admin1.locationsharing.db.dao.operations;

import android.content.Context;

import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.DaoSession;
import com.example.admin1.locationsharing.db.dao.UserLocations;
import com.example.admin1.locationsharing.db.dao.UserLocationsDao;
import com.example.admin1.locationsharing.responses.UserLocationData;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by admin1 on 27/12/16.
 */

public class UserLocationsOperations {

    public static void addUserLocations(Context context, UserLocationData userLocationData){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        UserLocationsDao userLocationsDao = daoSession.getUserLocationsDao();
        SharedPreferencesData preferencesData = new SharedPreferencesData(context);
        UserLocations userLocations = new UserLocations();
        userLocations.setLongitude(userLocationData.getLon());
        userLocations.setLatitude(userLocationData.getLat());
        userLocations.setTime(userLocationData.getTime());
        userLocations.setRadius(userLocationData.getRadius());
        userLocations.setToken(preferencesData.getSelectedUserEmail());
        userLocationsDao.insert(userLocations);
    }
    public static void  deleteUserLocations(Context context, Long key){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        UserLocationsDao userLocationsDao = daoSession.getUserLocationsDao();
        userLocationsDao.deleteByKey(key);
    }
    public static List<UserLocations> getUserLocations(Context context, String token){
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(context);
        UserLocationsDao userLocationsDao = daoSession.getUserLocationsDao();
        QueryBuilder<UserLocations> queryBuilder = userLocationsDao.queryBuilder();
        queryBuilder.where(UserLocationsDao.Properties.Token.eq(token));
        return queryBuilder.list();
    }
}
