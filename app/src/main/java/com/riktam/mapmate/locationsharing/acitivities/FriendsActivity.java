package com.riktam.mapmate.locationsharing.acitivities;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.adapters.FriendsRecyclerViewAdapter;
import com.riktam.mapmate.locationsharing.app.MyApplication;
import com.riktam.mapmate.locationsharing.db.dao.Friends;
import com.riktam.mapmate.locationsharing.db.operations.FriendsTableOperations;
import com.riktam.mapmate.locationsharing.interfaces.ItemClickListener;
import com.riktam.mapmate.locationsharing.mappers.DownloadLocationsDataMapper;
import com.riktam.mapmate.locationsharing.mappers.FriendsDataMapper;
import com.riktam.mapmate.locationsharing.mappers.LocationSharingStatusMapper;
import com.riktam.mapmate.locationsharing.pojo.FriendsData;
import com.riktam.mapmate.locationsharing.responses.FriendRequestAcceptResponse;
import com.riktam.mapmate.locationsharing.responses.FriendRequestResponse;
import com.riktam.mapmate.locationsharing.responses.FriendsServiceResponse;
import com.riktam.mapmate.locationsharing.responses.SharingStatusResponse;
import com.riktam.mapmate.locationsharing.responses.UserLocationsResponse;
import com.riktam.mapmate.locationsharing.utils.Constants;
import com.riktam.mapmate.locationsharing.utils.DrawRouteFunctionality;
import com.riktam.mapmate.locationsharing.utils.Navigator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FriendsActivity extends AppCompatActivity implements View.OnClickListener, ItemClickListener {

    private RecyclerView friendsRecyclerView;
    private TextView emailTextView;
    private TextView addFriendsTextView;
    private ArrayList<FriendsData> friendsDataList;
    private FriendsRecyclerViewAdapter friendsRecyclerViewAdapter;
    private SwipeRefreshLayout swipeContainer;
    private boolean isRefreshing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        MyApplication.getInstance().setCurrentActivityContext(FriendsActivity.this);
        initializeViews();
        setListenersToViews();
        getFriendsData();
        setUpRecyclerView();
        setUpListenerToSwipeContainer();
    }

    private void initializeViews() {
        friendsRecyclerView = (RecyclerView) findViewById(R.id.friends_recyclerView);
        emailTextView = (TextView) findViewById(R.id.friends_activity_email_editText);
        addFriendsTextView = (TextView) findViewById(R.id.friends_activity_add_btn);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.friends));

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.friends_swipeContainer);

    }

    private void setUpListenerToSwipeContainer() {
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                isRefreshing = true;
                new FriendsDataMapper().getFriends(onGetFriendsDataListener, isRefreshing);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }


    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().setCurrentActivityContext(FriendsActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.friends_activity_menu, menu);
        final MenuItem myActionMenuItem = menu.findItem(R.id.friends_menu_item_action_search);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_or_add_friend));

        /*searchView.setIconifiedByDefault(true);
        searchView.setFocusable(true);
        searchView.setIconified(false);
        searchView.requestFocusFromTouch();*/

        ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setHintTextColor(getResources().getColor(R.color.white));        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                if (filter(friendsDataList, query).size() <= 0) {
                    addFriendsTextView.setVisibility(View.VISIBLE);
                    addfriend();
                } else {
                    friendsRecyclerView.setVisibility(View.GONE);
                    swipeContainer.setVisibility(View.GONE);
                }
                myActionMenuItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String string) {
                final ArrayList<FriendsData> friendsArrayList = filter(friendsDataList, string);
                friendsRecyclerViewAdapter.setFilter(friendsArrayList, string);
                if (friendsArrayList.size() <= 0) {
                    emailTextView.setVisibility(View.VISIBLE);
                    addFriendsTextView.setVisibility(View.VISIBLE);
                    friendsRecyclerView.setVisibility(View.GONE);
                    swipeContainer.setVisibility(View.GONE);
                    emailTextView.setText(string);
                } else {
                    friendsRecyclerView.setVisibility(View.VISIBLE);
                    swipeContainer.setVisibility(View.VISIBLE);
                    emailTextView.setVisibility(View.GONE);
                    addFriendsTextView.setVisibility(View.GONE);
                }
                return true;
            }
        });
        return true;
    }

    private ArrayList<FriendsData> filter(ArrayList<FriendsData> friendsDataList, String query) {
        query = query.toLowerCase();
        ArrayList<FriendsData> filteredFriendsList = new ArrayList<>();
        for (int i = 0; i < friendsDataList.size(); i++) {
            final String email = friendsDataList.get(i).getFriendsEmail().toLowerCase();
            final String name = friendsDataList.get(i).getFriendFirstName().toLowerCase();
            if (email.contains(query) || name.contains(query)) {
                filteredFriendsList.add(friendsDataList.get(i));
            }
        }
        return filteredFriendsList;
    }

    private void getFriendsData() {
        //MyApplication.getInstance().showProgressDialog(getString(R.string.loading_data), getString(R.string.please_wait));
        List<Friends> friendsList = FriendsTableOperations.getInstance().getFriends();
        friendsDataList = new ArrayList<FriendsData>();
        setFriendsDataList(friendsList);

    }

    public void setFriendsDataList(List<Friends> friendsList) {
        emailTextView.setVisibility(View.GONE);
        if (friendsList.size() <= 0) {
            emailTextView.setVisibility(View.VISIBLE);
            emailTextView.setText(getString(R.string.click_on_search_icon));
        } else {
            for (int i = 0; i < friendsList.size(); i++) {
                FriendsData friendsData = new FriendsData();
                Friends friends = friendsList.get(i);
                friendsData.setFriendsEmail(friends.getFriend_email());
                friendsData.setFriendFirstName(friends.getFriend_first_name());
                friendsData.setSharing(friends.getSharing());
                friendsData.setFriendProfileUrl(friends.getFriend_profile_url());
                ;
                if (Integer.parseInt(friends.getStatus()) == 1 && friends.getSharing() == 1) {
                    friendsData.setStatus(getString(R.string.stop));
                } else if (Integer.parseInt(friends.getStatus()) == 1 && friends.getSharing() == 0) {
                    friendsData.setStatus(getString(R.string.start));
                } else if (Integer.parseInt(friends.getRequester_id()) == Integer.parseInt(friends.getFriend_id())) {
                    friendsData.setStatus(getString(R.string.accept_request));
                } else if (Integer.parseInt(friends.getRequester_id()) != Integer.parseInt(friends.getFriend_id())) {
                    friendsData.setStatus(getString(R.string.request_sent));
                }
                friendsDataList.add(friendsData);
            }
            sortFriendsData(friendsDataList);

        }
    }

    private void sortFriendsData(ArrayList<FriendsData> friendsList) {
        int size = friendsList.size();
        ArrayList<FriendsData> accept = new ArrayList<FriendsData>();
        ArrayList<FriendsData> requested = new ArrayList<FriendsData>();
        ArrayList<FriendsData> friends = new ArrayList<FriendsData>();
        for (int i = 0; i < size; i++) {
            if (friendsList.get(i).getStatus().equals(getString(R.string.accept_request))) {
                accept.add(friendsList.get(i));
            } else if (friendsList.get(i).getStatus().equals(getString(R.string.request_sent))) {
                requested.add(friendsList.get(i));
            } else {
                friends.add(friendsList.get(i));
            }
        }

        friendsDataList = new ArrayList<FriendsData>();

        friendsDataList.addAll(sortData(requested));
        friendsDataList.addAll(sortData(accept));
        friendsDataList.addAll(sortData(friends));
        MyApplication.getInstance().hideProgressDialog();
    }

    private ArrayList<FriendsData> sortData(ArrayList<FriendsData> friendsData){
        Collections.sort(friendsData,friendNameComparator);
        return friendsData;
    }

    private Comparator<? super FriendsData> friendNameComparator = new
            Comparator<FriendsData>() {

                @Override
                public int compare(FriendsData lhs, FriendsData rhs) {
                    return lhs.getFriendFirstName().compareTo(rhs.getFriendFirstName());
                }
            };
    private void setListenersToViews() {
        addFriendsTextView.setOnClickListener(this);
    }

    private void setUpRecyclerView() {
        LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(MyApplication.getCurrentActivityContext());
        friendsRecyclerView.addItemDecoration(new DividerItemDecoration(MyApplication.getCurrentActivityContext(), DividerItemDecoration.VERTICAL));
        friendsRecyclerView.setHasFixedSize(true);
        friendsRecyclerView.setLayoutManager(verticalLayoutManager);
        friendsRecyclerViewAdapter = new FriendsRecyclerViewAdapter(friendsDataList);
        friendsRecyclerViewAdapter.setItemClickListener(this);
        friendsRecyclerView.setAdapter(friendsRecyclerViewAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.friends_activity_add_btn:
                addfriend();
                break;
        }
    }

    private void addfriend() {
        if (validateEmail()) {
            if (MyApplication.getInstance().sharedPreferencesData.getEmail().equals(emailTextView.getText().toString())) {
                MyApplication.getInstance().showToast(getString(R.string.cant_add_yourself));
            } else if (FriendsTableOperations.getInstance().getFriendWithEmail(emailTextView.getText().toString()).size() <= 0) {
                new FriendsDataMapper().sendRequest(sendRequestListener, emailTextView.getText().toString());
            } else {
                MyApplication.getInstance().showToast(getString(R.string.user_already_added));
            }
        }
    }

    private boolean isEmailValid(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private boolean validateEmail() {
        boolean isValid = true;
        String email = emailTextView.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            MyApplication.getInstance().showToast(getString(R.string.email_required));
            isValid = false;
        } else {
            if (!isEmailValid(email)) {
                MyApplication.getInstance().showToast(getString(R.string.email_invalid));
                isValid = false;
            }
        }
        return isValid;
    }

    @Override
    public void onItemClick(View view, int position, FriendsData friendsData) {
        List<Friends> friendData = FriendsTableOperations.getInstance().getFriendWithEmail(friendsData.getFriendsEmail());
        if (friendsData.getStatus().equals(getString(R.string.accept_request))) {
            int friendRequestId = friendData.get(0).getFriend_request_id();
            int friendId = Integer.parseInt(friendData.get(0).getFriend_id());
            new FriendsDataMapper().acceptFriendRequest(onRequestAcceptedListener, friendRequestId, friendId);
        } else if (friendsData.getStatus().equals(getString(R.string.request_sent))) {
            MyApplication.getInstance().showToast(getString(R.string.friend_didnt_accepted_request));
        } else {
            if (view.getId() == R.id.friends_recycler_item_share_location_switch && friendsData.getStatus().equals(getResources().getString(R.string.stop))) {
                //MyApplication.getInstance().showToast(getString(R.string.sharing_stopped));
                new LocationSharingStatusMapper(this).updateLocationSharingStatus(onStatusUpdatedListener, Constants.STOP_SHARING, Integer.parseInt(friendData.get(0).getFriend_id()));
            }
            if (view.getId() == R.id.friends_recycler_item_share_location_switch && friendsData.getStatus().equals(getResources().getString(R.string.start))) {
                //MyApplication.getInstance().showToast(getString(R.string.sharing_started));
                new LocationSharingStatusMapper(this).updateLocationSharingStatus(onStatusUpdatedListener, Constants.START_SHARING, Integer.parseInt(friendData.get(0).getFriend_id()));
            } else if (view.getId() == R.id.friends_recycler_item_email_textView || view.getId() == R.id.friends_recycler_item_name_textView || view.getId() == R.id.friends_recycler_item_profile_imageView) {
                drawRoute(friendsData.getFriendsEmail());
            }
        }
    }

    private void drawRoute(String email) {
        DrawRouteFunctionality.getInstance().setRouteVisible(true);
        DownloadLocationsDataMapper dataMapper = new DownloadLocationsDataMapper(MyApplication.getCurrentActivityContext());
        dataMapper.getLocations(onGetLocationListener, FriendsTableOperations.getInstance().getFriendId(email));
    }

    private DownloadLocationsDataMapper.OnTaskCompletedListener onGetLocationListener = new DownloadLocationsDataMapper.OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted(UserLocationsResponse userLocationsResponse) {
            if (userLocationsResponse.getSuccess().equalsIgnoreCase("true")) {
                Navigator.getInstance().navigateToMapActivity(MyApplication.getInstance().sharedPreferencesData.getSelectedUserEmail());
                finish();
            } else {
                MyApplication.getInstance().showToast(userLocationsResponse.getMessage());
            }
        }

        @Override
        public void onTaskFailed(String response) {
            MyApplication.getInstance().hideProgressDialog();
            MyApplication.getInstance().showToast(response);
        }
    };

    LocationSharingStatusMapper.OnStatusUpdatedListener onStatusUpdatedListener = new LocationSharingStatusMapper.OnStatusUpdatedListener() {
        @Override
        public void onTaskCompleted(SharingStatusResponse sharingStatusResponse) {
            if (sharingStatusResponse.isSuccess()) {
                new FriendsDataMapper().getFriends(onGetFriendsDataListener, true);
                //MyApplication.getInstance().showToast(getString(R.string.status_updated));
            } else {
                MyApplication.getInstance().showToast(getString(R.string.status_update_failed));
            }
        }

        @Override
        public void onTaskFailed(String response) {
            MyApplication.getInstance().showToast(response);
        }
    };

    FriendsDataMapper.OnRequestAcceptedListener onRequestAcceptedListener = new FriendsDataMapper.OnRequestAcceptedListener() {
        @Override
        public void onTaskCompleted(FriendRequestAcceptResponse friendRequestAcceptResponse) {
            if (friendRequestAcceptResponse.isSuccess()) {
                MyApplication.getInstance().showToast(getString(R.string.accepted));
                isRefreshing = true;
                new FriendsDataMapper().getFriends(onGetFriendsDataListener, isRefreshing);
            }
        }

        @Override
        public void onTaskFailed(String response) {
            MyApplication.getInstance().showToast(response);
        }
    };

    FriendsDataMapper.OnTaskCompletedListener onGetFriendsDataListener = new FriendsDataMapper.OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted(FriendsServiceResponse friendsServiceResponse) {
            getFriendsData();
            friendsRecyclerViewAdapter.clear();
            friendsRecyclerViewAdapter.addAll(friendsDataList);
            if (isRefreshing) {
                swipeContainer.setRefreshing(false);
                isRefreshing = false;
            }
        }

        @Override
        public void onTaskFailed(String response) {
            MyApplication.getInstance().showToast(response);
            if (isRefreshing) {
                swipeContainer.setRefreshing(false);
                isRefreshing = false;
            }
        }
    };

    FriendsDataMapper.OnRequestSentListener sendRequestListener = new FriendsDataMapper.OnRequestSentListener() {
        @Override
        public void onTaskCompleted(FriendRequestResponse friendRequestResponse) {
            if (friendRequestResponse.isSuccess()) {
                MyApplication.getInstance().showToast(getString(R.string.request_sent));
                isRefreshing = true;
                new FriendsDataMapper().getFriends(onGetFriendsDataListener, isRefreshing);
            } else if (!friendRequestResponse.isSuccess()) {
                MyApplication.getInstance().showToast("" + friendRequestResponse.getData());
            }
        }

        @Override
        public void onTaskFailed(String response) {
            MyApplication.getInstance().showToast(response);
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Navigator.getInstance().navigateToMapActivity();
        finish();
    }

}
