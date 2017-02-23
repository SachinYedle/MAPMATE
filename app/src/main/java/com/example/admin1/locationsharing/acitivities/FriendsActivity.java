package com.example.admin1.locationsharing.acitivities;

import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.adapters.FriendsRecyclerViewAdapter;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.Friends;
import com.example.admin1.locationsharing.db.operations.FriendsTableOperations;
import com.example.admin1.locationsharing.interfaces.ItemClickListener;
import com.example.admin1.locationsharing.mappers.FriendsDataMapper;
import com.example.admin1.locationsharing.pojo.FriendsData;
import com.example.admin1.locationsharing.responses.FriendRequestAcceptResponse;
import com.example.admin1.locationsharing.responses.FriendRequestResponse;
import com.example.admin1.locationsharing.responses.FriendsServiceResponse;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.Navigator;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity implements View.OnClickListener, ItemClickListener {

    private RecyclerView friendsRecyclerView;
    private TextView emailTextView;
    private TextView addFriendsTextView;
    private ArrayList<FriendsData> friendsDataList;
    private FriendsRecyclerViewAdapter friendsRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        MyApplication.getInstance().setCurrentActivityContext(FriendsActivity.this);
        initializeViews();
        setListenersToViews();
        getFriendsData();
        setUpRecyclerView();
    }

    private void initializeViews() {
        friendsRecyclerView = (RecyclerView) findViewById(R.id.friends_activity_recyclerView);
        emailTextView = (TextView) findViewById(R.id.friends_activity_email_editText);
        addFriendsTextView = (TextView) findViewById(R.id.friends_activity_add_btn);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Friends");
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
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                CustomLog.d("SearchView", "submit");
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                if(filter(friendsDataList, query).size()<=0){
                    addfriend();
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
                    emailTextView.setText(string);
                } else {
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
            final String text = friendsDataList.get(i).getFriendsEmail().toLowerCase();
            if (text.contains(query)) {
                filteredFriendsList.add(friendsDataList.get(i));
            }
        }
        return filteredFriendsList;
    }

    private void getFriendsData() {
        MyApplication.getInstance().showProgressDialog("Loading data...", "please wait");
        List<Friends> friendsList = FriendsTableOperations.getInstance().getFriends();
        friendsDataList = new ArrayList<FriendsData>();
        setFriendsDataList(friendsList);
        MyApplication.getInstance().hideProgressDialog();
    }

    public void setFriendsDataList(List<Friends> friendsList) {
        emailTextView.setVisibility(View.GONE);
        if (friendsList.size() <= 0) {
            emailTextView.setVisibility(View.VISIBLE);
            emailTextView.setText("Click on search icon to add friend");
        } else {
            for (int i = 0; i < friendsList.size(); i++) {
                FriendsData friendsData = new FriendsData();
                Friends friends = friendsList.get(i);
                friendsData.setFriendsEmail(friends.getFriend_email());
                friendsData.setFriendFirstName(friends.getFriend_first_name());
                if (Integer.parseInt(friends.getStatus()) == 1) {
                    friendsData.setStatus("Remove");
                } else if (Integer.parseInt(friends.getRequester_id()) == Integer.parseInt(friends.getFriend_id())) {
                    friendsData.setStatus("Sccept request");
                } else if (Integer.parseInt(friends.getRequester_id()) != Integer.parseInt(friends.getFriend_id())) {
                    friendsData.setStatus("request sent");
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
            if (friendsList.get(i).getStatus().equals("accept request")) {
                accept.add(friendsList.get(i));
            } else if (friendsList.get(i).getStatus().equals("request sent")) {
                requested.add(friendsList.get(i));
            } else {
                friends.add(friendsList.get(i));
            }
        }
        friendsDataList = new ArrayList<FriendsData>();
        friendsDataList.addAll(requested);
        friendsDataList.addAll(accept);
        friendsDataList.addAll(friends);
    }

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

    private void addfriend(){
        if (validateEmail()) {
            if (MyApplication.getInstance().sharedPreferencesData.getEmail().equals(emailTextView.getText().toString())) {
                MyApplication.getInstance().showToast("Can't add yourself");
            } else if (FriendsTableOperations.getInstance().getFriendWithEmail(emailTextView.getText().toString()).size() <= 0) {
                new FriendsDataMapper().sendRequest(sendRequestListener, emailTextView.getText().toString());
            } else {
                MyApplication.getInstance().showToast("User Already added");
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
            MyApplication.getInstance().showToast("Email Required");
            isValid = false;
        } else {
            if (!isEmailValid(email)) {
                MyApplication.getInstance().showToast("Email invalid");
                isValid = false;
            }
        }
        return isValid;
    }

    @Override
    public void onItemClick(View view, int position,FriendsData friendsData) {
        if (friendsData.getStatus().equals("accept request")) {
            List<Friends> friendData = FriendsTableOperations.getInstance().getFriendWithEmail(friendsData.getFriendsEmail());
            int friendRequestId = friendData.get(0).getFriend_request_id();
            int friendId = Integer.parseInt(friendData.get(0).getFriend_id());
            new FriendsDataMapper().acceptFriendRequest(onRequestAcceptedListener, friendRequestId, friendId);
        } else if (friendsData.getStatus().equals("request sent")) {
            MyApplication.getInstance().showToast("Friend didn't accepted your request");
        } else {
            if (view.getId() == R.id.friends_recyclerView_status_textView && friendsData.getStatus().equals("remove")) {
                MyApplication.getInstance().showToast("Remove friend");
            } else {
                Intent intent = new Intent(FriendsActivity.this, MapActivity.class);
                intent.putExtra("email", friendsData.getFriendsEmail());
                startActivity(intent);
                finish();
            }
        }
    }

    FriendsDataMapper.OnRequestAcceptedListener onRequestAcceptedListener = new FriendsDataMapper.OnRequestAcceptedListener() {
        @Override
        public void onTaskCompleted(FriendRequestAcceptResponse friendRequestAcceptResponse) {
            if (friendRequestAcceptResponse.isSuccess()) {
                MyApplication.getInstance().showToast("Accepted");
                new FriendsDataMapper().getFriends(onGetFriendsDataListener);
            }
        }

        @Override
        public void onTaskFailed(String response) {
            MyApplication.getInstance().showToast("Error: " + response);
        }
    };

    FriendsDataMapper.OnTaskCompletedListener onGetFriendsDataListener = new FriendsDataMapper.OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted(FriendsServiceResponse friendsServiceResponse) {
            finish();
            Navigator.getInstance().navigateToFriendsActivity();
        }

        @Override
        public void onTaskFailed(String response) {
            MyApplication.getInstance().showToast("Getting Friends data : " + response);
        }
    };

    FriendsDataMapper.OnRequestSentListener sendRequestListener = new FriendsDataMapper.OnRequestSentListener() {
        @Override
        public void onTaskCompleted(FriendRequestResponse friendRequestResponse) {
            if (friendRequestResponse.isSuccess()) {
                MyApplication.getInstance().showToast("Request Sent");
                new FriendsDataMapper().getFriends(onGetFriendsDataListener);
            } else if (!friendRequestResponse.isSuccess()) {
                MyApplication.getInstance().showToast("" + friendRequestResponse.getData());
            }
        }

        @Override
        public void onTaskFailed(String response) {
            MyApplication.getInstance().showToast("Error: " + response);
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Navigator.getInstance().navigateToMapActivity();
        finish();
    }
}
