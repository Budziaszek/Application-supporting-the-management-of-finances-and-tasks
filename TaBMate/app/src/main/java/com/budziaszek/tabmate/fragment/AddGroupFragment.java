package com.budziaszek.tabmate.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.view.InformUser;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.view.KeyboardManager;

public class AddGroupFragment extends BasicFragment {

    private static final String TAG = "GroupProcedure";

    private Activity activity;

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    private boolean edit = false;
    private EditText groupNameEdit;
    private EditText groupDescriptionEdit;
    private Group oldGroup = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Created");
        View fView = inflater.inflate(R.layout.group_add, container, false);

        activity = getActivity();

        mDisplayView = fView.findViewById(R.id.add_group_layout);
        mProgressView = fView.findViewById(R.id.progress_add_group);

        groupNameEdit = fView.findViewById(R.id.edit_group_name);
        groupDescriptionEdit = fView.findViewById(R.id.edit_group_description);

        if (edit) {
            TextView textView = fView.findViewById(R.id.create_title);
            textView.setText(R.string.edit);
            Group group = ((MainActivity) activity).getCurrentGroup();
            oldGroup = group;

            groupNameEdit.setText(group.getName());
            groupDescriptionEdit.setText(group.getDescription());

            Button submitGroupButton = fView.findViewById(R.id.submit_create_group);
            submitGroupButton.setOnClickListener(view -> {
                String name = groupNameEdit.getText().toString();
                String description = groupDescriptionEdit.getText().toString();
                KeyboardManager.hideKeyboard(getActivity());
                editGroup(name, description);
            });
        } else {
            Button submitGroupButton = fView.findViewById(R.id.submit_create_group);
            submitGroupButton.setOnClickListener(view -> {
                String name = groupNameEdit.getText().toString();
                if (!name.equals("")) {
                    String description = groupDescriptionEdit.getText().toString();
                    String id = ((MainActivity) getActivity()).getCurrentUserId();
                    KeyboardManager.hideKeyboard(getActivity());
                    addNewGroup(id, name, description);
                } else {
                    InformUser.inform(activity, R.string.name_required);
                }
            });
        }
        return fView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    /**
     * Attempts to create new group and save it to Firestore.
     */
    private void addNewGroup(String id, String name, String description) {
        showProgress(true);
        Group newGroup = new Group(name, description);
        newGroup.addMember(id);

        firestoreRequests.addGroup(newGroup,
                (documentReference) -> {
                    showProgress(false);
                    InformUser.inform(activity, R.string.group_created);
                    ((MainActivity) activity).enableBack(false);
                    ((MainActivity) activity).startFragment(MainPageFragment.class);
                },
                (e) -> {
                    showProgress(false);
                    InformUser.informFailure(activity, e);
                });
    }

    /**
     * Attempts to edit existing group and save it to Firestore.
     */
    private void editGroup(String name, String description) {
        showProgress(true);
        Group newGroup = new Group(name, description);
        newGroup.setMembers(oldGroup.getMembers());

        firestoreRequests.updateGroup(newGroup, oldGroup.getId(),
                (Void) -> {
                    showProgress(false);
                    InformUser.inform(activity, R.string.saved);
                    ((MainActivity) activity).enableBack(false);
                    ((MainActivity) activity).startFragment(MainPageFragment.class);
                },
                (e) -> {
                    showProgress(false);
                    InformUser.informFailure(getActivity(), e);
                });
    }

    public void setEdit() {
        edit = true;
    }
}
