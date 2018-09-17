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

import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.view.InformUser;
import com.budziaszek.tabmate.firestoreData.User;
import com.budziaszek.tabmate.view.InvitationClickListener;
import com.budziaszek.tabmate.view.InvitationsAdapter;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NewGroupFragment extends BasicFragment {

    private static final String TAG = "NewGroupProcedure";

    private InvitationsAdapter invitationsAdapter;
    private List<String> invitationsList = new ArrayList<>();

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fView = inflater.inflate(R.layout.fragment_new_group, container,false);
        mDisplayView = fView.findViewById(R.id.no_group_layout);
        mProgressView = fView.findViewById(R.id.progress_no_group);

        //Refresh
        swipeLayout = fView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                firestoreRequests.getGroupByField("members",
                        ((MainActivity)getActivity()).getCurrentUserId(),
                        NewGroupFragment.this::checkGroupsTask);
            }
        });
        swipeLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()),
                getResources().getColor(R.color.colorAccentDark, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()));

        //Button create
        Button createGroupButton = fView.findViewById(R.id.create_button);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).startFragment(AddGroupFragment.class);
            }
        });

        //Check data and display or go on
        showProgress(true);
        firestoreRequests.getGroupByField("members", ((MainActivity)getActivity()).getCurrentUserId(), this::checkGroupsTask);

        //Invitations
        RecyclerView invitationsRecycler = fView.findViewById(R.id.invitations_list);
        invitationsAdapter = new InvitationsAdapter(invitationsList, new InvitationClickListener() {
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
                firestoreRequests.getGroupByField("members", ((MainActivity)getActivity()).getCurrentUserId(),
                        NewGroupFragment.this::checkGroupsTask);
            }
            @Override
            public void onRemoveClicked(int position) {
                firestoreRequests.removeInvitation(invitationsList.get(position), ((MainActivity)getActivity()).getCurrentUserId(),
                        (aVoid) -> {
                            showProgress(false);
                            Log.d(TAG, "Invitation removed.");
                            //((MainActivity)getActivity()).startFragment(DisplayGroupFragment.class);
                            firestoreRequests.getGroupByField("members", ((MainActivity)getActivity()).getCurrentUserId(),
                                     NewGroupFragment.this::checkGroupsTask);
                        },
                        (e) -> {
                            InformUser.informFailure(getActivity(), e);
                            Log.e(TAG, e.getMessage());
                            showProgress(false);
                        });
            }
        });
        invitationsRecycler.setAdapter(invitationsAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        invitationsRecycler.setLayoutManager(mLayoutManager);
        invitationsRecycler.setItemAnimator(new DefaultItemAnimator());
        invitationsRecycler.setAdapter(invitationsAdapter);

        return fView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    private void checkGroupsTask(Task<QuerySnapshot> task) {
        showProgress(false);
        if (task.isSuccessful()) {
            swipeLayout.setRefreshing(false);
            if(task.getResult().getDocuments().isEmpty()){
                Log.d(TAG, "No group found");
                //TODO Activity might be null? exception occurred once
                firestoreRequests.getUser(((MainActivity)getActivity()).getCurrentUserId(), this::checkAndManageInvitations);
            }
            firestoreRequests.getUser(((MainActivity)getActivity()).getCurrentUserId(), this::checkAndManageInvitations);
        } else {
            swipeLayout.setRefreshing(false);
            Exception exception = task.getException();
            if(exception != null)
                InformUser.informFailure(getActivity(), exception);
        }
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
}
