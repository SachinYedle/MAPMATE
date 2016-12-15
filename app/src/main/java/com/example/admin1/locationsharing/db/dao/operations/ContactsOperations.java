package com.example.admin1.locationsharing.db.dao.operations;

import android.support.v4.app.FragmentManager;
import android.content.Context;

import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.Contacts;
import com.example.admin1.locationsharing.db.dao.ContactsDao;
import com.example.admin1.locationsharing.db.dao.DaoSession;
import com.example.admin1.locationsharing.db.dao.UserLocations;
import com.example.admin1.locationsharing.db.dao.UserLocationsDao;
import com.example.admin1.locationsharing.pojo.Contact;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.Navigator;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;

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
        contact.setContact_id(contact.getContact_id());
        contacts.setPhoto(contact.getPhoto());
        contacts.setIs_modified(false);
        contactsDao.insert(contacts);
    }

    public static void insertAddedContactToDb(Context context, Contact contact){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        ContactsDao contactsDao = daoSession.getContactsDao();
        Contacts contacts = new Contacts();
        SharedPreferencesData preferencesData = new SharedPreferencesData(context);
        int contactIdToStoreInPrefs = preferencesData.getContactId();
        if(contactIdToStoreInPrefs == contact.getContact_id()){
            contacts.setPhone(contact.getPhone());
            contacts.setFirst_name(contact.getFirstName());
            contacts.setLast_name(contact.getLastName());
            contacts.setIs_contact_added(contact.isAdded());
            contacts.setIs_location_shared(contact.isShared());
            contacts.setIs_location_requested(contact.isRequested());
            contact.setContact_id(contact.getContact_id());
            contacts.setPhoto(contact.getPhoto());
            contacts.setIs_modified(false);
            contactsDao.insert(contacts);
        }
    }

    public static void modifyContactToDb(Context context, Contact contact){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        ContactsDao contactsDao = daoSession.getContactsDao();

        QueryBuilder<Contacts> queryBuilder = contactsDao.queryBuilder();
        List<Contacts> contactsList = queryBuilder.where(ContactsDao.Properties.Phone.eq(contact.getPhone())).list();
        if (contactsList != null) {
            for (Contacts contactsObj : contactsList) {
                contactsObj.setFirst_name(contact.getFirstName()+" "+contact.getLastName());
                contactsObj.setLast_name("");
                contactsObj.setPhone(contact.getPhone());
                contactsDao.update(contactsObj);
            }
        }
    }

    public static void deleteContactFromDb(Context context, Contact contact){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        ContactsDao contactsDao = daoSession.getContactsDao();

        QueryBuilder<Contacts> queryBuilder = contactsDao.queryBuilder();
        List<Contacts> contactsList = queryBuilder.where(ContactsDao.Properties.Phone.eq(contact.getPhone())).list();
        if (contactsList != null) {
            for (Contacts contactsObj : contactsList) {
                contactsObj.setIs_modified(true);
                contactsDao.update(contactsObj);
            }
        }
    }
    public static void deleteUnmodifiedContactFromDB(Context context){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        ContactsDao contactsDao = daoSession.getContactsDao();

        QueryBuilder<Contacts> queryBuilder = contactsDao.queryBuilder();
        List<Contacts> contactsList = queryBuilder.where(ContactsDao.Properties.Is_modified.eq(false)).list();

        if(contactsList.size() > 0){
            contactsDao.delete(contactsList.get(0));
            CustomLog.i("Delete","Contact deleted"+contactsList.get(0).getFirst_name());
        }
        updateModifiedStatus(context);

    }

    public static void updateModifiedStatus(Context context){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        ContactsDao contactsDao = daoSession.getContactsDao();

        List<Contacts> contactsList = contactsDao.loadAll();
        if (contactsList != null) {
            for (Contacts contactsObj : contactsList) {
                contactsObj.setIs_modified(false);
                contactsDao.update(contactsObj);
            }
        }
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
        return contactsDao.queryBuilder().orderAsc(ContactsDao.Properties.First_name).list();
    }
    public static int getContactsCount(Context context){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(context);
        ContactsDao contactsDao = daoSession.getContactsDao();
        return contactsDao.loadAll().size();
    }
}
