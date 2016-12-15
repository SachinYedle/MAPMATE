package com.example.admin1.locationsharing.db.dao.operations;

import android.support.v4.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.widget.Toast;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.pojo.Contact;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.Navigator;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;

/**
 * Created by admin1 on 14/12/16.
 */

public class ContactsPhoneToDBSyncOperations {
    private Cursor contacts;
    private ContentResolver resolver;
    private Context context;
    private android.support.v4.app.FragmentManager fragmentManager;
    private int flagToCheckCRUD;//firstTime = 0, add = 1, mod = 2, del = 3;

    public ContactsPhoneToDBSyncOperations(Context context, FragmentManager fragmentManager){
        this.context = context;
        this.fragmentManager = fragmentManager;
        resolver = context.getContentResolver();
        contacts = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

    }

    public void fetchContactsAndStoreToDB(){
        int dbContactCount = ContactsOperations.getContactsCount(context);
        if(dbContactCount == 0){
            flagToCheckCRUD = 0;
        }else if(contacts.getCount() < dbContactCount){
            flagToCheckCRUD = 3;
        } else if(contacts.getCount() == dbContactCount){
            flagToCheckCRUD = 2;
        } else {
            flagToCheckCRUD = 1;
        }
        new LoadContacts().execute(flagToCheckCRUD);
    }

    private class LoadContacts extends AsyncTask<Integer,Integer,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MyApplication.getInstance().showProgressDialog(context.getString(R.string.loading_contacts),context.getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            int flagToCheckCRUD = integers[0];
            if (contacts != null) {
                int count = contacts.getCount(),counter = 1;
                SharedPreferencesData preferencesData = new SharedPreferencesData(context);
                int contactIdToStoreInPrefs = preferencesData.getContactId();
                if (count == 0) {
                    Toast.makeText(context, "No contacts in your contact list.", Toast.LENGTH_LONG).show();
                }

                while (contacts.moveToNext()) {
                    String firstName = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    int contactId = Integer.parseInt(contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)));
                    String phoneNumber = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phoneNumber = phoneNumber.replaceAll("[()\\-\\s]", "");
                    String lastName = "";
                    String imageURI = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
                    Contact contact = new Contact();
                    contact.setFirstName(firstName);
                    contact.setLastName(lastName);
                    contact.setPhoto(imageURI);
                    contact.setAdded(false);
                    contact.setRequested(false);
                    contact.setShared(false);
                    contact.setPhone(phoneNumber);
                    contact.setContact_id(contactId);
                    publishProgress(counter,count);



                    CustomLog.i("nameas","Contact id: "+contactId +" Fname:"+firstName+" Lname:"+lastName);
                    if(flagToCheckCRUD == 0){
                        ContactsOperations.insertContactsToDb(context,contact);
                    } else if(flagToCheckCRUD == 1 || contactId > contactIdToStoreInPrefs ){
                        if(contactId > contactIdToStoreInPrefs){
                            contactIdToStoreInPrefs = contactId;
                            preferencesData.setContactId(contactIdToStoreInPrefs);
                        }
                        ContactsOperations.insertAddedContactToDb(context,contact);
                        CustomLog.i("Do In background","Added");
                    } else if(flagToCheckCRUD == 2){
                        ContactsOperations.modifyContactToDb(context,contact);
                        CustomLog.i("Do In background","Modified");
                    } else if(flagToCheckCRUD == 3){
                        ContactsOperations.deleteContactFromDb(context,contact);
                        CustomLog.i("Do In background","Deleted");
                    }
                    if(contactId > contactIdToStoreInPrefs){
                        contactIdToStoreInPrefs = contactId;
                        preferencesData.setContactId(contactIdToStoreInPrefs);
                    }
                    counter++;
                }
                if(flagToCheckCRUD == 3){
                    ContactsOperations.deleteUnmodifiedContactFromDB(context);
                }
            } else {
                CustomLog.e("Cursor close 1", "----------------");
            }
            contacts.close();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            MyApplication.getInstance().updateTextInProgressDialog("Contacts: "+values[0] + "/"+ values[1]);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            MyApplication.getInstance().hideProgressDialog();
            Navigator.navigateToContactsFragment(fragmentManager);
        }
    }
}
