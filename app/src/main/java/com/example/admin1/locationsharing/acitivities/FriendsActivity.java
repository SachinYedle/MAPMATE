package com.example.admin1.locationsharing.acitivities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.adapters.FriendsRecyclerViewAdapter;
import com.example.admin1.locationsharing.app.MyApplication;
import com.example.admin1.locationsharing.db.dao.Friends;
import com.example.admin1.locationsharing.db.dao.operations.FriendsTableOperations;
import com.example.admin1.locationsharing.interfaces.ItemClickListener;
import com.example.admin1.locationsharing.pojo.FriendsData;
import com.example.admin1.locationsharing.responses.FriendsResponse;

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
        FriendsTableOperations.getInstance().deleteFriendsTableData();
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
            }else if(Integer.parseInt(friends.getRequester_id()) == Integer.parseInt(friends.getFriend_id())){
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
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {

    }
}
