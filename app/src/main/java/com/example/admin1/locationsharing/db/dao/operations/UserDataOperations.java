package com.example.admin1.locationsharing.db.dao.operations;

import android.content.Context;

import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.DaoSession;
import com.example.admin1.locationsharing.db.dao.UserDataTable;
import com.example.admin1.locationsharing.db.dao.UserDataTableDao;
import com.example.admin1.locationsharing.pojo.UserData;

import java.util.List;

/**
 * Created by admin1 on 2/12/16.
 */


public class UserDataOperations {

    public static List<UserDataTable> getUserData(Context context){
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(context);
        UserDataTableDao userDataTableDao = daoSession.getUserDataTableDao();
        return userDataTableDao.loadAll();
    }
}
