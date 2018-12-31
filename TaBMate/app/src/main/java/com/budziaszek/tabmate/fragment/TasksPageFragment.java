package com.budziaszek.tabmate.fragment;

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
import com.budziaszek.tabmate.data.DataManager;
import com.budziaszek.tabmate.data.FirestoreRequests;
import com.budziaszek.tabmate.data.Task;
import com.budziaszek.tabmate.view.helper.InformUser;
import com.budziaszek.tabmate.view.adapter.TasksItemsAdapter;
import com.budziaszek.tabmate.view.listener.TaskClickListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TasksPageFragment extends BasicFragment {

    private static final String TAG = "TaskPageFragmentProcedure";

    //private Activity activity;
    private TasksItemsAdapter tasksAdapter;
    private List<Task> tasks = new ArrayList<>();
    private Task.Status status;

    public TasksPageFragment() {
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
                status = Task.Status.ARCHIVED;
            } else if (key.equals(Task.Status.TODO.toString())) {
                status = Task.Status.TODO;
            } else if (key.equals(Task.Status.DOING.toString())) {
                status = Task.Status.DOING;
            } else if (key.equals(Task.Status.DONE.toString())) {
                status = Task.Status.DONE;
            } else if (key.equals(Task.Status.ARCHIVED.toString())) {
                status = Task.Status.ARCHIVED;
            }
        }
        else{
            status = Task.Status.ARCHIVED;
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
            if(!DataManager.getInstance().isRefreshFinished())
                return;

            Log.d(TAG, "Ask for refresh tasks");
            DataManager.getInstance().refresh(((MainActivity) activity).getCurrentUserId());

            ((BasicFragment)getParentFragment()).informAboutNetworkConnection();
            ((BasicFragment)getParentFragment()).informAboutDataSynchronization();
        });
        swipeLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()),
                getResources().getColor(R.color.colorAccentDark, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()));

        // Tasks
        RecyclerView tasksRecycler = fView.findViewById(R.id.tasks_list);
        tasksAdapter = new TasksItemsAdapter(tasks, getContext(), status.color,
                new TaskClickListener() {
                    @Override
                    public void onClick(int position) {
                        ((MainActivity) activity).setCurrentTask(tasks.get(position));
                        ((MainActivity) activity).startFragment(TaskFragment.class);
                    }
                    @Override
                    public void onLongClick(int position){
                        //Move to next page
                        Task task = tasks.get(position);
                        if(task.getStatus() == Task.Status.ARCHIVED) {
                            return;
                        }
                        task.setNextStatus();
                        //if(status == Task.Status.TO DO) {
                        //    task.addDoer(((MainActivity) activity).getCurrentUserId());
                        //}(

                        FirestoreRequests.updateTask(task,
                                (aVoid) -> {},
                                (e) -> Log.d(TAG, e.getMessage())
                        );

                        DataManager.getInstance().refresh(((MainActivity)activity).getCurrentUserId());
                        //TODO snackbar with undo
                        InformUser.inform(activity, R.string.task_moved);
                    }
                }, ((MainActivity)activity).getCurrentUserId());

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        tasksRecycler.setLayoutManager(mLayoutManager);
        tasksRecycler.setItemAnimator(new DefaultItemAnimator());
        tasksRecycler.setAdapter(tasksAdapter);

        DataManager instance = DataManager.getInstance();
        instance.addObserver(this);
        if (instance.getFiltratedTasks() == null) {
            Log.d(TAG, "Ask for refresh tasks");
            instance.refresh(((MainActivity)activity).getCurrentUserId());
        } else {
            tasksChanged();
        }

        showProgress(false);

        return fView;
    }

    @Override
    public void tasksChanged() {
        List<Task> allTasks = DataManager.getInstance().getFiltratedTasks();
        List<Task> newTasks = new ArrayList<>();
        List<Task> oldTasks = tasks;

        for (Task task : allTasks) {
            if (task.getStatus().name.equals(status.name)) {
                newTasks.add(task);
            }
        }
        newTasks.sort(Comparator.comparing(Task::getTitle));
        tasks = newTasks;
        tasksAdapter.update(tasks);

        //TODO check what exactly changed
        for(int i = 0; i < newTasks.size(); i++){
            if(oldTasks.size() <= i) {
                tasksAdapter.notifyItemInserted(i);
            } else {
                Task newTask = newTasks.get(i);
                Task oldTask = oldTasks.get(i);
                if (!oldTask.equals(newTask)) {
                    tasksAdapter.notifyItemChanged(i);
                }
            }
        }
        for( int i = newTasks.size(); i< oldTasks.size(); i++){
            tasksAdapter.notifyItemRemoved(i);
        }
    }

    @Override
    public void informAboutDataSynchronization() {
        ((BasicFragment)getParentFragment()).informAboutDataSynchronization();
    }


}
