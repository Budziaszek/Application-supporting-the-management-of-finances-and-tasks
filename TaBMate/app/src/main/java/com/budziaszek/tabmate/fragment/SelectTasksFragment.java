package com.budziaszek.tabmate.fragment;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.firestoreData.DataManager;
import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.firestoreData.User;
import com.budziaszek.tabmate.firestoreData.UserTask;
import com.budziaszek.tabmate.view.InformUser;
import com.budziaszek.tabmate.view.KeyboardManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SelectTasksFragment extends BasicFragment {

    private static final String TAG = "SelectTasksFragmentProcedure";
    private List<String> selectedGroupsIds = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Created");
        View fView = inflater.inflate(R.layout.tasks_select, container, false);

        Activity activity = getActivity();

        // Allow groups selection
        LinearLayout layout = fView.findViewById(R.id.groups_checkboxes);
        List<Group> groups = DataManager.getInstance().getGroups();
        for(Group group: groups){
            selectedGroupsIds.add(group.getId());
            CheckBox cb = new CheckBox(activity);
            cb.setText(group.getName());
            cb.setChecked(DataManager.getInstance().getSelectedGroupsIds().contains(group.getId()));
            cb.setPadding(5, 5, 5, 5);
            cb.setTextSize(18);
            layout.addView(cb);
            cb.setOnClickListener(v -> {
                CheckBox checkBox = (CheckBox) v;
                if(checkBox.getText().equals(group.getName())) {
                    boolean checked = checkBox.isChecked();
                    if (checked)
                        selectedGroupsIds.add(group.getId());
                    else
                        selectedGroupsIds.remove(group.getId());
                }
            });
        }

        Button assignButton = fView.findViewById(R.id.assign_tasks_button);
        assignButton.setOnClickListener(view -> {
            KeyboardManager.hideKeyboard(activity);

            Integer number_of_tasks_selected = 0;
            Integer number_of_tasks;
            EditText tasks_input = fView.findViewById(R.id.tasks_input);
            try {
                number_of_tasks = Integer.parseInt(tasks_input.getText().toString());
            } catch (Exception e){
                Log.d("ProcedureError", "error");
                tasks_input.setError(getString(R.string.error_field_required));
                return;
            }

            List<UserTask> tasks = DataManager.getInstance().getTasks();
            List<UserTask> tasksToBeAssigned = new ArrayList<>();

            tasks.sort(Comparator.comparing(UserTask::getDateForSort).reversed());
            for(UserTask task:tasks){
                if(task.getStatus() == UserTask.Status.TODO && selectedGroupsIds.contains(task.getGroup())){
                    if(selectedGroupsIds.contains(task.getGroup()) && task.getDoers().size() == 0)
                        if(number_of_tasks_selected < number_of_tasks){
                            tasksToBeAssigned.add(task);
                            number_of_tasks_selected++;
                        }
                }
            }

            if(tasksToBeAssigned.size() == 0){
                InformUser.inform(activity, R.string.tasks_not_found);
                return;
            }

            FirestoreRequests firestoreRequests = new FirestoreRequests();
            for(UserTask task:tasksToBeAssigned) {
                Log.d(TAG, "Assigned " + task.getTitle());
                task.addDoer(((MainActivity)getActivity()).getCurrentUserId());
                firestoreRequests.updateTask(task, v->{}, e->{});
            }
            DataManager.getInstance().refresh(((MainActivity)getActivity()).getCurrentUserId());
            activity.onBackPressed();
        });

        return fView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

}
