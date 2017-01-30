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

    private static UserLastknownLocationOperations instance = null;
    public static UserLastknownLocationOperations getInstance() {
        if (instance == null) {
            instance = new UserLastknownLocationOperations();
        }
        return instance;
    }

    public List<UserLastKnownLocation> getUserLastKnownLocationWithEmail(String email) {
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(MyApplication.getCurrentActivityContext());
        UserLastKnownLocationDao userLastKnownLocationDao = daoSession.getUserLastKnownLocationDao();
        QueryBuilder<UserLastKnownLocation> queryBuilder = userLastKnownLocationDao.queryBuilder();
        queryBuilder.distinct().where(UserLastKnownLocationDao.Properties.Email.eq(email));
        return queryBuilder.list();
    }

    public void insertUsersLastKnownLocation(UserLastKnownLocation userLastKnownLocation){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(MyApplication.getCurrentActivityContext());
        UserLastKnownLocationDao userLastKnownLocationDao = daoSession.getUserLastKnownLocationDao();
        userLastKnownLocationDao.insert(userLastKnownLocation);
    }

    public void deleteUserLastKnownData(){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(MyApplication.getCurrentActivityContext());
        UserLastKnownLocationDao userLastKnownLocationDao = daoSession.getUserLastKnownLocationDao();
        userLastKnownLocationDao.deleteAll();
    }

    public List<UserLastKnownLocation> getUserLastKnownLocation() {
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(MyApplication.getCurrentActivityContext());
        UserLastKnownLocationDao userLastKnownLocationDao = daoSession.getUserLastKnownLocationDao();
        return userLastKnownLocationDao.loadAll();
    }

    public List<UserLastKnownLocation> getUserWithLatLng(String latitude,String longitude) {
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(MyApplication.getCurrentActivityContext());
        UserLastKnownLocationDao userLastKnownLocationDao = daoSession.getUserLastKnownLocationDao();
        QueryBuilder<UserLastKnownLocation> queryBuilder = userLastKnownLocationDao.queryBuilder();
        queryBuilder.distinct().where(UserLastKnownLocationDao.Properties.Latitude.eq(latitude),UserLastKnownLocationDao.Properties.Longitude.eq(longitude));
        return queryBuilder.list();
    }
}

