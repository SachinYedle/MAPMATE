package com.example.admin1.locationsharing.db.dao.operations;

import android.content.Context;

import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.DaoSession;
import com.example.admin1.locationsharing.db.dao.SharedContactTable;
import com.example.admin1.locationsharing.db.dao.SharedContactTableDao;
import com.example.admin1.locationsharing.pojo.Contact;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by admin1 on 6/12/16.
 */

public class UserSharedContactOperation {
    public static List<SharedContactTable> getUserData(Context context) {
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(context);
        SharedContactTableDao sharedContactTableDao = daoSession.getSharedContactTableDao();
        return sharedContactTableDao.loadAll();
    }
    public static List<SharedContactTable> getUserWithPhneAndName(Context context, Contact contact){
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(context);
        SharedContactTableDao sharedContactTableDao = daoSession.getSharedContactTableDao();

        QueryBuilder<SharedContactTable> queryBuilder = sharedContactTableDao.queryBuilder();
        queryBuilder.distinct().where(SharedContactTableDao.Properties.Name.eq(contact.getName()),SharedContactTableDao.Properties.Phone.eq(contact.getPhone()));
        return queryBuilder.list();
    }
}
