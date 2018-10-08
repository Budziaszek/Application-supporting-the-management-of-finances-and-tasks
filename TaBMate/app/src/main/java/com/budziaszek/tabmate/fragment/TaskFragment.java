package com.budziaszek.tabmate.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.SeekBar;
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

public class TaskFragment extends BasicFragment implements DatePickerDialog.OnDateSetListener,
        NumberPicker.OnValueChangeListener {

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
    private UserTask.Status newStatus;
    private TextView taskDeadline;
    private TextView taskGroup;
    private Spinner taskGroupInput;
    private TextView taskEstimatedTime;
    private TextView taskTimeVote;
    private SeekBar seekBar;

    private MembersItemsAdapter doersAdapter;
    private List<User> doers = new ArrayList<>();

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Created");
        fView = inflater.inflate(R.layout.task, container, false);

        activity = getActivity();
        task = ((MainActivity) activity).getCurrentTask();

        taskTitle = fView.findViewById(R.id.task_title);
        taskDescription = fView.findViewById(R.id.nick);
        taskTitleInput = fView.findViewById(R.id.task_title_input);
        taskDescriptionInput = fView.findViewById(R.id.task_description_input);
        taskStatus = fView.findViewById(R.id.task_status);
        taskDeadline = fView.findViewById(R.id.task_deadline);
        taskGroup = fView.findViewById(R.id.task_group);
        taskGroupInput = fView.findViewById(R.id.spinner_group);
        taskEstimatedTime = fView.findViewById(R.id.task_estimated_time);
        taskTimeVote = fView.findViewById(R.id.task_time_vote);
        seekBar = fView.findViewById(R.id.readiness_seek_bar);

        seekBar.setOnTouchListener((v, event) -> !isEdited);

        // Add task
        if (task == null) {
            newStatus = UserTask.Status.TODO;
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
            task = new UserTask("", "", "", "", new ArrayList<>(), UserTask.Status.TODO);
            isCreated = true;
            setEditing(true);
        } else {
            newStatus = task.getStatus();
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
        MenuItem remove = menu.findItem(R.id.action_remove);

        if (isEdited) {
            edit.setVisible(false);
            save.setVisible(true);
            remove.setVisible(false);
        } else {
            edit.setVisible(true);
            save.setVisible(false);
            remove.setVisible(true);
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
            if (update()) {
                fView.findViewById(R.id.doers_layout).setVisibility(View.VISIBLE);
                setEditing(false);
                activity.invalidateOptionsMenu();
                KeyboardManager.hideKeyboard(activity);
            }
            return true;
        } else if (id == R.id.action_remove) {
            alertRemoveTask();
            return true;
        }
        return false;
    }

    /**
     * Displays current task data.
     */
    private void showTask() {
        taskTitle.setText(task.getTitle());
        taskDescription.setText(task.getDescription());
        taskTitleInput.setText(task.getTitle());
        taskDescriptionInput.setText(task.getDescription());
        taskDeadline.setText(task.getDateString());
        taskEstimatedTime.setText(String.valueOf(task.getEstimatedTime()));
        Integer timeVote = task.getTimeEstimationVote().get(((MainActivity)activity).getCurrentUserId());
        Integer readinessVote = task.getReadinessVote().get(((MainActivity)activity).getCurrentUserId());
        if(timeVote != null)
            taskTimeVote.setText(String.valueOf(timeVote));
        else
            taskTimeVote.setText(R.string.vote);
        if(readinessVote != null)
            seekBar.setProgress(readinessVote);
        else
            seekBar.setProgress(2);

        TextView taskGroup = fView.findViewById(R.id.task_group);
        Group group = DataManager.getInstance().getGroup(task.getGroup());
        if (group != null)
            taskGroup.setText(group.getName());

        //Status
        TextView status = fView.findViewById(R.id.task_status);
        status.setText(task.getStatus().name);
        if(task.getStatus() == UserTask.Status.ARCHIVED && task.getStatusBeforeArchive() != null)
            status.append(" (" + task.getStatusBeforeArchive()+ ")");

        Map<String, User> allUsers = DataManager.getInstance().getUsersInMap();
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
                seekBar.setVisibility(View.GONE);
                fView.findViewById(R.id.label_readiness_vote).setVisibility(View.INVISIBLE);
                taskEstimatedTime.setVisibility(View.GONE);
                taskTimeVote.setVisibility(View.GONE);
                fView.findViewById(R.id.label_task_time_vote).setVisibility(View.GONE);
                fView.findViewById(R.id.label_task_estimated_time).setVisibility(View.GONE);
            } else {
                taskGroupInput.setVisibility(View.INVISIBLE);
                taskTitleInput.setText(taskTitle.getText());
                taskDescriptionInput.setText(taskDescription.getText());
            }

            taskStatus.setOnClickListener(view -> {
                newStatus = UserTask.getNextStatus(newStatus);
                taskStatus.setText(newStatus.name);
                taskStatus.setBackground(getResources().getDrawable(newStatus.color, activity.getTheme()));
            });
            taskStatus.setBackground(getResources().getDrawable(task.getStatus().color, activity.getTheme()));

            taskDeadline.setOnClickListener(view -> {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                DatePickerDialog picker = new DatePickerDialog(getContext(), TaskFragment.this, year, month, day);
                picker.show();
            });
            taskDeadline.setBackgroundColor(getResources().getColor(R.color.colorAccentLight, activity.getTheme()));

            taskTimeVote.setOnClickListener(view -> {
                Dialog dialog = new Dialog(activity);
                dialog.setContentView(R.layout.dialog);
                Button buttonSet = dialog.findViewById(R.id.set);
                Button buttonCancel = dialog.findViewById(R.id.cancel);
                NumberPicker numberPicker = dialog.findViewById(R.id.numberPicker);

                numberPicker.setMaxValue(100);
                numberPicker.setMinValue(0);
                Integer value = task.getTimeEstimationVote().get(((MainActivity)getActivity()).getCurrentUserId());
                if(value != null)
                    numberPicker.setValue(value);

                numberPicker.setWrapSelectorWheel(false);
                numberPicker.setOnValueChangedListener(this);

                buttonSet.setOnClickListener(v -> {
                    taskTimeVote.setText(String.valueOf(numberPicker.getValue()));
                    dialog.dismiss();
                });
                buttonCancel.setOnClickListener(v -> dialog.dismiss());
                dialog.show();
            });
            taskTimeVote.setBackgroundColor(getResources().getColor(R.color.colorAccentLight, activity.getTheme()));

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

            taskTimeVote.setOnClickListener(view -> {
            });
            taskTimeVote.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private boolean update() {
        String title = taskTitleInput.getText().toString();

        if (!title.equals("")) {
            //Set data
            task.setTitle(title);
            task.setDescription(taskDescriptionInput.getText().toString());

            if(task.getStatus() != UserTask.Status.ARCHIVED && newStatus == UserTask.Status.ARCHIVED)
                task.setArchived();
            else
                task.setStatus(newStatus);

            if(!taskTimeVote.getText().toString().equals(getResources().getString(R.string.vote)))
                task.addTimeEstimationVote(((MainActivity)getActivity()).getCurrentUserId(),
                        Integer.parseInt(taskTimeVote.getText().toString()));
            taskEstimatedTime.setText(String.valueOf(task.getEstimatedTime()));
            task.addReadinessVote(((MainActivity)getActivity()).getCurrentUserId(), seekBar.getProgress());

            //Save data
            if (isCreated) {
                task.setGroup(((Group) taskGroupInput.getSelectedItem()).getId());
                firestoreRequests.addTask(task,
                        (x) -> InformUser.inform(activity, R.string.task_created),
                        (e) -> InformUser.informFailure(activity, e));
                ((MainActivity) activity).startFragment(TasksPagerFragment.class);
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

    /**
     * Displays alert and removes task if submitted.
     */
    public void alertRemoveTask() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);

        builder.setTitle(R.string.remove_task)
                .setMessage(R.string.confirm_remove_task)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    DataManager.getInstance().removeTask(task, activity);
                    DataManager.getInstance().refreshAllGroupsTasks();
                    activity.onBackPressed();
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {

    }
}
