package com.budziaszek.tabmate.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.firestoreData.DataManager;
import com.budziaszek.tabmate.firestoreData.UserTask;
import com.budziaszek.tabmate.view.SwipeController;
import com.budziaszek.tabmate.view.adapter.TasksItemsAdapter;
import com.budziaszek.tabmate.view.listener.TasksClickListener;

import java.util.ArrayList;
import java.util.List;

public class TaskPageFragment extends BasicFragment {

    private static final String TAG = "TaskPageProcedure";

    private Activity activity;
    private TasksItemsAdapter tasksAdapter;
    private List<UserTask> tasks = new ArrayList<>();
    private UserTask.Status status;

    public TaskPageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fView = inflater.inflate(R.layout.fagment_display_tasks, container, false);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            String key = bundle.getString("status");
            Log.d(TAG, key);
            if(key == null){
                status = UserTask.Status.TODO;
            }
            else if (key.equals(UserTask.Status.TODO.toString())){
                status = UserTask.Status.TODO;
            }
            else if (key.equals(UserTask.Status.DOING.toString())){
                status = UserTask.Status.DOING;
            }
            else if (key.equals(UserTask.Status.DONE.toString())){
                status = UserTask.Status.DONE;
            }
        }

        activity = getParentFragment().getActivity();

        mDisplayView = fView.findViewById(R.id.show_tasks_layout);
        mProgressView = fView.findViewById(R.id.progress_tasks);

        //Refresh
        swipeLayout = fView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "Ask for refresh tasks");
                tasks = new ArrayList<>();
                DataManager.getInstance().refreshAllGroupsTasks();
                swipeLayout.setRefreshing(false);
            }
        });
        swipeLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()),
                getResources().getColor(R.color.colorAccentDark, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()));

        // Tasks
        RecyclerView groupsRecycler = fView.findViewById(R.id.tasks_list);
        tasksAdapter = new TasksItemsAdapter(tasks, getContext(), status.color,
                new TasksClickListener() {
            @Override
            public void onClick(int position) {

            }
        });

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        groupsRecycler.setLayoutManager(mLayoutManager);
        groupsRecycler.setItemAnimator(new DefaultItemAnimator());
        groupsRecycler.setAdapter(tasksAdapter);

        //TODO maybe use swipe controller somehow
        /*SwipeController swipeController = new SwipeController();
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(groupsRecycler);*/

        DataManager instance = DataManager.getInstance();
        instance.addObserver(this);
        if(instance.getTasks() == null) {
            Log.d(TAG, "Ask for refresh tasks");
            instance.refreshAllGroupsTasks();
        }
        else{
            tasksChanged();
        }

        showProgress(false);

        return fView;
    }

    @Override
    public void tasksChanged() {
        List<UserTask> allTasks = DataManager.getInstance().getTasks();
        tasks = new ArrayList<>();
        for(UserTask task : allTasks) {
            if (task.getStatus().name.equals(status.name)) {
                tasks.add(task);
            }
        }
        tasksAdapter.update(tasks);
    }

}
