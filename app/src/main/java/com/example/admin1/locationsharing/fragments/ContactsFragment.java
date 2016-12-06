package com.example.admin1.locationsharing.fragments;

import android.Manifest;
import android.support.v4.app.Fragment;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
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
import com.example.admin1.locationsharing.db.dao.DaoSession;
import com.example.admin1.locationsharing.db.dao.SharedContactTable;
import com.example.admin1.locationsharing.db.dao.SharedContactTableDao;
import com.example.admin1.locationsharing.db.dao.operations.UserSharedContactOperation;
import com.example.admin1.locationsharing.pojo.Contact;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.Navigator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin1 on 6/12/16.
 */

public class ContactsFragment extends Fragment {

    private static final int REQUEST_PERMISSION_CODE = 123;
    private Cursor contacts;
    private ArrayList<Contact> contactArrayList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        initializeVariables();
    }

    public void initializeVariables() {
        contactArrayList = new ArrayList<Contact>();
        checkContactsPermission();
        getContacts();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_fragment_layout,container,false);
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.contact_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        ContactRecyclerViewAdapter recyclerViewAdapter = new ContactRecyclerViewAdapter(getActivity(),contactArrayList);
        recyclerView.setAdapter(recyclerViewAdapter);
        return view;
    }

    public boolean checkContactsPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_CONTACTS)) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_PERMISSION_CODE);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_PERMISSION_CODE);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.READ_CONTACTS)
                            == PackageManager.PERMISSION_GRANTED) {
                        getContacts();
                    }

                } else {
                    Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_LONG).show();
                }
                break;
        }
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
            addSelectedContactsToDB();
            Navigator.navigateToMapActivity();
        }
        return super.onOptionsItemSelected(item);

    }

    private void addSelectedContactsToDB(){
        DaoSession daoSession = MyApplication.getInstance().getWritableDaoSession(getActivity());
        SharedContactTableDao sharedContactTableDao = daoSession.getSharedContactTableDao();
        Contact contact;
        for (int i = 0; i < contactArrayList.size(); i++){
            contact = contactArrayList.get(i);
            if(contact.isSelected()){
                SharedContactTable sharedContactTable = new SharedContactTable();
                sharedContactTable.setName(contact.getName());
                sharedContactTable.setPhone(contact.getPhone());
                sharedContactTableDao.insert(sharedContactTable);
            }

        }
    }

    public void getContacts(){
        contacts = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (contacts != null) {
            CustomLog.d("contacts count", "" + contacts.getCount());
            if (contacts.getCount() == 0) {
                Toast.makeText(getActivity(), "No contacts in your contact list.", Toast.LENGTH_LONG).show();
            }
            while (contacts.moveToNext()) {
                String name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Contact contact = new Contact();
                contact.setName(name);
                contact.setPhone(phoneNumber);
                contact.setSelected(false);
                List<SharedContactTable> sharedContact = UserSharedContactOperation.getUserWithPhneAndName(getActivity(),contact);
                if(sharedContact.size() <= 0){
                    contactArrayList.add(contact);
                }
            }
            contacts.close();
        } else {
            CustomLog.e("Cursor close 1", "----------------");
            contacts.close();
        }
    }
}
