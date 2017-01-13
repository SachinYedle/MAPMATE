package com.example.admin1.locationsharing.db.dao.operations;


import android.content.Context;

import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.DaoSession;
import com.example.admin1.locationsharing.db.dao.Friends;
import com.example.admin1.locationsharing.db.dao.FriendsDao;
import com.example.admin1.locationsharing.db.dao.UserLastKnownLocation;
import com.example.admin1.locationsharing.db.dao.UserLastKnownLocationDao;
import com.example.admin1.locationsharing.responses.FriendsResponse;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by admin1 on 13/1/17.
 */

public class FriendsTableOperations {
    private static FriendsTableOperations instance = null;
    public static FriendsTableOperations getInstance() {
        if (instance == null) {
            instance = new FriendsTableOperations();
        }
        return instance;
    }
    public void deleteFriendsTableData(){
        List<Friends> friendsList = getFriends();
        if(friendsList.size() > 0){
            DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(MyApplication.getCurrentActivityContext());
            FriendsDao friendsDao = daoSession.getFriendsDao();
            friendsDao.deleteAll();
        }
    }
    public List<Friends> getFriends() {
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(MyApplication.getCurrentActivityContext());
        FriendsDao friendsDao = daoSession.getFriendsDao();
        return friendsDao.loadAll();
    }

    public void insertFriends(Friends friends){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(MyApplication.getCurrentActivityContext());
        FriendsDao friendsDao = daoSession.getFriendsDao();
        friendsDao.insert(friends);
    }
}
