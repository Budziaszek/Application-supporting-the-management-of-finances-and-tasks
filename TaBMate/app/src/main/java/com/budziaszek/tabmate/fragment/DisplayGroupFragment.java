package com.budziaszek.tabmate.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.budziaszek.tabmate.firestoreData.DataManager;
import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.view.InformUser;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.firestoreData.User;
import com.budziaszek.tabmate.view.listener.MemberClickListener;
import com.budziaszek.tabmate.view.adapter.MembersItemsAdapter;
import com.budziaszek.tabmate.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//TODO administrator
public class DisplayGroupFragment extends BasicFragment {

    private static final String TAG = "DisplayGroupProcedure";
    private Activity activity;

    private View fView;

    private MembersItemsAdapter membersAdapter;
    private List<User> users = new ArrayList<>();

    private String newMemberEmail = null;
    private String newMemberId = null;

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fView = inflater.inflate(R.layout.fragment_display_group, container, false);

        activity = getActivity();

        mDisplayView = fView.findViewById(R.id.show_groups_layout);
        mProgressView = fView.findViewById(R.id.progress_display);

        // Members
        RecyclerView membersRecycler = fView.findViewById(R.id.tasks_list);
        membersAdapter = new MembersItemsAdapter(users, new MemberClickListener() {
            @Override
            public void onLeaveClicked(int position) {
                //((MainActivity)activity).alertLeaveGroup();
                alertLeaveGroup();
                DataManager.getInstance().refreshGroupsAndUsers(((MainActivity)activity).getCurrentUserId());
            }
        }, ((MainActivity) activity).getCurrentUserId());
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
        getActivity().getMenuInflater().inflate(R.menu.menu_groups, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit_group) {
            ((MainActivity) activity).startEditFragment();
            return true;
        }
        return false;
    }

    /**
     * Displays current group data.
     */
    private void showGroup() {
        Group group = ((MainActivity) activity).getCurrentGroup();

        TextView groupName = fView.findViewById(R.id.group_name);
        TextView groupDescription = fView.findViewById(R.id.group_description);

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

    /**
     * Displays alert and removes user group group if submitted.
     */
    public void alertLeaveGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);

        builder.setTitle(R.string.leave_group)
                .setMessage(R.string.confirm_leave_group)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Group currentGroup = ((MainActivity) activity).getCurrentGroup();
                        if (currentGroup.getMembers().size() > 1) {
                            //Remove only user
                            DataManager.getInstance().removeGroupMember(((MainActivity) activity).getCurrentUserId(),
                                    currentGroup.getId(), activity);
                        } else {
                            //Remove whole group
                            DataManager.getInstance().removeGroup(currentGroup.getId(), activity);
                        }
                        ((MainActivity) activity).onBackPressed();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
