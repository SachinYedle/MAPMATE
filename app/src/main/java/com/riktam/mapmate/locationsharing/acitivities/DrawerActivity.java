package com.riktam.mapmate.locationsharing.acitivities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.app.MyApplication;

public class DrawerActivity extends AppCompatActivity {

    public DrawerLayout mDrawerLayout;
    private CharSequence mTitle;
    private Bundle mSavedInstanceState;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().setCurrentActivityContext(DrawerActivity.this);

    }

    /**
     * Sets navigation drawer
     */
    protected void setDrawerLayout(Context context) {

        mTitle = getTitle();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(drawerListener);

        mDrawerToggle = new ActionBarDrawerToggle((Activity) context,
                mDrawerLayout, R.string.app_name,
                R.string.app_name);

        if (mSavedInstanceState == null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }

    }

    DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(View drawerView) {

        }

        @Override
        public void onDrawerClosed(View drawerView) {
            //calling onPrepareOptionsMenu() to show action bar icons
            invalidateOptionsMenu();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null)
            // Pass the event to ActionBarDrawerToggle, if it returns
            // true, then it has handled the app icon touch event
            if (mDrawerToggle.onOptionsItemSelected(item)) {
                return true;
            }
        return super.onOptionsItemSelected(item);
    }
}