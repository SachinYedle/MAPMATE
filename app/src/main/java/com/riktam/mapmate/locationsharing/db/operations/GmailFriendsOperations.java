package com.riktam.mapmate.locationsharing.db.operations;

import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.db.dao.DaoSession;
import com.riktam.mapmate.locationsharing.db.dao.FriendsDao;
import com.riktam.mapmate.locationsharing.db.dao.UserGmailFriends;
import com.riktam.mapmate.locationsharing.db.dao.UserGmailFriendsDao;

import java.util.List;

/**
 * Created by admin1 on 14/6/17.
 */

public class GmailFriendsOperations {

    private static GmailFriendsOperations instance = null;
    public static GmailFriendsOperations getInstance() {
        if (instance == null) {
            instance = new GmailFriendsOperations();
        }
        return instance;
    }
    public void deleteFriendsTableData(){
        List<UserGmailFriends> friendsList = getGmailFriends();
        if(friendsList.size() > 0){
            DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(MyApplication.getCurrentActivityContext());
            UserGmailFriendsDao friendsDao = daoSession.getUserGmailFriendsDao();
            friendsDao.deleteAll();
        }
    }
    public List<UserGmailFriends> getGmailFriends() {
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(MyApplication.getCurrentActivityContext());
        UserGmailFriendsDao friendsDao = daoSession.getUserGmailFriendsDao();
        return friendsDao.loadAll();
    }


    public void insertFriends(UserGmailFriends friends){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(MyApplication.getCurrentActivityContext());
        UserGmailFriendsDao friendsDao = daoSession.getUserGmailFriendsDao();
        friendsDao.insert(friends);
    }
}
