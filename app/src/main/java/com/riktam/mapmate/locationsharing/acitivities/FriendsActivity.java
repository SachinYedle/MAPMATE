package com.riktam.mapmate.locationsharing.acitivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.riktam.mapmate.locationsharing.db.dao.UserGmailFriends;
import com.riktam.mapmate.locationsharing.db.operations.FriendsTableOperations;
import com.riktam.mapmate.locationsharing.db.operations.GmailFriendsOperations;
import com.riktam.mapmate.locationsharing.fragments.DrawerFragment;
import com.riktam.mapmate.locationsharing.interfaces.ItemClickListener;
import com.riktam.mapmate.locationsharing.interfaces.PositiveClick;
import com.riktam.mapmate.locationsharing.mappers.CheckIfUserRegisteredMapper;
import com.riktam.mapmate.locationsharing.mappers.DownloadLocationsDataMapper;
import com.riktam.mapmate.locationsharing.mappers.FriendsDataMapper;
import com.riktam.mapmate.locationsharing.mappers.InviteFriendMapper;
import com.riktam.mapmate.locationsharing.mappers.LocationSharingStatusMapper;
import com.riktam.mapmate.locationsharing.pojo.FriendsData;
import com.riktam.mapmate.locationsharing.responses.CheckIfregisteredResponse;
import com.riktam.mapmate.locationsharing.responses.FriendRequestAcceptResponse;
import com.riktam.mapmate.locationsharing.responses.FriendRequestResponse;
import com.riktam.mapmate.locationsharing.responses.FriendsServiceResponse;
import com.riktam.mapmate.locationsharing.responses.MailInviteResponse;
import com.riktam.mapmate.locationsharing.responses.SharingStatusResponse;
import com.riktam.mapmate.locationsharing.responses.UserLocationsResponse;
import com.riktam.mapmate.locationsharing.utils.Constants;
import com.riktam.mapmate.locationsharing.utils.DrawRouteFunctionality;
import com.riktam.mapmate.locationsharing.utils.Navigator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FriendsActivity extends DrawerActivity implements View.OnClickListener, ItemClickListener {

    private RecyclerView friendsRecyclerView;
    private TextView emailTextView, switchInfoTextView;
    public TextView googleFriendsHeaderTextView;
    private TextView addFriendsTextView;
    private ArrayList<FriendsData> friendsDataList;
    private FriendsRecyclerViewAdapter friendsRecyclerViewAdapter;
    private ArrayList<FriendsData> gmailFriends;
    private SwipeRefreshLayout swipeContainer;
    private boolean isRefreshing = false;
    private FriendsData friend;
    private ArrayList<FriendsData> friendsRecyclerItems;
    public int googleFriendsStartingPos;
    private SearchView searchView;
    private int clickedItemPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        MyApplication.getInstance().setCurrentActivityContext(FriendsActivity.this);
        initializeViews();
        setUpToolbar();
        setListenersToViews();
        getGmailFriends();
        getFriendsData();
        setUpListenerToSwipeContainer();
        //setUpGmailFriendsRecyclerView();
    }

    private void initializeViews() {
        friendsRecyclerView = (RecyclerView) findViewById(R.id.friends_recyclerView);
        //gmailFriendsRecyclerView = (RecyclerView) findViewById(R.id.friends_gmail_friends_recyclerView);

        emailTextView = (TextView) findViewById(R.id.friends_activity_email_editText);
        addFriendsTextView = (TextView) findViewById(R.id.friends_activity_add_btn);
        googleFriendsHeaderTextView = (TextView) findViewById(R.id.friends_recycler_header_item_google_friends);
        switchInfoTextView = (TextView) findViewById(R.id.friends_recycler_header_item_switch_info_textView);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.friends_swipeContainer);

        DrawerFragment fragment = new DrawerFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.drawer_fragment_layout, fragment);
        fragmentTransaction.addToBackStack("DrawerLayout");
        fragmentTransaction.commit();

        //hideSwithInfoTextView();
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnClickListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setDrawerLayout(MyApplication.getCurrentActivityContext());
        getSupportActionBar().setTitle(getString(R.string.friends));
    }

    public void hideSwitchInfoTextView() {
//        Timer t = new Timer(false);
//        t.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    public void run() {
        switchInfoTextView.setVisibility(View.GONE);
//                    }
//                });
//            }
//        }, 5000);
    }

    public void showSwitchInfoTextView(){
        switchInfoTextView.setVisibility(View.VISIBLE);
    }

    private void getGmailFriends() {
        gmailFriends = new ArrayList<>();
        List<UserGmailFriends> gmailFriendsList = GmailFriendsOperations.getInstance().getGmailFriends();
        for (int i = 0; i < gmailFriendsList.size(); i++) {
            FriendsData friendsData = new FriendsData();
            friendsData.setFriendFirstName(gmailFriendsList.get(i).getName());
            friendsData.setFriendsEmail(gmailFriendsList.get(i).getEmail());
            gmailFriends.add(friendsData);
        }
    }

    /* private ArrayList<FriendsData> sortGmailFriendsData(ArrayList<FriendsData> friendsData) {
         Collections.sort(friendsData, );
         return friendsData;
     }

     private Comparator<? super FriendsData> gmailFriendNameComparator = new
             Comparator<FriendsData>() {

                 @Override
                 public int compare(FriendsData lhs, FriendsData rhs) {
                     return lhs.getFriendFirstName().compareTo(rhs.getFriendFirstName());
                 }
             };
 */
    private void setUpListenerToSwipeContainer() {
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                isRefreshing = true;
                ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setText("");
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
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_or_add_friend));
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            searchView.setIconifiedByDefault(false);
            searchView.setFocusable(true);
            searchView.setIconified(false);
            searchView.requestFocusFromTouch();
        }
        ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text))
                .setHintTextColor(getResources().getColor(R.color.grey));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                if (filter(friendsRecyclerItems, query).size() <= 0) {
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
                ArrayList<FriendsData> friendsArrayList = filter(friendsDataList, string);
                googleFriendsStartingPos = friendsArrayList.size();
                friendsArrayList = filter(friendsRecyclerItems, string);
                /*final ArrayList<FriendsData> gmailFriendsList = filter(gmailFriends, string);
                friendsArrayList.addAll(gmailFriendsList);*/
                friendsRecyclerViewAdapter.setFilter(friendsArrayList, string);
                if (friendsArrayList.size() <= 1) {
                    emailTextView.setVisibility(View.VISIBLE);
                    addFriendsTextView.setVisibility(View.VISIBLE);
                    friendsRecyclerView.setVisibility(View.GONE);
                    googleFriendsHeaderTextView.setVisibility(View.GONE);
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
            if (friendsDataList.get(i).getFriendsEmail() == null) {
                filteredFriendsList.add(friendsDataList.get(i));
            } else {
                String name = "", email = "";
                if(friendsDataList.get(i).getFriendsEmail()!= null){
                    email = friendsDataList.get(i).getFriendsEmail().toLowerCase();
                }

                if(friendsDataList.get(i).getFriendFirstName()!=null){
                    name = friendsDataList.get(i).getFriendFirstName().toLowerCase();
                }

                if (!email.equals("") && email.contains(query) || !name.equals("") &&name.contains(query)) {
                    filteredFriendsList.add(friendsDataList.get(i));
                }
            }
        }
        return filteredFriendsList;
    }

    private ArrayList<UserGmailFriends> filterGmailFriendsData(ArrayList<UserGmailFriends> friendsDataList, String query) {
        query = query.toLowerCase();
        ArrayList<UserGmailFriends> filteredFriendsList = new ArrayList<>();
        for (int i = 0; i < friendsDataList.size(); i++) {
            final String email = friendsDataList.get(i).getEmail().toLowerCase();
            final String name = friendsDataList.get(i).getName().toLowerCase();
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
        if (friendsList.size() <= 0 && gmailFriends.size() <= 0) {
            emailTextView.setVisibility(View.VISIBLE);
            addFriendsTextView.setVisibility(View.VISIBLE);
            friendsRecyclerView.setVisibility(View.GONE);
            googleFriendsHeaderTextView.setVisibility(View.GONE);
            swipeContainer.setVisibility(View.GONE);
            emailTextView.setText(getString(R.string.click_on_search_icon));
        } else {
            for (int i = 0; i < friendsList.size(); i++) {
                FriendsData friendsData = new FriendsData();
                Friends friends = friendsList.get(i);
                friendsData.setFriendsEmail(friends.getFriend_email());
                friendsData.setFriendFirstName(friends.getFriend_first_name());
                friendsData.setSharing(friends.getSharing());
                friendsData.setFriendProfileUrl(friends.getFriend_profile_url());
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
        friendsDataList.addAll(sortData(accept));
        friendsDataList.addAll(sortData(friends));
        friendsDataList.addAll(sortData(requested));
        makeActualList(friendsDataList, sortData(gmailFriends));
        MyApplication.getInstance().hideProgressDialog();
    }


    private void makeActualList(ArrayList<FriendsData> friendsData, ArrayList<FriendsData> userGmailFriends) {
        friendsRecyclerItems = new ArrayList<FriendsData>();
        friendsRecyclerItems.addAll(friendsData);
        if (userGmailFriends != null) {
            FriendsData friendData = new FriendsData();
            friendData.setFriendFirstName(getString(R.string.other_friends));
            googleFriendsStartingPos = friendsRecyclerItems.size();
            friendsRecyclerItems.add(friendData);
            for (int i = 0; i < userGmailFriends.size(); i++) {
                if (!containsEmail(friendsData, userGmailFriends.get(i).getFriendsEmail())) {
                    friendsRecyclerItems.add(userGmailFriends.get(i));
                }
            }
        }
        setUpRecyclerView();
    }

    private boolean containsEmail(ArrayList<FriendsData> list, String email) {
        for (FriendsData object : list) {
            if (object.getFriendsEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<FriendsData> sortData(ArrayList<FriendsData> friendsData) {
        Collections.sort(friendsData, friendNameComparator);
        return friendsData;
    }

    private Comparator<? super FriendsData> friendNameComparator = new
            Comparator<FriendsData>() {

                @Override
                public int compare(FriendsData lhs, FriendsData rhs) {
                    if(lhs.getFriendFirstName() != null && rhs.getFriendFirstName()!= null)
                        return lhs.getFriendFirstName().compareTo(rhs.getFriendFirstName());
                    else return -1;
                }
            };

    private void setListenersToViews() {
        addFriendsTextView.setOnClickListener(this);
    }

    private void setUpRecyclerView() {
        LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(MyApplication.getCurrentActivityContext());
//        dividerItemDecoration = new DividerItemDecoration(MyApplication.getCurrentActivityContext(), DividerItemDecoration.VERTICAL);
        //friendsRecyclerView.addItemDecoration(dividerItemDecoration);
        friendsRecyclerView.setHasFixedSize(true);
        friendsRecyclerView.setLayoutManager(verticalLayoutManager);

        if(friendsDataList.size() <= 0){
            switchInfoTextView.setVisibility(View.GONE);
        }
        friendsRecyclerViewAdapter = new FriendsRecyclerViewAdapter(friendsRecyclerItems);
        friendsRecyclerViewAdapter.setItemClickListener(this);
        friendsRecyclerView.setAdapter(friendsRecyclerViewAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.friends_activity_add_btn:
                addfriend();
                break;
            case R.id.toolbar:
                Navigator.getInstance().navigateToFriendsActivity("addFriend");
                break;
        }
    }

    private void addfriend() {
        if (validateEmail()) {
            if (MyApplication.getInstance().sharedPreferencesData.getEmail().equals(emailTextView.getText().toString())) {
                MyApplication.getInstance().showToast(getString(R.string.cant_add_yourself));
            } else if (FriendsTableOperations.getInstance().getFriendWithEmail(emailTextView.getText().toString()).size() <= 0) {
                PositiveClick positiveClick = new PositiveClick() {
                    @Override
                    public void onClick() {
                        new FriendsDataMapper().sendRequest(sendRequestListener, emailTextView.getText().toString());
                    }
                };
                MyApplication.getInstance().showAlertWithPositiveNegativeButton(getString(R.string
                        .send_request), "After sending request you will be able to see your friends real time location and vice-versa", getString(R.string.cancel), getString(R.string
                        .send), positiveClick);


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
        hideSoftKeyboard();
        clickedItemPosition = position;
        this.friend = friendsData;
        if (friendsData.getFriendProfileUrl() == null && friendsData.getStatus() == null) {
            String email = friend.getFriendsEmail();
            new CheckIfUserRegisteredMapper().checkIfRegistered(onCheckRegisterListener, email);
        } else {
            List<Friends> friendData = FriendsTableOperations.getInstance().getFriendWithEmail(friendsData.getFriendsEmail());
            if (friendsData.getStatus().equals(getString(R.string.accept_request))) {
                int friendRequestId = friendData.get(0).getFriend_request_id();
                int friendId = Integer.parseInt(friendData.get(0).getFriend_id());
                new FriendsDataMapper().acceptFriendRequest(onRequestAcceptedListener, friendRequestId, friendId);
            } else if (friendsData.getStatus().equals(getString(R.string.request_sent))) {
                MyApplication.getInstance().showToast(getString(R.string.friend_didnt_accepted_request));
            } else {
                if (view.getId() == R.id.friends_recycler_item_share_location_switch && friendsData.getStatus().equals(getResources().getString(R.string.start))) {
                    MyApplication.getInstance().showToast(getString(R.string.sharing_stopped) +" "+ friendsData.getFriendFirstName());
                    new LocationSharingStatusMapper(this).updateLocationSharingStatus(onStatusUpdatedListener, Constants.STOP_SHARING, Integer.parseInt(friendData.get(0).getFriend_id()));
                }
                if (view.getId() == R.id.friends_recycler_item_share_location_switch && friendsData.getStatus().equals(getResources().getString(R.string.stop))) {
                    MyApplication.getInstance().showToast(getString(R.string.sharing_started) +" "+ friendsData.getFriendFirstName());
                    new LocationSharingStatusMapper(this).updateLocationSharingStatus(onStatusUpdatedListener, Constants.START_SHARING, Integer.parseInt(friendData.get(0).getFriend_id()));
                } else if (view.getId() == R.id.friends_recycler_item_email_textView || view.getId() == R.id.friends_recycler_item_name_textView || view.getId() == R.id.friends_recycler_item_profile_imageView) {
                    drawRoute(friendsData.getFriendsEmail());
                }
            }
        }

    }


    CheckIfUserRegisteredMapper.OnTaskCompletedListener onCheckRegisterListener = new CheckIfUserRegisteredMapper.OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted(final CheckIfregisteredResponse checkIfregisteredResponse) {
            PositiveClick positiveClick = new PositiveClick() {
                @Override
                public void onClick() {
                    new FriendsDataMapper().sendRequest(sendRequestListener, checkIfregisteredResponse.getData().getFriendEmail());
                }
            };
            MyApplication.getInstance().showAlertWithPositiveNegativeButton(getString(R.string
                    .send_request), "Send request to " + friend.getFriendFirstName() + " to see his/her real time location and to share your location with him", getString(R.string.cancel), getString(R.string
                    .send), positiveClick);
        }

        @Override
        public void onTaskFailed(String response) {
            PositiveClick positiveClick = new PositiveClick() {
                @Override
                public void onClick() {
                    new InviteFriendMapper().inviteFriend(inviteListener, friend.getFriendsEmail());
                }
            };

            MyApplication.getInstance().showAlertWithPositiveNegativeButton(getString(R.string
                    .send_invite), "Your Google friend " + friend.getFriendFirstName() + " is not using the App.", getString(R.string.cancel), getString(R.string
                    .invite), positiveClick);
        }
    };

    InviteFriendMapper.OnInviteListener inviteListener = new InviteFriendMapper.OnInviteListener() {
        @Override
        public void onTaskCompleted(MailInviteResponse checkIfregisteredResponse) {
            //MyApplication.getInstance().showToast(getString(R.string.invitation_sent));
            PositiveClick positiveClick = new PositiveClick() {
                @Override
                public void onClick() {
                    Intent inviteIntent = new Intent();
                    inviteIntent.setAction(Intent.ACTION_SEND);
                    String textMessage = "Hi " + friend.getFriendFirstName() + ", \n" + MyApplication.getInstance().sharedPreferencesData.getEmail()
                            + " invited you to " + getString(R.string.app_name) +" "+ getString(R.string.app_id);
                    inviteIntent.putExtra(Intent.EXTRA_TEXT, textMessage);
                    inviteIntent.setType("text/plain");
                    startActivity(Intent.createChooser(inviteIntent, "Invite to " + getString(R.string.app_name)));
                }
            };

            MyApplication.getInstance().showAlertWithPositiveNegativeButton(getString(R.string
                            .send_invite), getString(R.string.invitation_sent_by_mail),
                    getString(R.string.no), getString(R.string.yes), positiveClick);
        }

        @Override
        public void onTaskFailed(String response) {
            //MyApplication.getInstance().showToast(getString(R.string.invitation_failed));
            PositiveClick positiveClick = new PositiveClick() {
                @Override
                public void onClick() {
                    Intent inviteIntent = new Intent();
                    inviteIntent.setAction(Intent.ACTION_SEND);
                    String textMessage = "Hi " + friend.getFriendFirstName() + ", \n" + MyApplication.getInstance().sharedPreferencesData.getEmail()
                            + " invited you to "+getString(R.string.app_name) + " " +getString(R.string.app_id);
                    inviteIntent.putExtra(Intent.EXTRA_TEXT, textMessage);
                    inviteIntent.setType("text/plain");
                    startActivity(Intent.createChooser(inviteIntent, "Invite to " + getString(R.string.app_name)));
                }
            };

            MyApplication.getInstance().showAlertWithPositiveNegativeButton(getString(R.string
                    .send_invite), getString(R.string.invitation_sent_by_mail_failed), getString(R.string.cancel), getString(R.string
                    .invite), positiveClick);
        }
    };

    private void drawRoute(String email) {
        MyApplication.getInstance().sharedPreferencesData.setSelectedUserEmail(email);
        DownloadLocationsDataMapper dataMapper = new DownloadLocationsDataMapper(MyApplication.getCurrentActivityContext());
        dataMapper.getLocations(onGetLocationListener, FriendsTableOperations.getInstance().getFriendId(email));
    }

    private DownloadLocationsDataMapper.OnTaskCompletedListener onGetLocationListener = new DownloadLocationsDataMapper.OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted(UserLocationsResponse userLocationsResponse) {
            if (userLocationsResponse.getSuccess().equalsIgnoreCase("true")) {
                Navigator.getInstance().navigateToFriendsRouteActivity(MyApplication.getInstance().sharedPreferencesData.getSelectedUserEmail(), "friends");
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
                FriendsTableOperations.getInstance().updateFriend(friend);
                //new FriendsDataMapper().getFriends(onGetFriendsDataListener, true);
                //MyApplication.getInstance().showToast(getString(R.string.status_updated));
                friendsRecyclerViewAdapter.notifyItemChanged(clickedItemPosition);
            } else {
                MyApplication.getInstance().showToast(getString(R.string.status_update_failed));
                new FriendsDataMapper().getFriends(onGetFriendsDataListener, true);
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
            if (gmailFriends == null) {
                getGmailFriends();
            }
            getFriendsData();
//            if (friendsRecyclerViewAdapter != null) {
//                friendsRecyclerViewAdapter.clear();
//            }
//            if (friendsRecyclerItems != null) {
//                friendsRecyclerItems.clear();
//            }
            makeActualList(friendsDataList, gmailFriends);
            friendsRecyclerViewAdapter.addAll(friendsRecyclerItems);
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
                ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setText("");
                new FriendsDataMapper().getFriends(onGetFriendsDataListener, isRefreshing);

            } else if (!friendRequestResponse.isSuccess()) {
                if(friendRequestResponse.getData().equalsIgnoreCase("Requested email hasn't registered yet")){
                    PositiveClick positiveClick = new PositiveClick() {
                        @Override
                        public void onClick() {
                            if(friend!=null){
                                friend.setFriendFirstName(emailTextView.getText().toString());
                            }else {
                                friend = new FriendsData();
                                friend.setFriendFirstName(emailTextView.getText().toString());
                            }
                            ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setText("");
                            new InviteFriendMapper().inviteFriend(inviteListener, friend.getFriendFirstName());
                        }
                    };

                    MyApplication.getInstance().showAlertWithPositiveNegativeButton(getString(R.string.send_invite), "Your friend " + emailTextView.getText().toString() + " is not using the App.", getString(R.string.cancel), getString(R.string.invite), positiveClick);
                }
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
        if (isDrawerOpen()) {
            closeDrawer();
        } else {
//            super.onBackPressed();
            Navigator.getInstance().navigateToMapActivity();
            finish();
        }
    }
}
