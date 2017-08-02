package com.riktam.mapmate.locationsharing.db.operations;

import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.db.dao.DaoSession;
import com.riktam.mapmate.locationsharing.db.dao.UserLocations;
import com.riktam.mapmate.locationsharing.db.dao.UserLocationsDao;
import com.riktam.mapmate.locationsharing.responses.UserLocationData;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by admin1 on 27/12/16.
 */

public class UserLocationsOperations {

    private static UserLocationsOperations instance = null;
    public static UserLocationsOperations getInstance() {
        if (instance == null) {
            instance = new UserLocationsOperations();
        }
        return instance;
    }
    public void addUserLocations(UserLocationData userLocationData){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(MyApplication.getCurrentActivityContext());
        UserLocationsDao userLocationsDao = daoSession.getUserLocationsDao();
        UserLocations userLocations = new UserLocations();
        userLocations.setLongitude(userLocationData.getLon());
        userLocations.setLatitude(userLocationData.getLat());
        userLocations.setCreated_time(userLocationData.getCreatedTime());
        userLocations.setRadius(userLocationData.getRadius());
        userLocations.setEmail(MyApplication.getInstance().sharedPreferencesData.getSelectedUserEmail());
        userLocations.setUpdated_time(userLocationData.getUpdatedTime());
        userLocations.setSharing(userLocationData.getSharing());
        userLocationsDao.insert(userLocations);
    }
    public List<UserLocations> getUserLocations(String email){
        DaoSession daoSession = MyApplication.getInstance().getReadableDaoSession(MyApplication.getCurrentActivityContext());
        UserLocationsDao userLocationsDao = daoSession.getUserLocationsDao();
        QueryBuilder<UserLocations> queryBuilder = userLocationsDao.queryBuilder();
        queryBuilder.where(UserLocationsDao.Properties.Email.eq(email));
        return queryBuilder.list();
    }
}
