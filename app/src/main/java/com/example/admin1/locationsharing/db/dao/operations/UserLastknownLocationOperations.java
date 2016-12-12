package com.example.admin1.locationsharing.db.dao.operations;

import android.content.Context;

import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.DaoSession;
import com.example.admin1.locationsharing.db.dao.SharedContactTable;
import com.example.admin1.locationsharing.db.dao.SharedContactTableDao;
import com.example.admin1.locationsharing.db.dao.UserLastKnownLocation;
import com.example.admin1.locationsharing.db.dao.UserLastKnownLocationDao;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by admin1 on 8/12/16.
 */

public class UserLastknownLocationOperations {
    public static List<UserLastKnownLocation> getUserLastKnownLocation(Context context, String phone) {
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(context);
        UserLastKnownLocationDao userLastKnownLocationDao = daoSession.getUserLastKnownLocationDao();
        QueryBuilder<UserLastKnownLocation> queryBuilder = userLastKnownLocationDao.queryBuilder();
        queryBuilder.distinct().where(UserLastKnownLocationDao.Properties.Phone.eq(phone));
        return queryBuilder.list();
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

