package com.budziaszek.tabmate.fragment;

import android.app.Activity;
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
import com.budziaszek.tabmate.firestoreData.DataManager;
import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.firestoreData.User;
import com.budziaszek.tabmate.view.DataChangeListener;
import com.budziaszek.tabmate.view.adapter.GroupsItemsAdapter;
import com.budziaszek.tabmate.view.listener.GroupsClickListener;
import com.budziaszek.tabmate.view.InformUser;
import com.budziaszek.tabmate.view.listener.InvitationClickListener;
import com.budziaszek.tabmate.view.adapter.InvitationsItemsAdapter;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class MainPageFragment extends BasicFragment implements DataChangeListener {

    private static final String TAG = "MainPageProcedure";

    private Activity activity;

    private View fView;

    private GroupsItemsAdapter groupsAdapter;
    private List<Group> groups = new ArrayList<>();

    private InvitationsItemsAdapter invitationsAdapter;
    private List<String> invitationsList = new ArrayList<>();

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fView = inflater.inflate(R.layout.main_page, container, false);
        activity = getActivity();

        invitationsList.clear();
        groups.clear();

        mDisplayView = fView.findViewById(R.id.user_groups_layout);
        mProgressView = fView.findViewById(R.id.progress_groups);

        //Refresh
        swipeLayout = fView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                groups = new ArrayList<>();
                Log.d(TAG, "Ask for refresh groups and users");
                DataManager.getInstance().refreshGroupsAndUsers(((MainActivity)activity).getCurrentUserId());

                Log.d(TAG, "Ask for refresh invitations");
                DataManager.getInstance().refreshInvitations(((MainActivity)activity).getCurrentUserId());
                swipeLayout.setRefreshing(false);
            }
        });
        swipeLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()),
                getResources().getColor(R.color.colorAccentDark, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()));

        // Groups
        setRecyclerGroups();
        //Invitations
        setRecyclerInvitations();

        groups = new ArrayList<>();

        DataManager instance = DataManager.getInstance();
        if(instance.getGroups() == null) {
            Log.d(TAG, "Ask for refresh groups and users");
            instance.refreshGroupsAndUsers(((MainActivity) activity).getCurrentUserId());
        }else{
            groupsChanged();
        }

        if(instance.getInvitations() == null) {
            Log.d(TAG, "Ask for refresh invitations");
            instance.refreshInvitations(((MainActivity) activity).getCurrentUserId());
        }else{
            invitationsChanged();
        }


        DataManager.getInstance().addObserver((DataChangeListener)this);
        showProgress(false);

        return fView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    private void setRecyclerGroups(){
        RecyclerView groupsRecycler = fView.findViewById(R.id.tasks_list);
        groupsAdapter = new GroupsItemsAdapter(groups, new GroupsClickListener(){
            @Override
            public void onItemLongClicked(int position){
                ((MainActivity)activity).setCurrentGroupIndex(position);
                ((MainActivity)activity).startFragment(DisplayGroupFragment.class);
            }
            @Override
            public void onItemClicked(int position){
                ((MainActivity)activity).setCurrentGroupIndex(position);
                ((MainActivity)activity).startFragment(DisplayGroupFragment.class);
            }
        }, ((MainActivity)activity).getCurrentGroupIndex());

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        groupsRecycler.setLayoutManager(mLayoutManager);
        groupsRecycler.setItemAnimator(new DefaultItemAnimator());
        groupsRecycler.setAdapter(groupsAdapter);

        Button newGroupButton = fView.findViewById(R.id.new_group_button);
        newGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)activity).enableBack(true);
                ((MainActivity)activity).startFragment(AddGroupFragment.class);
            }
        });
    }

    private void setRecyclerInvitations(){
        RecyclerView invitationsRecycler = fView.findViewById(R.id.invitations_list);
        invitationsAdapter = new InvitationsItemsAdapter(invitationsList, new InvitationClickListener() {
            @Override
            public void onAcceptClicked(int position) {
                showProgress(true);
                firestoreRequests.addGroupMember(invitationsList.get(position), ((MainActivity)activity).getCurrentUserId(),
                        (aVoid) -> {
                            showProgress(false);
                            Log.d(TAG, "Invitation accepted.");
                        },
                        (e) -> {
                            InformUser.inform(getActivity(), R.string.invitation_incorrect);
                            showProgress(false);
                        });
                firestoreRequests.removeInvitation(invitationsList.get(position), ((MainActivity)activity).getCurrentUserId(),
                        (aVoid) -> {
                            showProgress(false);
                            Log.d(TAG, "(Invalid) Invitation removed.");
                        },
                        (e) -> {
                            InformUser.informFailure(getActivity(), e);
                            Log.e(TAG, e.getMessage());
                            showProgress(false);
                        });
                DataManager.getInstance().refreshInvitations(((MainActivity)getActivity()).getCurrentUserId());
                DataManager.getInstance().refreshGroupsAndUsers(((MainActivity)activity).getCurrentUserId());

            }
            @Override
            public void onRemoveClicked(int position) {
                firestoreRequests.removeInvitation(invitationsList.get(position), ((MainActivity)activity).getCurrentUserId(),
                        (aVoid) -> {
                            showProgress(false);
                            Log.d(TAG, "Invitation removed.");
                            DataManager.getInstance().refreshInvitations(((MainActivity)getActivity()).getCurrentUserId());
                        },
                        (e) -> {
                            InformUser.informFailure(getActivity(), e);
                            Log.e(TAG, e.getMessage());
                            showProgress(false);
                        });
            }
        });
        invitationsRecycler.setAdapter(invitationsAdapter);
        RecyclerView.LayoutManager mLayoutManagerInvitations = new LinearLayoutManager(fView.getContext());
        invitationsRecycler.setLayoutManager(mLayoutManagerInvitations);
        invitationsRecycler.setItemAnimator(new DefaultItemAnimator());
        invitationsRecycler.setAdapter(invitationsAdapter);
    }

    @Override
    public void invitationsChanged(){
        invitationsList = DataManager.getInstance().getInvitations();
        invitationsAdapter.update(invitationsList);
    }

    @Override
    public void groupsChanged(){
        groups = DataManager.getInstance().getGroups();
        groupsAdapter.update(groups);
    }
}