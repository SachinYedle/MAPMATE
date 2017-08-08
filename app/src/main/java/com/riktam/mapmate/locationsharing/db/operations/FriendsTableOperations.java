package com.riktam.mapmate.locationsharing.db.operations;


import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.db.dao.DaoSession;
import com.riktam.mapmate.locationsharing.db.dao.Friends;
import com.riktam.mapmate.locationsharing.db.dao.FriendsDao;
import com.riktam.mapmate.locationsharing.pojo.FriendsData;

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

    public List<Friends> getFriendWithEmail(String email) {
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(MyApplication.getCurrentActivityContext());
        FriendsDao friendsDao = daoSession.getFriendsDao();
        QueryBuilder<Friends> queryBuilder = friendsDao.queryBuilder();
        queryBuilder.distinct().where(FriendsDao.Properties.Friend_email.eq(email));
        return queryBuilder.list();
    }

    public String getFriendId(String email) {
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(MyApplication.getCurrentActivityContext());
        FriendsDao friendsDao = daoSession.getFriendsDao();
        QueryBuilder<Friends> queryBuilder = friendsDao.queryBuilder();
        queryBuilder.distinct().where(FriendsDao.Properties.Friend_email.eq(email));
        return queryBuilder.list().get(0).getFriend_id();
    }

    public void insertFriends(Friends friends){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(MyApplication.getCurrentActivityContext());
        FriendsDao friendsDao = daoSession.getFriendsDao();
        friendsDao.insert(friends);
    }

    public void updateFriend(FriendsData friends){
        List<Friends> friends1 =  getFriendWithEmail(friends.getFriendsEmail());
        Friends friend = friends1.get(0);
        if(friend.getStatus().equalsIgnoreCase(MyApplication.getCurrentActivityContext().getString(R.string.stop))){
            friend.setStatus(MyApplication.getCurrentActivityContext().getString(R.string.start));
        }else {
            friend.setStatus(MyApplication.getCurrentActivityContext().getString(R.string.stop));
        }
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(MyApplication.getCurrentActivityContext());
        FriendsDao friendsDao = daoSession.getFriendsDao();
        friendsDao.update(friend);
    }
}
