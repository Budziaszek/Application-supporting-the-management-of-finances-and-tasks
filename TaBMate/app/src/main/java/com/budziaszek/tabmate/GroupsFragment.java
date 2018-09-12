package com.budziaszek.tabmate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;


public class GroupsFragment extends Fragment {

    private static final String TAG = "GroupProcedure";

    private View fView;
    private ViewFlipper flipper;

    private MembersAdapter mMembersAdapter;
    private InvitationsAdapter mInvitationsAdapter;
    private List<User> users;

    private String mNewMemberEmail = null;
    private String mNewMemberId = null;

    private List<Group> groups = new ArrayList<>();
    private Integer currentGroup = null;
    private List<String> invitationsList = null;

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    //TODO override onbackpressed
    public enum FlipperPage{

        PROGRESS(0),
        NO_GROUP(1),
        CREATE_GROUP(2),
        VIEW_GROUP(3);

        private final int id;

        FlipperPage(int id) { this.id = id; }
        public int getValue() { return id; }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        users = new ArrayList<>();
        fView = inflater.inflate(R.layout.groups_fragment, container, false);
        initializeButtons();
        flipper = fView.findViewById(R.id.flipper);

        flipper.setDisplayedChild(FlipperPage.PROGRESS.getValue());
        firestoreRequests.getGroupByField("id", ((MainActivity)getActivity()).getCurrentUserId(), this::updateGroup);

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

    private void initializeButtons(){

        Button createGroupButton = fView.findViewById(R.id.create_button);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipper.setDisplayedChild(FlipperPage.CREATE_GROUP.getValue());
            }
        });

        Button submitGroupButton = fView.findViewById(R.id.submit_create_group);
        submitGroupButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                EditText groupNameEdit =  fView.findViewById(R.id.edit_group_name);
                EditText groupDescriptionEdit =  fView.findViewById(R.id.edit_group_description);

                String name =  groupNameEdit.getText().toString();
                String description =  groupDescriptionEdit.getText().toString();
                String id = ((MainActivity)getActivity()).getCurrentUserId();

                Manager.hideKeyboard(getActivity());

                addNewGroup(id, name, description);
            }
        });
    }

    private void addNewGroup(String id, String name, String description) {
        flipper.setDisplayedChild(FlipperPage.PROGRESS.getValue());
        Group newGroup = new Group(name + "_" + id, name, description);
        newGroup.addMember(id);

       firestoreRequests.addGroup(newGroup, name + " " + id,
               (Void) ->  {
                    Toast.makeText(fView.getContext(), "Group created",
                            Toast.LENGTH_SHORT).show();
                            flipper.setDisplayedChild(FlipperPage.PROGRESS.getValue());
                            firestoreRequests.getGroupByField("id", ((MainActivity)getActivity()).getCurrentUserId(), this::updateGroup);
                            Log.d(TAG, "Group added");
                            },
               (e) -> {Toast.makeText(fView.getContext(), e.toString(),
                            Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Error while adding group", e);
                            flipper.setDisplayedChild(FlipperPage.CREATE_GROUP.getValue());
                            });
    }

    private void addUserToList(DocumentSnapshot documentSnapshot){
        User user = documentSnapshot.toObject(User.class);
        users.add(user);
        mMembersAdapter.notifyDataSetChanged();
    }

    private void updateGroup(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            checkGroups(task);
        } else {
            Exception exception = task.getException();
            if(exception != null)
                Toast.makeText(fView.getContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkGroups(Task<QuerySnapshot> task){
        boolean foundGroup = false;
        //TODO add multiple groups to view
        for (QueryDocumentSnapshot document : task.getResult()) {

            Group group = document.toObject(Group.class);

            TextView groupName =  fView.findViewById(R.id.invitations_group_name);
            TextView groupDescription = fView.findViewById(R.id.group_description);
            RecyclerView members = fView.findViewById(R.id.members_list);

            for(String uid : group.getMembers()){
                firestoreRequests.getUser(uid, this::addUserToList);
            }

            mMembersAdapter = new MembersAdapter(users);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
            members.setLayoutManager(mLayoutManager);
            members.setItemAnimator(new DefaultItemAnimator());
            members.setAdapter(mMembersAdapter);

            groupName.setText(group.getName());
            groupDescription.setText(group.getDescription());

            groups.add(group);

            foundGroup = true;
            currentGroup = 0;
            break;
        }
        if(!foundGroup){
            flipper.setDisplayedChild(FlipperPage.NO_GROUP.getValue());
            firestoreRequests.getUser(((MainActivity)getActivity()).getCurrentUserId(), this::checkAndManageInvitations);
        }
        else{
            flipper.setDisplayedChild(FlipperPage.VIEW_GROUP.getValue());
        }
    }

    private void checkAndManageInvitations(DocumentSnapshot documentSnapshot){
            User user =  documentSnapshot.toObject(User.class);
            if(user != null) {
                invitationsList = user.getInvitations();
                //TODO get name from Firebase (now id is sent), what if it was removed
                if(invitationsList != null && !invitationsList.isEmpty()) {
                    RecyclerView invitations = fView.findViewById(R.id.invitations_list);
                    invitations.setAdapter(mInvitationsAdapter);
                    mInvitationsAdapter = new InvitationsAdapter(invitationsList, new ClickListener() {
                        @Override
                        public void onAcceptClicked(int position) {
                            firestoreRequests.addGroupMember(invitationsList.get(position), ((MainActivity)getActivity()).getCurrentUserId());
                            //TODO remove invitation from database, from list, inform adapter
                            Toast.makeText(fView.getContext(), "Accepted " + position, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRemoveClicked(int position) {
                            Toast.makeText(fView.getContext(), "Removed " + position, Toast.LENGTH_SHORT).show();
                        }
                    });
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
                    invitations.setLayoutManager(mLayoutManager);
                    invitations.setItemAnimator(new DefaultItemAnimator());
                    invitations.setAdapter(mInvitationsAdapter);
                }
            }
    }

    private void checkUser(Task<QuerySnapshot> task){
        List<DocumentSnapshot> documents = task.getResult().getDocuments();
        if(documents.isEmpty()){
            Toast.makeText(fView.getContext(), "User not found",
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "User" + mNewMemberEmail +" wasn't found");
        }
        else {
            User user = documents.get(0).toObject(User.class);
            if(user != null)
                mNewMemberId = user.getId();

            if(groups.get(currentGroup).getMembers().contains(mNewMemberId)){
                // Is already a member
                Toast.makeText(fView.getContext(), "User " + mNewMemberEmail + " is already a group member.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "User already a member " + mNewMemberId);
            }
            else {
                //Add
                firestoreRequests.addInvitation(mNewMemberId, groups.get(currentGroup).getId());
                Toast.makeText(fView.getContext(), "Invitation sent to " + mNewMemberEmail, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "User found " + mNewMemberId);
            }
        }
    }

    private void sendInvitation(Task<QuerySnapshot> task){
        if (task.isSuccessful()) {
            checkUser(task);
        } else {
            Exception exception = task.getException();
            if(exception != null) {
                Toast.makeText(fView.getContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Error while adding invitation " +exception.getMessage());
            }
        }
    }

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
                firestoreRequests.getUserByField("email", mNewMemberEmail, x -> sendInvitation(x));
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
