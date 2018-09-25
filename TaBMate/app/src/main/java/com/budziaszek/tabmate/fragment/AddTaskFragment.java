package com.budziaszek.tabmate.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.firestoreData.DataManager;
import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.firestoreData.UserTask;
import com.budziaszek.tabmate.view.InformUser;
import com.budziaszek.tabmate.view.KeyboardManager;
import com.budziaszek.tabmate.view.adapter.GroupSpinnerAdapter;

import java.util.List;

public class AddTaskFragment extends BasicFragment {

    private static final String TAG = "AddGroupProcedure";

    private Activity activity;

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    private EditText taskNameEdit;
    private EditText taskDescriptionEdit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fView = inflater.inflate(R.layout.fragment_add_task, container, false);

        activity = getActivity();

        mDisplayView = fView.findViewById(R.id.add_task_layout);
        mProgressView = fView.findViewById(R.id.progress_add_task);

        taskNameEdit = fView.findViewById(R.id.edit_task_name);
        taskDescriptionEdit = fView.findViewById(R.id.edit_task_description);

        // Groups spinner
        Spinner spinner = (Spinner) fView.findViewById(R.id.spinner_group);

        List<Group> groupsList = DataManager.getInstance().getGroups();
        Group groups[] = new Group[groupsList.size()];
        groups = groupsList.toArray(groups);

        GroupSpinnerAdapter adapter = new GroupSpinnerAdapter(getActivity(),
                android.R.layout.simple_spinner_item, groups);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        Button submitTaskButton = fView.findViewById(R.id.submit_create_task);
        submitTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = taskNameEdit.getText().toString();
                if(!name.equals("")) {
                    String description = taskDescriptionEdit.getText().toString();
                    String group = ((Group) spinner.getSelectedItem()).getId();

                    KeyboardManager.hideKeyboard(getActivity());
                    addNewTask(name, description, group);
                }
                else{
                    InformUser.inform(activity, R.string.name_required);
                }
            }
        });
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
    private void addNewTask(String name, String description, String group) {
        showProgress(true);
        UserTask newTask = new UserTask(name, description, group);

        firestoreRequests.addTask(newTask,
                (documentReference) ->  {
                    showProgress(false);
                    InformUser.inform(getActivity(), R.string.task_created);
                    DataManager.getInstance().refreshAllGroupsTasks();
                    ((MainActivity)activity).enableBack(false);
                    ((MainActivity)activity).startFragment(DisplayTasksFragment.class);
                },
                (e) -> {
                    showProgress(false);
                    InformUser.informFailure(getActivity(), e);
                });
    }
}