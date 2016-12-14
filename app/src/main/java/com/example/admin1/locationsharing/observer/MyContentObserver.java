package com.example.admin1.locationsharing.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;

import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.operations.ContactsOperations;
import com.example.admin1.locationsharing.utils.CustomLog;

/**
 * Created by admin1 on 14/12/16.
 */

public class MyContentObserver extends ContentObserver {

    private Context context;

    public MyContentObserver(Handler handler) {
        super(handler);
        context = MyApplication.getCurrentActivityContext();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Cursor contacts = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        int phoneContactCount = contacts.getCount();
        contacts.close();
        int dbContactCount = ContactsOperations.getContactsCount(context);
        if(phoneContactCount < dbContactCount){
            CustomLog.i("Observer","Contact Deletede");
        } else if(phoneContactCount == dbContactCount){
            CustomLog.i("Observer","Contact Modified");
        } else {
            CustomLog.i("Observer","Contact Added");
        }
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        CustomLog.i("Observer","Contact change");
    }
}
