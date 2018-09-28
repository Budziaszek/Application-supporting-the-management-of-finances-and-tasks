package com.budziaszek.tabmate.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
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

import com.budziaszek.tabmate.firestoreData.DataManager;
import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.firestoreData.UserTask;
import com.budziaszek.tabmate.view.InformUser;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.firestoreData.User;
import com.budziaszek.tabmate.view.KeyboardManager;
import com.budziaszek.tabmate.view.adapter.GroupSpinnerAdapter;
import com.budziaszek.tabmate.view.adapter.MembersItemsAdapter;
import com.budziaszek.tabmate.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//TODO administrator
public class GroupFragment extends BasicFragment {

    private static final String TAG = "DisplayGroupProcedure";
    private Activity activity;

    private View fView;

    private Group group;

    private MembersItemsAdapter membersAdapter;
    private List<User> users = new ArrayList<>();

    private String newMemberEmail = null;
    private String newMemberId = null;

    private Boolean isEdited;
    private Boolean isCreated;

    private TextView groupName;
    private TextView groupDescription;
    private TextView groupNameInput;
    private TextView groupDescriptionInput;

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Created");
        fView = inflater.inflate(R.layout.group, container, false);

        activity = getActivity();
        group = ((MainActivity) getActivity()).getCurrentGroup();

        groupName = fView.findViewById(R.id.group_name);
        groupDescription = fView.findViewById(R.id.group_description);
        groupNameInput = fView.findViewById(R.id.group_name_input);
        groupDescriptionInput = fView.findViewById(R.id.group_description_input);

        // Add group
        if (group == null) {
            fView.findViewById(R.id.members_layout).setVisibility(View.INVISIBLE);
            TextView title = fView.findViewById(R.id.details_title);
            title.setText(R.string.create_group_short);
            group = new Group("", "", new ArrayList<>());
            isCreated = true;
            setEditing(true);
        } else {
            isCreated = false;
            setEditing(false);
        }

        // Members
        RecyclerView membersRecycler = fView.findViewById(R.id.members_list);
        membersAdapter = new MembersItemsAdapter(users, position -> {
            alertLeaveGroup();
            DataManager.getInstance().refresh(((MainActivity) activity).getCurrentUserId());
        }, ((MainActivity) activity).getCurrentUserId());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        membersRecycler.setLayoutManager(mLayoutManager);
        membersRecycler.setItemAnimator(new DefaultItemAnimator());
        membersRecycler.setAdapter(membersAdapter);

        Button addMemberButton = fView.findViewById(R.id.add_member_button);
        addMemberButton.setOnClickListener(view -> alertNewMember());

        ((MainActivity) activity).enableBack(true);
        showGroup();
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
        getActivity().getMenuInflater().inflate(R.menu.menu_details, menu);

        MenuItem edit = menu.findItem(R.id.action_edit);
        MenuItem save = menu.findItem(R.id.action_save);

        if (isEdited) {
            edit.setVisible(false);
            save.setVisible(true);
        } else {
            edit.setVisible(true);
            save.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            setEditing(true);
            fView.findViewById(R.id.members_layout).setVisibility(View.INVISIBLE);
            activity.invalidateOptionsMenu();
            return true;
        } else if (id == R.id.action_save) {
            fView.findViewById(R.id.members_layout).setVisibility(View.VISIBLE);
            if (update()) {
                setEditing(false);
                activity.invalidateOptionsMenu();
                KeyboardManager.hideKeyboard(activity);
            }
            return true;
        }
        return false;
    }

    /**
     * Displays current group data.
     */
    private void showGroup() {
        groupName.setText(group.getName());
        groupDescription.setText(group.getDescription());

        Map<String, User> allUsers = DataManager.getInstance().getUsers();
        users = new ArrayList<>();
        List<String> members = group.getMembers();
        for (int i = 0; i < members.size(); i++) {
            String memberId = members.get(i);
            if (allUsers.containsKey(memberId)) {
                users.add(allUsers.get(memberId));
            }
        }
        membersAdapter.update(users);
    }

    /**
     * Check document with potential new member and send invitation if everything is correct.
     */
    private void checkNewMember(List<DocumentSnapshot> documents) {
        if (documents.isEmpty()) {
            InformUser.inform(getActivity(), R.string.user_not_found);
        } else {
            User user = documents.get(0).toObject(User.class);
            if (user != null)
                newMemberId = user.getId();
            Group currentGroup = ((MainActivity) activity).getCurrentGroup();
            if (currentGroup.getMembers().contains(newMemberId)) {
                // Is already a member
                InformUser.inform(getActivity(), R.string.user_is_a_member);
            } else {
                //Add
                firestoreRequests.addInvitation(newMemberId, currentGroup.getId(),
                        (aVoid) -> InformUser.inform(getActivity(), R.string.invitation_sent),
                        (e) -> InformUser.inform(getActivity(), R.string.invitation_incorrect));
            }
        }
    }

    /**
     * Check if task was successful and call checkMember if so.
     */
    private void checkInvitationTask(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            checkNewMember(task.getResult().getDocuments());
        } else {
            Exception exception = task.getException();
            if (exception != null) {
                InformUser.informFailure(getActivity(), exception);
            }
        }
    }

    /**
     * Alert gets from user email of potential new member, finds it in database and call functions to proceed.
     */
    private void alertNewMember() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Enter user email");

        // Set up the input
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setPadding(20, 20, 20, 20);
        input.setBackgroundColor(getResources().getColor(R.color.colorAccentLightSemi, getActivity().getTheme()));

        FrameLayout container = new FrameLayout(getActivity());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(30, 10, 30, 10);
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        // Set up the buttons
        builder.setPositiveButton("Send", (dialog, which) -> {
            newMemberEmail = input.getText().toString();
            firestoreRequests.getUserByField("email", newMemberEmail, this::checkInvitationTask);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Displays alert and removes user group group if submitted.
     */
    public void alertLeaveGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);

        builder.setTitle(R.string.leave_group)
                .setMessage(R.string.confirm_leave_group)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    Group currentGroup = ((MainActivity) activity).getCurrentGroup();
                    if (currentGroup.getMembers().size() > 1) {
                        //Remove only user
                        DataManager.getInstance().removeGroupMember(((MainActivity) activity).getCurrentUserId(),
                                currentGroup.getId(), activity);
                    } else {
                        //Remove whole group
                        DataManager.getInstance().removeGroup(currentGroup.getId(), activity);
                    }
                    DataManager.getInstance().refresh(((MainActivity)activity).getCurrentUserId());
                    activity.onBackPressed();
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void setEditing(Boolean edit) {
        isEdited = edit;

        if (edit) {
            groupNameInput.setVisibility(View.VISIBLE);
            groupDescriptionInput.setVisibility(View.VISIBLE);

            groupName.setVisibility(View.INVISIBLE);
            groupDescription.setVisibility(View.INVISIBLE);

            if(isCreated) {
                groupNameInput.setText("");
                groupDescriptionInput.setText("");
            }
            else {
                groupNameInput.setText(groupName.getText());
                groupDescriptionInput.setText(groupDescription.getText());
            }
        } else {
            groupNameInput.setVisibility(View.INVISIBLE);
            groupDescriptionInput.setVisibility(View.INVISIBLE);

            groupName.setVisibility(View.VISIBLE);
            groupDescription.setVisibility(View.VISIBLE);

            groupName.setText(groupNameInput.getText().toString());
            groupDescription.setText(groupDescriptionInput.getText().toString());
        }
    }

    private boolean update() {
        String name = groupNameInput.getText().toString();

        if (!name.equals("")) {
            group.setName(name);
            group.setDescription(groupDescriptionInput.getText().toString());

            if(isCreated){
                group.addMember(((MainActivity)activity).getCurrentUserId());
                firestoreRequests.addGroup(group,
                        (documentReference) -> {
                            InformUser.inform(activity, R.string.group_created);
                            ((MainActivity) activity).enableBack(false);
                            ((MainActivity) activity).startFragment(MainPageFragment.class);
                        },
                        (e) ->
                            InformUser.informFailure(activity, e)
                        );
            }else {
                firestoreRequests.updateGroup(group, group.getId(),
                        (x) -> {},
                        (e) -> InformUser.informFailure(activity, e)
                );
            }
            DataManager.getInstance().refresh(((MainActivity) getActivity()).getCurrentUserId());
            return true;
        } else {
            InformUser.inform(activity, R.string.name_required);
            return false;
        }
    }


}
