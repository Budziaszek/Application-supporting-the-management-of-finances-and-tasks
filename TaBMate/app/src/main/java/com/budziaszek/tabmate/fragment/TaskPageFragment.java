package com.budziaszek.tabmate.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.firestoreData.DataManager;
import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.firestoreData.UserTask;
import com.budziaszek.tabmate.view.InformUser;
import com.budziaszek.tabmate.view.adapter.TasksItemsAdapter;
import com.budziaszek.tabmate.view.listener.TasksClickListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TaskPageFragment extends BasicFragment {

    private static final String TAG = "TaskPageProcedure";

    private Activity activity;
    private TasksItemsAdapter tasksAdapter;
    private List<UserTask> tasks = new ArrayList<>();
    private UserTask.Status status;

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    public TaskPageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Created");
        View fView = inflater.inflate(R.layout.tasks_page, container, false);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            String key = bundle.getString("status");
            Log.d(TAG, key);
            if (key == null) {
                status = UserTask.Status.ARCHIVED;
            } else if (key.equals(UserTask.Status.TODO.toString())) {
                status = UserTask.Status.TODO;
            } else if (key.equals(UserTask.Status.DOING.toString())) {
                status = UserTask.Status.DOING;
            } else if (key.equals(UserTask.Status.DONE.toString())) {
                status = UserTask.Status.DONE;
            } else if (key.equals(UserTask.Status.ARCHIVED.toString())) {
                status = UserTask.Status.ARCHIVED;
            }
        }
        else{
            status = UserTask.Status.ARCHIVED;
        }
        if(getParentFragment()!= null)
            activity = getParentFragment().getActivity();
        else
            activity = getActivity();

        mDisplayView = fView.findViewById(R.id.show_tasks_layout);
        mProgressView = fView.findViewById(R.id.progress_tasks);

        //Refresh
        swipeLayout = fView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "Ask for refresh tasks");
            DataManager.getInstance().refreshAllGroupsTasks();
            swipeLayout.setRefreshing(false);

            ((BasicFragment)getParentFragment()).informAboutNetworkConnection();
        });
        swipeLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()),
                getResources().getColor(R.color.colorAccentDark, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()));

        // Tasks
        RecyclerView tasksRecycler = fView.findViewById(R.id.tasks_list);
        tasksAdapter = new TasksItemsAdapter(tasks, getContext(), status.color,
                new TasksClickListener() {
                    @Override
                    public void onClick(int position) {
                        ((MainActivity) activity).setCurrentTask(tasks.get(position));
                        ((MainActivity) activity).startFragment(TaskFragment.class);
                    }
                    @Override
                    public void onLongClick(int position){
                        //Move to next page
                        UserTask task = tasks.get(position);
                        if(task.getStatus() == UserTask.Status.ARCHIVED) {
                            return;
                        }
                        task.setNextStatus();


                        if(status == UserTask.Status.TODO) {
                            task.addDoer(((MainActivity) activity).getCurrentUserId());
                        }
                        firestoreRequests.updateTask(task,
                                (aVoid) -> {},
                                (e) -> Log.d(TAG, e.getMessage())
                        );

                        DataManager.getInstance().refreshAllGroupsTasks();
                        //TODO snackbar with undo
                        InformUser.inform(activity, R.string.task_moved);
                    }
                });

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        tasksRecycler.setLayoutManager(mLayoutManager);
        tasksRecycler.setItemAnimator(new DefaultItemAnimator());
        tasksRecycler.setAdapter(tasksAdapter);

        DataManager instance = DataManager.getInstance();
        instance.addObserver(this);
        if (instance.getFiltratedTasks() == null) {
            Log.d(TAG, "Ask for refresh tasks");
            instance.refreshAllGroupsTasks();
        } else {
            tasksChanged();
        }

        showProgress(false);

        return fView;
    }

    @Override
    public void tasksChanged() {
        //Select only one current page status
        List<UserTask> allTasks = DataManager.getInstance().getTasks();
        List<UserTask> newTasks = new ArrayList<>();
        for (UserTask task : allTasks) {
            if (task.getStatus().name.equals(status.name)) {
                    newTasks.add(task);
            }
        }
        List<UserTask> oldTasks = tasks;
        tasks = newTasks;
        tasksAdapter.update(tasks);

        newTasks.sort(Comparator.comparing(UserTask::getTitle));
        //TODO check what exactly changed
        for(int i = 0; i < newTasks.size(); i++){
            if(oldTasks.size() <= i) {
                tasksAdapter.notifyItemInserted(i);
            } else {
                UserTask newTask = newTasks.get(i);
                UserTask oldTask = oldTasks.get(i);
                if (!oldTask.equals(newTask)) {
                    tasksAdapter.notifyItemChanged(i);
                }
            }
        }
        for( int i = newTasks.size(); i< oldTasks.size(); i++){
            tasksAdapter.notifyItemRemoved(i);
        }
    }

}
