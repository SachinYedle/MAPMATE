package com.example.admin1.locationsharing.db.dao.operations;

import android.content.Context;

import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.DaoSession;
import com.example.admin1.locationsharing.db.dao.UserLastKnownLocation;
import com.example.admin1.locationsharing.db.dao.UserLastKnownLocationDao;
import com.example.admin1.locationsharing.db.dao.UserLocations;
import com.example.admin1.locationsharing.db.dao.UserLocationsDao;
import com.example.admin1.locationsharing.responses.UsersLast30MinLocations;
import com.example.admin1.locationsharing.responses.UsersLastLocations;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by admin1 on 8/12/16.
 */

public class UsersLast30MinLocationsOperation {

    public static List<UserLocations> getUsersLast30MinLocations(Context context, String phone) {
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(context);
        UserLocationsDao userLocationsDao = daoSession.getUserLocationsDao();
        QueryBuilder<UserLocations> queryBuilder = userLocationsDao.queryBuilder();
        queryBuilder.distinct().where(UserLocationsDao.Properties.Phone.eq(phone));
        return queryBuilder.list();
    }

    public static List<UserLocations> getUsersLast30MinLocations(Context context) {
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(context);
        UserLocationsDao userLocationsDao = daoSession.getUserLocationsDao();
        return userLocationsDao.loadAll();
    }

    public static void deleteUsersLast30MinLocations(Context context,Long key){
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(context);
        UserLocationsDao userLocationsDao = daoSession.getUserLocationsDao();
        userLocationsDao.deleteByKey(key);
    }
}
