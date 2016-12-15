package com.example.admin1.locationsharing.fragments;

import android.Manifest;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.adapters.ContactRecyclerViewAdapter;
import com.example.admin1.locationsharing.adapters.SelectdContactsRVAdapter;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.Contacts;
import com.example.admin1.locationsharing.db.dao.operations.ContactsOperations;
import com.example.admin1.locationsharing.db.dao.operations.ContactsPhoneToDBSyncOperations;
import com.example.admin1.locationsharing.interfaces.ItemClickListener;
import com.example.admin1.locationsharing.observer.MyContentObserver;
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

public class ContactsFragment extends Fragment implements ItemClickListener {


    private ArrayList<Contacts> contactsArrayList;
    private ArrayList<Contacts> selectedContactsList;
    private RecyclerView selectedContactsRecyclerView;
    private ContactRecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView recyclerView;
    private MyContentObserver myObserver;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        initializeVariables();
    }

    public void initializeVariables() {
        selectedContactsList = new ArrayList<Contacts>();
        SharedPreferencesData preferencesData = new SharedPreferencesData(getActivity());
        if(!preferencesData.getIsFirstTime()){
            preferencesData.setIsFirstTime(true);
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
            ContactsPhoneToDBSyncOperations operations = new ContactsPhoneToDBSyncOperations(getActivity(),getFragmentManager());
            operations.fetchContactsAndStoreToDB();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_fragment_layout,container,false);
        selectedContactsRecyclerView = (RecyclerView)view.findViewById(R.id.selected_contact_recyclerview);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        selectedContactsRecyclerView.setLayoutManager(horizontalLayoutManager);

        recyclerView = (RecyclerView)view.findViewById(R.id.contact_recyclerview);
        LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(verticalLayoutManager);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerViewAdapter = new ContactRecyclerViewAdapter(getActivity(),contactsArrayList);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setItemClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.contact_fragment_menu,menu);
        final MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                CustomLog.d("SearchView","submit");
                if( ! searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                myActionMenuItem.collapseActionView();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String string) {
                final ArrayList<Contacts> filteredContactList = filter(contactsArrayList, string);
                if(filteredContactList.size() > 0){
                    recyclerViewAdapter.setFilter(filteredContactList, string);
                    return true;
                }else {
                    recyclerViewAdapter.setFilter(filteredContactList,string);
                    CustomLog.i("SearchQuery","Result not found");
                    return false;
                }


            }
        });
    }

    private ArrayList<Contacts> filter(ArrayList<Contacts> contacts, String query) {
        query = query.toLowerCase();
        ArrayList<Contacts> filteredContactList = new ArrayList<>();
        for (int i = 0; i<contacts.size(); i++){
            final String text = contacts.get(i).getFirst_name().toLowerCase();
            if (text.contains(query)) {
                filteredContactList.add(contacts.get(i));
            }
        }
        return filteredContactList;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.add);
        item.setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.done:
                Navigator.navigateToMapActivity();
                break;
            case R.id.refresh:
                fetchContactsAndStoreToDB();
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(myObserver == null){
            myObserver = new MyContentObserver(new Handler());
        }
        getActivity().getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true,myObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getContentResolver().unregisterContentObserver(myObserver);
    }

    @Override
    public void onItemClick(View view, int position, ArrayList<Contacts> contacts) {
        if(!contacts.get(position).getIs_location_shared()){
            selectedContactsList.add(contacts.get(position));
            contacts.get(position).setIs_location_shared(true);
        }
        else{
            Contacts contact = contacts.get(position);
            int i = selectedContactsList.indexOf(contact);
            selectedContactsList.remove(i);
            contacts.get(position).setIs_location_shared(false);
        }
        if(selectedContactsList.size() > 0){
            selectedContactsRecyclerView.setVisibility(View.VISIBLE);
        }else {
            selectedContactsRecyclerView.setVisibility(View.GONE);
        }
        SelectdContactsRVAdapter adapter = new SelectdContactsRVAdapter(getActivity(),selectedContactsList);
        selectedContactsRecyclerView.smoothScrollToPosition(selectedContactsList.size());
        selectedContactsRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View view, int position) {

    }
}
