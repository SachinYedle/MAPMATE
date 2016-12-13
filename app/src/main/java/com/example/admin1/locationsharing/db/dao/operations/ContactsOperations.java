package com.example.admin1.locationsharing.db.dao.operations;

import android.content.Context;

import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.Contacts;
import com.example.admin1.locationsharing.db.dao.ContactsDao;
import com.example.admin1.locationsharing.db.dao.DaoSession;
import com.example.admin1.locationsharing.db.dao.UserLocations;
import com.example.admin1.locationsharing.db.dao.UserLocationsDao;
import com.example.admin1.locationsharing.pojo.Contact;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by admin1 on 12/12/16.
 */

public class ContactsOperations {

    public static void insertContactsToDb(Context context, Contact contact){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        ContactsDao contactsDao = daoSession.getContactsDao();
        Contacts contacts = new Contacts();
        contacts.setPhone(contact.getPhone());
        contacts.setFirst_name(contact.getFirstName());
        contacts.setLast_name(contact.getLastName());
        contacts.setIs_contact_added(contact.isAdded());
        contacts.setIs_location_shared(contact.isShared());
        contacts.setIs_location_requested(contact.isRequested());
        contacts.setPhoto(contact.getPhoto());
        contactsDao.insert(contacts);
    }
    public static List<Contacts> getAddedContactsFromDB(Context context){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        ContactsDao contactsDao = daoSession.getContactsDao();

        QueryBuilder<Contacts> queryBuilder = contactsDao.queryBuilder();
        queryBuilder.distinct().where(ContactsDao.Properties.Is_contact_added.eq(true));
        return queryBuilder.list();
    }
    public static List<Contacts> getContactsExceptAddedFromDB(Context context){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        ContactsDao contactsDao = daoSession.getContactsDao();

        QueryBuilder<Contacts> queryBuilder = contactsDao.queryBuilder();
        queryBuilder.distinct().where(ContactsDao.Properties.Is_contact_added.eq(false));
        return queryBuilder.list();
    }

    public static List<Contacts> getAllContactsFromDB(Context context){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        ContactsDao contactsDao = daoSession.getContactsDao();
        return contactsDao.loadAll();

    }
}
