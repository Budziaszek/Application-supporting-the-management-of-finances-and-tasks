package com.budziaszek.tabmate;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NewGroupFragment extends Fragment {

    private static final String TAG = "myNewFragment";

    private View fView;
    private SwipeRefreshLayout swipeLayout;

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    private RecyclerView invitationsRecycler;
    private InvitationsAdapter mInvitationsAdapter;
    private List<String> invitationsList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fView = inflater.inflate(R.layout.new_group, container,false);

        //Refresh
        swipeLayout = (SwipeRefreshLayout) fView.findViewById(R.id.swipe_container);
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
        swipeLayout.setRefreshing(true);
        firestoreRequests.getGroupByField("members", ((MainActivity)getActivity()).getCurrentUserId(), this::checkGroupsTask);

        //Invitations
        invitationsRecycler = fView.findViewById(R.id.invitations_list);
        mInvitationsAdapter = new InvitationsAdapter(invitationsList, new ClickListener() {
            @Override
            public void onAcceptClicked(int position) {
                firestoreRequests.addGroupMember(invitationsList.get(position), ((MainActivity)getActivity()).getCurrentUserId());
                //TODO remove invitation from database, from list, inform adapter
                //Toast.makeText(fView.getContext(), "Accepted " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRemoveClicked(int position) {
                //Toast.makeText(fView.getContext(), "Removed " + position, Toast.LENGTH_SHORT).show();
            }
        });
        invitationsRecycler.setAdapter(mInvitationsAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        invitationsRecycler.setLayoutManager(mLayoutManager);
        invitationsRecycler.setItemAnimator(new DefaultItemAnimator());
        invitationsRecycler.setAdapter(mInvitationsAdapter);

        return fView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    private void checkGroupsTask(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            if(task.getResult().getDocuments().isEmpty()){
                InformUser.log(TAG, "No group found");
                //TODO Activity might be null?
                firestoreRequests.getUser(((MainActivity)getActivity()).getCurrentUserId(), this::checkAndManageInvitations);
            }
            else{
                InformUser.log(TAG, "Group found");
                swipeLayout.setRefreshing(false);
                ((MainActivity)getActivity()).startFragment(DisplayGroupFragment.class);
            }
        } else {
            swipeLayout.setRefreshing(false);
            Exception exception = task.getException();
            if(exception != null)
                InformUser.informFailure(getActivity(), TAG, exception);
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
                InformUser.log(TAG, "Invitations " +invitationsList.toString());
                mInvitationsAdapter.update(invitationsList);
            }
            //TODO get name from Firebase (now id is sent), what if it was removed
        }
    }
}
