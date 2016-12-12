package com.example.admin1.locationsharing.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.acitivities.DrawerActivity;
import com.example.admin1.locationsharing.adapters.ContactRecyclerViewAdapter;
import com.example.admin1.locationsharing.adapters.DrawerRecyclerviewAdapter;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.SharedContactTable;
import com.example.admin1.locationsharing.db.dao.operations.UserSharedContactOperation;
import com.example.admin1.locationsharing.interfaces.ItemClickListener;
import com.example.admin1.locationsharing.mappers.UserDataMapper;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;

import java.util.List;

/**
 * Created by admin1 on 6/12/16.
 */

public class DrawerFragment extends Fragment implements ItemClickListener {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.drawer_layout,container,false);
        TextView userNameText = (TextView) view.findViewById(R.id.drawer_user_name_textView);
        SharedPreferencesData preferencesData = new SharedPreferencesData(getActivity());
        userNameText.setText(preferencesData.getUserId());

        List<SharedContactTable> selectedContactList = UserSharedContactOperation.getSharedContacts(getActivity());

        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.selected_contact_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        DrawerRecyclerviewAdapter recyclerViewAdapter = new DrawerRecyclerviewAdapter(getActivity(),selectedContactList);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(View view, int position) {
        MyApplication.getInstance().showProgressDialog(getString(R.string.loading_data),getString(R.string.please_wait));
        UserDataMapper userDataMapper = new UserDataMapper(MyApplication.getCurrentActivityContext());
        userDataMapper.getUsersLast30MinLocations();
        MyApplication.getInstance().hideProgressDialog();

        ((DrawerActivity) getActivity()).closeDrawer();
    }
}
