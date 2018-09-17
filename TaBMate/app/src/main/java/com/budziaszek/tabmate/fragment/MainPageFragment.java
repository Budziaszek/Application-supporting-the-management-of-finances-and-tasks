package com.budziaszek.tabmate.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.view.GroupsAdapter;
import com.budziaszek.tabmate.view.GroupsClickListener;

import java.util.ArrayList;
import java.util.List;

public class MainPageFragment extends BasicFragment {

    private static final String TAG = "MainPageProcedure";

    private View fView;

    private GroupsAdapter groupsAdapter;
    private ArrayList<Group> groups = new ArrayList<>();

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fView = inflater.inflate(R.layout.main_page, container, false);

        mDisplayView = fView.findViewById(R.id.user_groups_layout);
        mProgressView = fView.findViewById(R.id.progress_groups);

        //Refresh
        swipeLayout = fView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ((MainActivity)getActivity()).refreshGroupsAndUsers();
            }
        });
        swipeLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()),
                getResources().getColor(R.color.colorAccentDark, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()));

        // Groups
        RecyclerView groupsRecycler = fView.findViewById(R.id.tasks_list);
        groupsAdapter = new GroupsAdapter(groups, new GroupsClickListener(){
            @Override
            public void onItemLongClicked(int position){
                ((MainActivity)getActivity()).setCurrentGroupIndex(position);
                ((MainActivity)getActivity()).startFragment(DisplayGroupFragment.class);
            }
            @Override
            public void onItemClicked(int position){
                ((MainActivity)getActivity()).setCurrentGroupIndex(position);
            }
            @Override
            public void onEditClicked(int position){
                ((MainActivity)getActivity()).setCurrentGroupIndex(position);
                ((MainActivity)getActivity()).startEditFragment();
            }
            @Override
            public void onLeaveClicked(int position){
                ((MainActivity)getActivity()).setCurrentGroupIndex(position);
                ((MainActivity)getActivity()).alertLeaveGroup();
                //showProgress(true);
            }
        }, ((MainActivity)getActivity()).getCurrentGroupIndex());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        groupsRecycler.setLayoutManager(mLayoutManager);
        groupsRecycler.setItemAnimator(new DefaultItemAnimator());
        groupsRecycler.setAdapter(groupsAdapter);

        Button newGroupButton = fView.findViewById(R.id.new_task_button);
        newGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).startFragment(NewGroupFragment.class);
            }
        });

        showProgress(true);
        ((MainActivity)getActivity()).refreshGroupsAndUsers();

        return fView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public void afterRefresh(){
        List<Group> groups = ((MainActivity)getActivity()).getGroups();
        groupsAdapter.update(groups);
        Log.d(TAG, "after refresh");
    }
}