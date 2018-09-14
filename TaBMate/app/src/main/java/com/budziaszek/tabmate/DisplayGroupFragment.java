package com.budziaszek.tabmate;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
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

//TODO edit group
//TODO bug displaying members
//TODO administrator
public class DisplayGroupFragment extends Fragment {

    private static final String TAG = "DisplayGroupProcedure";

    private View fView;
    private SwipeRefreshLayout swipeLayout;

    private RecyclerView membersRecycler;
    private MembersAdapter mMembersAdapter;

    private FloatingActionButton next;
    private FloatingActionButton previous;

    private String mNewMemberEmail = null;
    private String mNewMemberId = null;

    private List<User> users = new ArrayList<>();

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fView = inflater.inflate(R.layout.display_group, container, false);

        //Refresh
        swipeLayout = fView.findViewById(R.id.swipe_container);
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

        firestoreRequests.getGroupByField("members", ((MainActivity)getActivity()).getCurrentUserId(), this::checkGroupsTask);

        // Members
        membersRecycler = fView.findViewById(R.id.members_list);
        mMembersAdapter = new MembersAdapter(users);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        membersRecycler.setLayoutManager(mLayoutManager);
        membersRecycler.setItemAnimator(new DefaultItemAnimator());
        membersRecycler.setAdapter(mMembersAdapter);

        //Navigation buttons
        next = fView.findViewById(R.id.next_button);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((MainActivity)getActivity()).setNextGroup()) {
                    next.setVisibility(View.INVISIBLE);
                }
                previous.setVisibility(View.VISIBLE);
                showGroup();
            }
        });

        previous = fView.findViewById(R.id.previous_button);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((MainActivity)getActivity()).setPreviousGroup()){
                    previous.setVisibility(View.INVISIBLE);
                }
                next.setVisibility(View.VISIBLE);
                showGroup();
            }
        });
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_new_member){
            alertNewMember();
            return true;
        }else if(id == R.id.action_new_group){
            ((MainActivity)getActivity()).startFragment(NewGroupFragment.class);
            return true;
        }else if(id == R.id.action_leave){
            alertLeaveGroup();
            return true;
        }else if(id == R.id.action_edit_group){
            ((MainActivity)getActivity()).startEditFragment();
            return true;
        }

        return false;
    }

    /**
     * Reads user from DocumentSnapshot and adds it to list.
     */
    private void addUserToList(DocumentSnapshot documentSnapshot){
        User user = documentSnapshot.toObject(User.class);
        ((MainActivity)getActivity()).addUser(user);
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
            showProgress(false);
        } else {
            Exception exception = task.getException();
            if(exception != null)
                InformUser.informFailure(getActivity(), exception);
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * Proceeds documents and update user groups if found.
     */
    private void checkGroups(QuerySnapshot documents){
        ((MainActivity)getActivity()).resetGroups();
        if(documents.isEmpty()) {
            ((MainActivity) getActivity()).startFragment(NewGroupFragment.class);
            Log.d(TAG, "Group not found");
        }
        else {
            if(documents.size() > 1){
                next.setVisibility(View.VISIBLE);
                previous.setVisibility(View.INVISIBLE);
                ((MainActivity)getActivity()).setCurrentGroup(0);
            }
            else{
                previous.setVisibility(View.INVISIBLE);
                next.setVisibility(View.INVISIBLE);
            }
            for (QueryDocumentSnapshot document : documents) {
                Group group = document.toObject(Group.class);
                Log.d(TAG, "User group: " + group.getId());
                ((MainActivity) getActivity()).addGroup(group);
            }
            showGroup();
        }
    }

    /**
     * Displays current group data.
     */
    private void showGroup(){
        users = new ArrayList<>();
        ((MainActivity)getActivity()).clearUsers();
        mMembersAdapter.update(users);

        Group group = ((MainActivity)getActivity()).getCurrentGroup();

        TextView groupName = fView.findViewById(R.id.group_name);
        TextView groupDescription = fView.findViewById(R.id.group_description);

        groupName.setText(group.getName());
        groupDescription.setText(group.getDescription());

        for (String uid : group.getMembers()) {
            firestoreRequests.getUser(uid, this::addUserToList);
        }
    }

    /**
     * Check document with potential new member and send invitation if everything is correct.
     */
    private void checkNewMember(List<DocumentSnapshot> documents){
        if(documents.isEmpty()){
            InformUser.inform(getActivity(), R.string.user_not_found);
        }
        else {
            User user = documents.get(0).toObject(User.class);
            if(user != null)
                mNewMemberId = user.getId();
            Group currentGroup = ((MainActivity)getActivity()).getCurrentGroup();
            if(currentGroup.getMembers().contains(mNewMemberId)){
                // Is already a member
                InformUser.inform(getActivity(), R.string.user_is_a_member);
            }
            else {
                //Add
                firestoreRequests.addInvitation(mNewMemberId, currentGroup.getId(),
                        (aVoid) ->  InformUser.inform(getActivity(), R.string.invitation_sent),
                        (e) -> InformUser.inform(getActivity(), R.string.invitation_incorrect));
               ;
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
                InformUser.informFailure(getActivity(), exception);
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

    /**
     * Displays alert and removes user group group if submitted.
     */
    private void alertLeaveGroup(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);

        builder.setTitle(R.string.leave_group)
                .setMessage(R.string.confirm_leave_group)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Group currentGroup = ((MainActivity)getActivity()).getCurrentGroup();
                        if(currentGroup.getMembers().size() > 1) {
                            //Remove only user
                            firestoreRequests.removeGroupMember(currentGroup.getId(),
                                    ((MainActivity) getActivity()).getCurrentUserId(),
                                    (aVoid) -> {
                                        InformUser.inform(getActivity(), R.string.left_group);
                                        firestoreRequests.getGroupByField("members", ((MainActivity) getActivity()).getCurrentUserId(),
                                                DisplayGroupFragment.this::checkGroupsTask);
                                    },
                                    (e) -> InformUser.informFailure(getActivity(), e)
                            );
                        }
                        else{
                            //Remove whole group
                            firestoreRequests.removeGroup(currentGroup.getId(),
                                    (aVoid) -> {
                                        InformUser.inform(getActivity(), R.string.left_group);
                                        firestoreRequests.getGroupByField("members", ((MainActivity) getActivity()).getCurrentUserId(),
                                                DisplayGroupFragment.this::checkGroupsTask);

                                    },
                                    (e) -> InformUser.informFailure(getActivity(), e)
                            );
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        View mDisplayView= fView.findViewById(R.id.show_groups_layout);
        View mProgressView = fView.findViewById(R.id.progress_display);

        mDisplayView.setVisibility(show ? View.GONE : View.VISIBLE);
        mDisplayView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mDisplayView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

}
