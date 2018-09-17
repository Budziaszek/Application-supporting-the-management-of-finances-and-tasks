package com.budziaszek.tabmate.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.view.InformUser;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.firestoreData.User;
import com.budziaszek.tabmate.view.MemberClickListener;
import com.budziaszek.tabmate.view.MembersAdapter;
import com.budziaszek.tabmate.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//TODO administrator
public class DisplayGroupFragment extends BasicFragment{

    private static final String TAG = "DisplayGroupProcedure";

    private View fView;
    private FloatingActionButton next;
    private FloatingActionButton previous;

    private MembersAdapter membersAdapter;
    private List<User> users = new ArrayList<>();

    private String newMemberEmail = null;
    private String newMemberId = null;

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fView = inflater.inflate(R.layout.fragment_display_group, container, false);

        mDisplayView = fView.findViewById(R.id.show_groups_layout);
        mProgressView = fView.findViewById(R.id.progress_display);

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

        // Members
        RecyclerView membersRecycler = fView.findViewById(R.id.tasks_list);
        membersAdapter = new MembersAdapter(users, new MemberClickListener() {
            @Override
            public void onLeaveClicked(int position) {
                ((MainActivity)getActivity()).alertLeaveGroup();
            }
        }, ((MainActivity)getActivity()).getCurrentUserId());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        membersRecycler.setLayoutManager(mLayoutManager);
        membersRecycler.setItemAnimator(new DefaultItemAnimator());
        membersRecycler.setAdapter(membersAdapter);

        Button addMemberButton = fView.findViewById(R.id.add_member_button);
        addMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertNewMember();
            }
        });

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
                if(((MainActivity)getActivity()).setPreviousGroupIndex()){
                    previous.setVisibility(View.INVISIBLE);
                }
                next.setVisibility(View.VISIBLE);
                showGroup();
            }
        });

        showProgress(true);
        ((MainActivity)getActivity()).refreshGroupsAndUsers();
        return fView;
    }

    @Override
    public void afterRefresh(){
        List<Group> groups = ((MainActivity)getActivity()).getGroups();
        int currentGroupIndex = ((MainActivity)getActivity()).getCurrentGroupIndex();
        if(groups.isEmpty()) {
            ((MainActivity) getActivity()).startFragment(NewGroupFragment.class);
            Log.d(TAG, "Group not found");
        }
        else {
            previous.setVisibility(View.VISIBLE);
            next.setVisibility(View.VISIBLE);

            if(groups.size() < 2 || currentGroupIndex == groups.size() - 1){
                next.setVisibility(View.INVISIBLE);
            }
            if(groups.size() - 1 == currentGroupIndex){
                next.setVisibility(View.INVISIBLE);
            }
        }
        showGroup();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    /*@Override
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
        int id = item.getItemId();

        if(id == R.id.action_new_member){
            alertNewMember();
            return true;
        }else if(id == R.id.action_leave){
            alertLeaveGroup();
            return true;
        }else
        if(id == R.id.action_edit_group){
            ((MainActivity)getActivity()).startEditFragment();
            return true;
        }else if(id == R.id.action_new_group){
            ((MainActivity)getActivity()).startFragment(NewGroupFragment.class);
            return true;
        }
        return false;
    }*/

    /**
     * Displays current group data.
     */
    private void showGroup(){
        Group group = ((MainActivity)getActivity()).getCurrentGroup();

        TextView groupName = fView.findViewById(R.id.group_name);
        TextView groupDescription = fView.findViewById(R.id.group_description);

        groupName.setText(group.getName());
        groupDescription.setText(group.getDescription());

        Map<String, User> allUsers =  ((MainActivity)getActivity()).getUsers();
        users = new ArrayList<>();
        List<String> members = group.getMembers();
        for(int i = 0; i<members.size(); i++){
            String memberId = members.get(i);
            if(allUsers.containsKey(memberId)) {
                users.add(allUsers.get(memberId));
            }
        }
        membersAdapter.update(users);
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
                newMemberId = user.getId();
            Group currentGroup = ((MainActivity)getActivity()).getCurrentGroup();
            if(currentGroup.getMembers().contains(newMemberId)){
                // Is already a member
                InformUser.inform(getActivity(), R.string.user_is_a_member);
            }
            else {
                //Add
                firestoreRequests.addInvitation(newMemberId, currentGroup.getId(),
                        (aVoid) ->  InformUser.inform(getActivity(), R.string.invitation_sent),
                        (e) -> InformUser.inform(getActivity(), R.string.invitation_incorrect));
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
                newMemberEmail = input.getText().toString();
                firestoreRequests.getUserByField("email", newMemberEmail, x -> checkInvitationTask(x));
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
