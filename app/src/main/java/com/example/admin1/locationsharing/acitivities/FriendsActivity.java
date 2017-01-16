package com.example.admin1.locationsharing.acitivities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.adapters.FriendsRecyclerViewAdapter;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.Friends;
import com.example.admin1.locationsharing.db.dao.operations.FriendsTableOperations;
import com.example.admin1.locationsharing.interfaces.ItemClickListener;
import com.example.admin1.locationsharing.mappers.FriendsDataMapper;
import com.example.admin1.locationsharing.pojo.FriendsData;
import com.example.admin1.locationsharing.responses.FriendRequestAcceptResponse;
import com.example.admin1.locationsharing.responses.FriendRequestResponse;
import com.example.admin1.locationsharing.responses.FriendsResponse;
import com.example.admin1.locationsharing.responses.FriendsServiceResponse;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.Navigator;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity implements View.OnClickListener, ItemClickListener{

    private RecyclerView friendsRecyclerView;
    private EditText emailEditText;
    private Button addFriendsButton;
    private ArrayList<FriendsData> friendsDataList;
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
    private void initializeViews(){
        friendsRecyclerView = (RecyclerView)findViewById(R.id.friends_activity_recyclerView);
        emailEditText = (EditText)findViewById(R.id.friends_activity_email_editText);
        addFriendsButton = (Button)findViewById(R.id.friends_activity_add_btn);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Friends");
    }

    private void getFriendsData(){
        MyApplication.getInstance().showProgressDialog("Loading data...","please wait");
        List<Friends> friendsList = FriendsTableOperations.getInstance().getFriends();
        friendsDataList = new ArrayList<FriendsData>();
        setFriendsDataList(friendsList);
        MyApplication.getInstance().hideProgressDialog();
    }

    public void setFriendsDataList(List<Friends> friendsList) {
        for (int i = 0; i < friendsList.size(); i++){
            FriendsData friendsData = new FriendsData();
            Friends friends = friendsList.get(i);
            friendsData.setFriendsEmail(friends.getFriend_email());
            if(Integer.parseInt(friends.getStatus()) == 1){
                friendsData.setStatus(" ");
            }else if(Integer.parseInt(friends.getRequester_id()) == Integer.parseInt(friends.getFriend_id())){
                friendsData.setStatus("accept");
            }else if(Integer.parseInt(friends.getRequester_id()) != Integer.parseInt(friends.getFriend_id())){
                friendsData.setStatus("requested");
            }
            friendsDataList.add(friendsData);
        }
    }

    private void setListenersToViews(){
        addFriendsButton.setOnClickListener(this);
    }

    private void setUpRecyclerView(){
        LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(MyApplication.getCurrentActivityContext());
        friendsRecyclerView.addItemDecoration(new DividerItemDecoration(MyApplication.getCurrentActivityContext(), DividerItemDecoration.VERTICAL));
        friendsRecyclerView.setHasFixedSize(true);
        friendsRecyclerView.setLayoutManager(verticalLayoutManager);
        FriendsRecyclerViewAdapter adapter = new FriendsRecyclerViewAdapter(friendsDataList);
        adapter.setItemClickListener(this);
        friendsRecyclerView.setAdapter(adapter);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.friends_activity_add_btn:
                if(validateEmail()){
                   new FriendsDataMapper().sendRequest(sendRequestListener,emailEditText.getText().toString());
                }
                break;
        }
    }
    private boolean isEmailValid(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private boolean validateEmail() {
        boolean isValid = true;
        String email = emailEditText.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email Required");
            isValid = false;
        } else {
            if (!isEmailValid(email)) {
                emailEditText.setText("");
                emailEditText.setError("Email invalid");
                isValid = false;
            }
        }
        return isValid;
    }
    @Override
    public void onItemClick(View view, int position) {
        FriendsData friendsData = friendsDataList.get(position);
        if(friendsData.getStatus().equals("accept")){
            List<Friends> friendData = FriendsTableOperations.getInstance().getFriendWithEmail(friendsData.getFriendsEmail());
            int friendRequestId = friendData.get(0).getFriend_request_id();
            int friendId = Integer.parseInt(friendData.get(0).getFriend_id());
            new FriendsDataMapper().acceptFriendRequest(onRequestAcceptedListener,friendRequestId,friendId);
        }
    }
    FriendsDataMapper.OnRequestAcceptedListener onRequestAcceptedListener = new FriendsDataMapper.OnRequestAcceptedListener() {
        @Override
        public void onTaskCompleted(FriendRequestAcceptResponse friendRequestAcceptResponse) {
            if(friendRequestAcceptResponse.isSuccess()){
                Toast.makeText(MyApplication.getCurrentActivityContext(),"Accepted",Toast.LENGTH_SHORT).show();
                new FriendsDataMapper().getFriends(onGetFriendsDataListener);
            }
        }

        @Override
        public void onTaskFailed(String response) {
            Toast.makeText(MyApplication.getCurrentActivityContext(),"Error: "+response,Toast.LENGTH_SHORT).show();
        }
    };

    FriendsDataMapper.OnTaskCompletedListener onGetFriendsDataListener = new FriendsDataMapper.OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted(FriendsServiceResponse friendsServiceResponse) {
            Toast.makeText(MyApplication.getCurrentActivityContext(),"Friends data Addeed: " +
                    friendsServiceResponse.isSuccess(),Toast.LENGTH_SHORT).show();
            finish();
            Navigator.getInstance().navigateToFriendsActivity();
        }

        @Override
        public void onTaskFailed(String response) {
            Toast.makeText(MyApplication.getCurrentActivityContext(),"Getting Friends data : "+response,Toast.LENGTH_SHORT).show();
        }
    };

    FriendsDataMapper.OnRequestSentListener sendRequestListener = new FriendsDataMapper.OnRequestSentListener() {
        @Override
        public void onTaskCompleted(FriendRequestResponse friendRequestResponse) {
            if(friendRequestResponse.isSuccess()){
                Toast.makeText(MyApplication.getCurrentActivityContext(),"Request Sent",Toast.LENGTH_SHORT).show();
                new FriendsDataMapper().getFriends(onGetFriendsDataListener);
            }else if(!friendRequestResponse.isSuccess()){
                emailEditText.setError(friendRequestResponse.getData());
            }
        }

        @Override
        public void onTaskFailed(String response) {
            Toast.makeText(MyApplication.getCurrentActivityContext(),"Error: "+response,Toast.LENGTH_SHORT).show();
        }
    };
}
