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
import com.budziaszek.tabmate.firestoreData.User;
import com.budziaszek.tabmate.view.adapter.GroupsItemsAdapter;
import com.budziaszek.tabmate.view.listener.GroupsClickListener;
import com.budziaszek.tabmate.view.InformUser;
import com.budziaszek.tabmate.view.listener.InvitationClickListener;
import com.budziaszek.tabmate.view.adapter.InvitationsItemsAdapter;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class MainPageFragment extends BasicFragment {

    private static final String TAG = "MainPageProcedure";

    private View fView;

    private GroupsItemsAdapter groupsAdapter;
    private ArrayList<Group> groups = new ArrayList<>();

    private InvitationsItemsAdapter invitationsAdapter;
    private List<String> invitationsList = new ArrayList<>();

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
       setRecyclerGroups();
        //Invitations
       setRecyclerInvitations();

       showProgress(true);
       ((MainActivity)getActivity()).refreshGroupsAndUsers();

       firestoreRequests.getUser(((MainActivity)getActivity()).getCurrentUserId(),
                MainPageFragment.this::checkAndManageInvitations);

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

    /**
     * Proceeds document, check and add invitations, if found manage their acceptation or removal.
     */
    private void checkAndManageInvitations(DocumentSnapshot documentSnapshot){
        swipeLayout.setRefreshing(false);
        User user =  documentSnapshot.toObject(User.class);
        if(user != null) {
            if(user.getInvitations() != null) {
                invitationsList = user.getInvitations();
                Log.d(TAG, "Invitations " +invitationsList.toString());
                invitationsAdapter.update(invitationsList);
            }
        }
    }

    private void setRecyclerGroups(){
        RecyclerView groupsRecycler = fView.findViewById(R.id.tasks_list);
        groupsAdapter = new GroupsItemsAdapter(groups, new GroupsClickListener(){
            @Override
            public void onItemLongClicked(int position){
                ((MainActivity)getActivity()).setCurrentGroupIndex(position);
                ((MainActivity)getActivity()).startFragment(DisplayGroupFragment.class);
            }
            @Override
            public void onItemClicked(int position){
                ((MainActivity)getActivity()).setCurrentGroupIndex(position);
                ((MainActivity)getActivity()).startFragment(DisplayGroupFragment.class);
            }
        }, ((MainActivity)getActivity()).getCurrentGroupIndex());

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        groupsRecycler.setLayoutManager(mLayoutManager);
        groupsRecycler.setItemAnimator(new DefaultItemAnimator());
        groupsRecycler.setAdapter(groupsAdapter);

        Button newGroupButton = fView.findViewById(R.id.new_group_button);
        newGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).startFragment(AddGroupFragment.class);
            }
        });
    }

    private void setRecyclerInvitations(){
        RecyclerView invitationsRecycler = fView.findViewById(R.id.invitations_list);
        invitationsAdapter = new InvitationsItemsAdapter(invitationsList, new InvitationClickListener() {
            @Override
            public void onAcceptClicked(int position) {
                showProgress(true);
                firestoreRequests.addGroupMember(invitationsList.get(position), ((MainActivity)getActivity()).getCurrentUserId(),
                        (aVoid) -> {
                            showProgress(false);
                            Log.d(TAG, "Invitation accepted.");
                        },
                        (e) -> {
                            InformUser.inform(getActivity(), R.string.invitation_incorrect);
                            showProgress(false);
                        });
                firestoreRequests.removeInvitation(invitationsList.get(position), ((MainActivity)getActivity()).getCurrentUserId(),
                        (aVoid) -> {
                            showProgress(false);
                            Log.d(TAG, "(Invalid) Invitation removed.");
                        },
                        (e) -> {
                            InformUser.informFailure(getActivity(), e);
                            Log.e(TAG, e.getMessage());
                            showProgress(false);
                        });
                firestoreRequests.getUser(((MainActivity)getActivity()).getCurrentUserId(),
                        MainPageFragment.this::checkAndManageInvitations);
                ((MainActivity)getActivity()).refreshGroupsAndUsers();

            }
            @Override
            public void onRemoveClicked(int position) {
                firestoreRequests.removeInvitation(invitationsList.get(position), ((MainActivity)getActivity()).getCurrentUserId(),
                        (aVoid) -> {
                            showProgress(false);
                            Log.d(TAG, "Invitation removed.");
                            //((MainActivity)getActivity()).startFragment(DisplayGroupFragment.class);
                            firestoreRequests.getUser(((MainActivity)getActivity()).getCurrentUserId(),
                                    MainPageFragment.this::checkAndManageInvitations);
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
}