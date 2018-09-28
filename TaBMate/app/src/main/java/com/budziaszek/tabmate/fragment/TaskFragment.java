package com.budziaszek.tabmate.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.app.DatePickerDialog;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.firestoreData.DataManager;
import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.firestoreData.User;
import com.budziaszek.tabmate.firestoreData.UserTask;
import com.budziaszek.tabmate.view.InformUser;
import com.budziaszek.tabmate.view.KeyboardManager;
import com.budziaszek.tabmate.view.adapter.GroupSpinnerAdapter;
import com.budziaszek.tabmate.view.adapter.MembersItemsAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

public class TaskFragment extends BasicFragment implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "DisplayTaskProcedure";
    private Activity activity;

    private View fView;

    private UserTask task;

    private Button joinTask;

    private Boolean isEdited;
    private Boolean isCreated;

    private TextView taskTitle;
    private TextView taskDescription;
    private TextView taskTitleInput;
    private TextView taskDescriptionInput;
    private TextView taskStatus;
    private TextView taskDeadline;
    private TextView taskGroup;
    private Spinner taskGroupInput;

    private MembersItemsAdapter doersAdapter;
    private List<User> doers = new ArrayList<>();

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Created");
        fView = inflater.inflate(R.layout.task, container, false);

        activity = getActivity();
        task = ((MainActivity) activity).getCurrentTask();

        taskTitle = fView.findViewById(R.id.task_title);
        taskDescription = fView.findViewById(R.id.task_description);
        taskTitleInput = fView.findViewById(R.id.task_title_input);
        taskDescriptionInput = fView.findViewById(R.id.task_description_input);
        taskStatus = fView.findViewById(R.id.task_status);
        taskDeadline = fView.findViewById(R.id.task_deadline);
        taskGroup = fView.findViewById(R.id.task_group);
        taskGroupInput = fView.findViewById(R.id.spinner_group);

        // Add task
        if (task == null) {
            List<Group> groupsList = DataManager.getInstance().getGroups();
            Group groups[] = new Group[groupsList.size()];
            groups = groupsList.toArray(groups);

            GroupSpinnerAdapter adapter = new GroupSpinnerAdapter(getActivity(),
                    android.R.layout.simple_spinner_item, groups);
            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

            taskGroupInput.setAdapter(adapter);

            fView.findViewById(R.id.doers_layout).setVisibility(View.INVISIBLE);
            TextView title = fView.findViewById(R.id.details_title);
            title.setText(R.string.add_new_task);
            task = new UserTask("", "", "", "", new ArrayList<>(), new ArrayList<>(), UserTask.Status.TODO);
            isCreated = true;
            setEditing(true);
        } else {
            isCreated = false;
            setEditing(false);
        }

        // Doers
        RecyclerView membersRecycler = fView.findViewById(R.id.doers_list);
        doersAdapter = new MembersItemsAdapter(doers, position -> {
            UserTask task = ((MainActivity) activity).getCurrentTask();
            String userId = ((MainActivity) getActivity()).getCurrentUserId();
            task.removeDoer(userId);
            firestoreRequests.updateTask(task,
                    (aVoid) -> {
                        DataManager.getInstance().refreshAllGroupsTasks();
                        activity.onBackPressed();
                    },
                    (e) -> InformUser.informFailure(activity, e)
            );
        }, ((MainActivity) activity).getCurrentUserId());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        membersRecycler.setLayoutManager(mLayoutManager);
        membersRecycler.setItemAnimator(new DefaultItemAnimator());
        membersRecycler.setAdapter(doersAdapter);

        joinTask = fView.findViewById(R.id.join_task_button);
        joinTask.setOnClickListener(view -> {
            UserTask task = ((MainActivity) activity).getCurrentTask();
            task.addDoer(((MainActivity) getActivity()).getCurrentUserId());
            firestoreRequests.updateTask(task,
                    (aVoid) -> {
                        DataManager.getInstance().refreshAllGroupsTasks();
                        activity.onBackPressed();
                    },
                    (e) -> InformUser.informFailure(activity, e)
            );
        });

        showTask();
        ((MainActivity) activity).enableBack(true);
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
            fView.findViewById(R.id.doers_layout).setVisibility(View.INVISIBLE);
            setEditing(true);
            activity.invalidateOptionsMenu();
            return true;
        } else if (id == R.id.action_save) {
            fView.findViewById(R.id.doers_layout).setVisibility(View.VISIBLE);
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
     * Displays current task data.
     */
    private void showTask() {
        // Details

        taskTitle.setText(task.getTitle());
        taskDescription.setText(task.getDescription());
        taskTitleInput.setText(task.getTitle());
        taskDescriptionInput.setText(task.getDescription());
        taskDeadline.setText(task.getDateString());

        TextView taskGroup = fView.findViewById(R.id.task_group);
        Group group = DataManager.getInstance().getGroup(task.getGroup());
        if (group != null)
            taskGroup.setText(group.getName());

        //Status
        TextView status = fView.findViewById(R.id.task_status);
        status.setText(task.getStatus().name);

        Map<String, User> allUsers = DataManager.getInstance().getUsers();
        doers = new ArrayList<>();
        List<String> doersIds = task.getDoers();

        if (doersIds.contains(((MainActivity) activity).getCurrentUserId())) {
            joinTask.setVisibility(View.GONE);
        }

        for (String doer : doersIds) {
            if (allUsers.containsKey(doer)) {
                doers.add(allUsers.get(doer));
            }
        }

        doersAdapter.update(doers);
    }

    private void setEditing(Boolean edit) {
        isEdited = edit;

        if (edit) {
            taskTitleInput.setVisibility(View.VISIBLE);
            taskDescriptionInput.setVisibility(View.VISIBLE);

            taskTitle.setVisibility(View.INVISIBLE);
            taskDescription.setVisibility(View.INVISIBLE);

            if (isCreated) {
                taskGroupInput.setVisibility(View.VISIBLE);
                taskGroup.setVisibility(View.INVISIBLE);
                taskTitleInput.setText("");
                taskDescriptionInput.setText("");
            } else {
                taskGroupInput.setVisibility(View.INVISIBLE);
                taskTitleInput.setText(taskTitle.getText());
                taskDescriptionInput.setText(taskDescription.getText());
            }

            taskStatus.setOnClickListener(view -> {
                task.setStatus(UserTask.getNextStatus(task.getStatus()));
                taskStatus.setText(task.getStatus().name);
            });
            taskStatus.setBackgroundColor(getResources().getColor(R.color.colorAccentLight, activity.getTheme()));

            taskDeadline.setOnClickListener(view -> {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                DatePickerDialog picker = new DatePickerDialog(getContext(), TaskFragment.this, year, month, day);
                picker.show();
            });
            taskDeadline.setBackgroundColor(getResources().getColor(R.color.colorAccentLight, activity.getTheme()));
        } else {
            taskTitleInput.setVisibility(View.INVISIBLE);
            taskDescriptionInput.setVisibility(View.INVISIBLE);
            taskGroupInput.setVisibility(View.INVISIBLE);

            taskTitle.setVisibility(View.VISIBLE);
            taskTitle.setText(taskTitleInput.getText().toString());

            taskDescription.setVisibility(View.VISIBLE);
            taskDescription.setText(taskDescriptionInput.getText().toString());

            taskDeadline.setOnClickListener(view -> {
            });
            taskDeadline.setBackgroundColor(Color.TRANSPARENT);

            taskStatus.setOnClickListener(view -> {
            });
            taskStatus.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private boolean update() {
        String title = taskTitleInput.getText().toString();

        if (!title.equals("")) {
            task.setTitle(title);
            task.setDescription(taskDescriptionInput.getText().toString());
            if (isCreated) {
                task.setGroup(((Group) taskGroupInput.getSelectedItem()).getId());
                firestoreRequests.addTask(task,
                        (x) -> InformUser.inform(activity, R.string.task_created),
                        (e) -> InformUser.informFailure(activity, e));
                ((MainActivity) activity).startFragment(PagerTasksFragment.class);
            } else {
                firestoreRequests.updateTask(task,
                        (x) -> {
                        },
                        (e) -> InformUser.informFailure(activity, e)
                );
            }
            DataManager.getInstance().refreshAllGroupsTasks();
            return true;
        } else {
            InformUser.inform(activity, R.string.name_required);
            return false;
        }

    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);
        task.setDate(calendar.getTime());
        taskDeadline.setText(task.getDateString());
        DataManager.getInstance().refreshAllGroupsTasks();
    }
}
