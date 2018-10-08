package com.budziaszek.tabmate.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.firestoreData.DataManager;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.firestoreData.User;

import java.util.List;

public class FindTasksFragment extends BasicFragment {

    private static final String TAG = "FindTasksProcedure";

    // private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Created");
        View fView = inflater.inflate(R.layout.tasks_find, container, false);

        Activity activity = getActivity();

        // Allow groups selection
        LinearLayout layout = fView.findViewById(R.id.groups_checkboxes);
        List<Group> groups = DataManager.getInstance().getGroups();
        for(Group group: groups){
            CheckBox cb = new CheckBox(activity);
            cb.setText(group.getName());
            cb.setChecked(DataManager.getInstance().getSelectedGroupsIds().contains(group.getId()));
            cb.setPadding(5, 5, 5, 5);
            cb.setTextSize(18);
            layout.addView(cb);
            cb.setOnClickListener(v -> {
                CheckBox checkBox = (CheckBox) v;
                Log.d(TAG, group.getName() + " " + checkBox.getText());
                if(checkBox.getText().equals(group.getName())) {
                    boolean checked = checkBox.isChecked();
                    if (checked) {
                        DataManager.getInstance().addFiltrationOptionGroup(group.getId());
                    } else {
                        DataManager.getInstance().removeFiltrationOptionGroup(group.getId());
                    }
                }
            });
        }

        // Allow users selection
        LinearLayout layoutUsers = fView.findViewById(R.id.users_checkboxes);
        List<User> users = DataManager.getInstance().getUsers();
        for(User user: users){
            CheckBox cb = new CheckBox(activity);
            cb.setText(user.getName());
            cb.setChecked(DataManager.getInstance().getSelectedUsersIds().contains(user.getId()));
            cb.setPadding(5, 5, 5, 5);
            cb.setTextSize(18);
            layoutUsers.addView(cb);
            cb.setOnClickListener(v -> {
                CheckBox checkBox = (CheckBox) v;
                Log.d(TAG, user.getName() + " " + checkBox.getText());
                if(checkBox.getText().equals(user.getName())) {
                    boolean checked = checkBox.isChecked();
                    if (checked) {
                        DataManager.getInstance().addFiltrationOptionUser(user.getId());
                    } else {
                        DataManager.getInstance().removeFiltrationOptionUser(user.getId());
                    }
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

}
