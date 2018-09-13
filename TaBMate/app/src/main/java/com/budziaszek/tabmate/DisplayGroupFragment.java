package com.budziaszek.tabmate;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DisplayGroupFragment extends Fragment {

    private static final String TAG = "GroupProcedure";

    private View fView;
    private SwipeRefreshLayout swipeLayout;

    private RecyclerView membersRecycler;
    private MembersAdapter mMembersAdapter;
    private List<User> users;

    private String mNewMemberEmail = null;
    private String mNewMemberId = null;

    private FirestoreRequests firestoreRequests = new FirestoreRequests();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        users = new ArrayList<>();
        fView = inflater.inflate(R.layout.display_group, container, false);

        //Refresh
        swipeLayout = (SwipeRefreshLayout) fView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                firestoreRequests.getGroupByField("members",
                        ((MainActivity)getActivity()).getCurrentUserId(),
                        DisplayGroupFragment.this::checkGroupsTask);
            }
        });
        swipeLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()),
                getResources().getColor(R.color.colorAccentDark, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()));


        swipeLayout.setRefreshing(true);
        firestoreRequests.getGroupByField("members", ((MainActivity)getActivity()).getCurrentUserId(), this::checkGroupsTask);

        // Members
        membersRecycler = fView.findViewById(R.id.members_list);
        mMembersAdapter = new MembersAdapter(users);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        membersRecycler.setLayoutManager(mLayoutManager);
        membersRecycler.setItemAnimator(new DefaultItemAnimator());
        membersRecycler.setAdapter(mMembersAdapter);

        return fView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();    //remove all items
        getActivity().getMenuInflater().inflate(R.menu.menu_groups, menu);
    }

    @Override
    //TODO set some options visible or invisible
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return false;
        }else if(id == R.id.action_new_member){
            alertNewMember();
            return true;
        }

        return false;
    }

    /**
     * Reads user from DocumentSnapshot and adds it to list.
     */
    private void addUserToList(DocumentSnapshot documentSnapshot){
        User user = documentSnapshot.toObject(User.class);
        users.add(user);
        mMembersAdapter.update(users);
    }

    /**
     * Receives task with and check if it was successful.
     */
    private void checkGroupsTask(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            checkGroups(task.getResult());
            swipeLayout.setRefreshing(false);
        } else {
            Exception exception = task.getException();
            if(exception != null)
                InformUser.informFailure(getActivity(), TAG, exception);
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * Proceeds documents and update user groups if found.
     */
    private void checkGroups(QuerySnapshot documents){
        //TODO on refresh different acttion - update only current group, don't ask for all!
        users = new ArrayList<>();
        boolean foundGroup = false;
        //TODO add multiple groups to view
        for (QueryDocumentSnapshot document : documents) {

            Group group = document.toObject(Group.class);
            InformUser.log(TAG, "User group: " + group.getId());

            TextView groupName =  fView.findViewById(R.id.group_name);
            TextView groupDescription = fView.findViewById(R.id.group_description);


            for(String uid : group.getMembers()){
                firestoreRequests.getUser(uid, this::addUserToList);
            }
            groupName.setText(group.getName());
            groupDescription.setText(group.getDescription());



            ((MainActivity)getActivity()).addGroup(group);
            foundGroup = true;

            break;
        }
        if(!foundGroup){
            InformUser.log(TAG, "Group not found");
            ((MainActivity)getActivity()).startFragment(NewGroupFragment.class);
        }
    }

    /**
     * Check document with potential new member and send invitation if everything is correct.
     */
    private void checkNewMember(List<DocumentSnapshot> documents){
        if(documents.isEmpty()){
            InformUser.inform(getActivity(), TAG, "User" + mNewMemberEmail +" not found");
        }
        else {
            User user = documents.get(0).toObject(User.class);
            if(user != null)
                mNewMemberId = user.getId();
            Group currentGroup = ((MainActivity)getActivity()).getCurrentGroup();
            if(currentGroup.getMembers().contains(mNewMemberId)){
                // Is already a member
                InformUser.inform(getActivity(), TAG, "User " + mNewMemberEmail + " is already a group member.");
            }
            else {
                //Add
                firestoreRequests.addInvitation(mNewMemberId, currentGroup.getId());
                InformUser.inform(getActivity(), TAG, "Invitation sent to " + mNewMemberEmail);
            }
        }
    }

    /**
     * Check if task was successful and call checkMember if so.
     */
    private void checkInvitationTask(Task<QuerySnapshot> task){
        if (task.isSuccessful()) {
            checkNewMember(task.getResult().getDocuments());
        } else {
            Exception exception = task.getException();
            if(exception != null) {
                InformUser.informFailure(getActivity(), TAG, exception);
            }
        }
    }

    /**
     * Alert gets from user email of potential new member, finds it in database and call functions to proceed.
     */
    private void alertNewMember(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Enter user email");

        // Set up the input
        final EditText input = new EditText(getActivity());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setPadding(20, 20, 20, 20);
        input.setBackgroundColor(getResources().getColor(R.color.colorAccentLightSemi, getActivity().getTheme()));

        FrameLayout container = new FrameLayout(getActivity());
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(30, 10, 30, 10);
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        // Set up the buttons
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mNewMemberEmail = input.getText().toString();
                //sendInvitation();
                firestoreRequests.getUserByField("email", mNewMemberEmail, x -> checkInvitationTask(x));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

}
