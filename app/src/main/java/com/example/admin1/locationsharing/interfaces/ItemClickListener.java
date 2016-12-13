package com.example.admin1.locationsharing.interfaces;

import android.view.View;

import com.example.admin1.locationsharing.db.dao.Contacts;

import java.util.ArrayList;

/**
 * Created by admin1 on 6/12/16.
 */

public interface ItemClickListener {
    public void onItemClick(View view, int position, ArrayList<Contacts> contacts);
    public void onItemClick(View view, int position);
}
