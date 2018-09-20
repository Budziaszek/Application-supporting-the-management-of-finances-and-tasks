package com.budziaszek.tabmate.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.firestoreData.UserTask;
import com.budziaszek.tabmate.view.InformUser;
import com.budziaszek.tabmate.view.Manager;

import java.util.ArrayList;
import java.util.List;

public class AddTaskFragment extends BasicFragment {

    private static final String TAG = "AddGroupProcedure";

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    private EditText taskNameEdit;
    private EditText taskDescriptionEdit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fView = inflater.inflate(R.layout.fragment_add_task, container, false);

        mDisplayView = fView.findViewById(R.id.add_task_layout);
        mProgressView = fView.findViewById(R.id.progress_add_task);

        taskNameEdit = fView.findViewById(R.id.edit_task_name);
        taskDescriptionEdit = fView.findViewById(R.id.edit_task_description);

        Spinner spinner = (Spinner) fView.findViewById(R.id.spinner_group);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, android.R.id.text1);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        List<Group> groups = ((MainActivity)getActivity()).getGroups();
        for(Group g :groups){
            adapter.add(g.getName());
        }
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        Button submitTaskButton = fView.findViewById(R.id.submit_create_task);
        submitTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = taskNameEdit.getText().toString();
                String description = taskDescriptionEdit.getText().toString();
                String group = spinner.getSelectedItem().toString();

                Manager.hideKeyboard(getActivity());
                addNewTask(name, description, group);
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
                    ((MainActivity)getActivity()).startFragment(DisplayTasksFragment.class);
                },
                (e) -> {
                    showProgress(false);
                    InformUser.informFailure(getActivity(), e);
                });
    }
}