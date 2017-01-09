package com.example.admin1.locationsharing.db.dao.operations;

import android.content.Context;

import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.DaoSession;
import com.example.admin1.locationsharing.db.dao.UserLastKnownLocation;
import com.example.admin1.locationsharing.db.dao.UserLastKnownLocationDao;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by admin1 on 8/12/16.
 */

public class UserLastknownLocationOperations {
    public static List<UserLastKnownLocation> getUserLastKnownLocation(Context context, String token) {
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(context);
        UserLastKnownLocationDao userLastKnownLocationDao = daoSession.getUserLastKnownLocationDao();
        QueryBuilder<UserLastKnownLocation> queryBuilder = userLastKnownLocationDao.queryBuilder();
        queryBuilder.distinct().where(UserLastKnownLocationDao.Properties.Token.eq(token));
        return queryBuilder.list();
    }

    public static void insertUsersLastKnownLocation(Context context,UserLastKnownLocation userLastKnownLocation){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        UserLastKnownLocationDao userLastKnownLocationDao = daoSession.getUserLastKnownLocationDao();
        userLastKnownLocationDao.insert(userLastKnownLocation);
    }

    public static void deleteTableData(Context context){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        UserLastKnownLocationDao userLastKnownLocationDao = daoSession.getUserLastKnownLocationDao();
        userLastKnownLocationDao.deleteAll();
    }

    public static List<UserLastKnownLocation> getUserLastKnownLocation(Context context) {
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(context);
        UserLastKnownLocationDao userLastKnownLocationDao = daoSession.getUserLastKnownLocationDao();
        return userLastKnownLocationDao.loadAll();
    }

    public static List<UserLastKnownLocation> getUserWithLatLng(Context context, String latitude,String longitude) {
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(context);
        UserLastKnownLocationDao userLastKnownLocationDao = daoSession.getUserLastKnownLocationDao();
        QueryBuilder<UserLastKnownLocation> queryBuilder = userLastKnownLocationDao.queryBuilder();
        queryBuilder.distinct().where(UserLastKnownLocationDao.Properties.Latitude.eq(latitude),UserLastKnownLocationDao.Properties.Longitude.eq(longitude));
        return queryBuilder.list();
    }
}

