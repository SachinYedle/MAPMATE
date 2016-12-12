package com.example.admin1.locationsharing.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.adapters.ContactRecyclerViewAdapter;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.Contacts;
import com.example.admin1.locationsharing.db.dao.operations.ContactsOperations;
import com.example.admin1.locationsharing.pojo.Contact;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.Navigator;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by admin1 on 6/12/16.
 */

public class ContactsFragment extends Fragment {

    private Cursor contacts;
    private ArrayList<Contacts> contactsArrayList;
    private Handler updateBarHandler;
    int counter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        initializeVariables();
    }

    public void initializeVariables() {
        SharedPreferencesData preferencesData = new SharedPreferencesData(getActivity());

        CustomLog.d("Contacts","initialize"+preferencesData.getIsFirstTime());

        if(!preferencesData.getIsFirstTime()){
            preferencesData.setIsFirstTime(true);
            updateBarHandler =new Handler();

            fetchContactsAndStoreToDB();

        }else{
            getContactsFromDB();
        }
    }

    public void getContactsFromDB(){
        List<Contacts> contactsList = ContactsOperations.getAllContactsFromDB(getActivity());
        contactsArrayList = new ArrayList<Contacts>(contactsList);

        Collections.sort(contactsArrayList, new Comparator<Contacts>(){
            @Override
            public int compare(Contacts contacts1, Contacts contacts2){

                boolean isAdded1 = contacts1.getIs_contact_added();
                boolean isAdded2 = contacts2.getIs_contact_added();
                if (isAdded1 != isAdded2){
                    if (isAdded1){
                        return -1;
                    }
                    return 1;
                }
                return 0;
            }
        });
    }

    public void fetchContactsAndStoreToDB(){
        if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
            getContactsAndStoreToDB();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_fragment_layout,container,false);
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.contact_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        ContactRecyclerViewAdapter recyclerViewAdapter = new ContactRecyclerViewAdapter(getActivity(),contactsArrayList);
        recyclerView.setAdapter(recyclerViewAdapter);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.contact_fragment_menu,menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.add);
        item.setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.done){
            Navigator.navigateToMapActivity();
        }
        return super.onOptionsItemSelected(item);

    }

    public void getContactsAndStoreToDB(){

        final ProgressDialog pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Reading contacts...");
        pDialog.setCancelable(false);
        pDialog.show();
        counter = 0;
        contacts = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (contacts != null) {
            if (contacts.getCount() == 0) {
                Toast.makeText(getActivity(), "No contacts in your contact list.", Toast.LENGTH_LONG).show();
            }

            while (contacts.moveToNext()) {


                pDialog.setMessage("Reading contacts : "+ counter++ +"/"+contacts.getCount());;

                String firstName = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phoneNumber = phoneNumber.replaceAll("[()\\-\\s]", "");
                String lastName = "";

                Contact contact = new Contact();
                contact.setFirstName(firstName);
                contact.setLastName(lastName);
                contact.setAdded(false);
                contact.setRequested(false);
                contact.setShared(false);
                contact.setPhone(phoneNumber);

                CustomLog.i("name","Fname:"+firstName+" Lname:"+lastName);
                ContactsOperations.insertContactsToDb(getActivity(),contact);
            }
        } else {
            CustomLog.e("Cursor close 1", "----------------");
        }
        contacts.close();
        MyApplication.getInstance().hideProgressDialog();
    }
}
