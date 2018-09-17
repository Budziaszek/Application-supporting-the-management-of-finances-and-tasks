package com.budziaszek.tabmate.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.firestoreData.Task;
import com.budziaszek.tabmate.view.TasksAdapter;
import com.budziaszek.tabmate.view.TasksClickListener;

import java.util.ArrayList;
import java.util.List;

public class DisplayTasksFragment extends BasicFragment {

    private static final String TAG = "DisplayTasksProcedure";

    private TasksAdapter tasksAdapter;
    private List<Task> tasks = new ArrayList<>();

    //private FirestoreRequests firestoreRequests = new FirestoreRequests();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fView = inflater.inflate(R.layout.tasks_page, container, false);

        mDisplayView = fView.findViewById(R.id.user_tasks_layout);
        mProgressView = fView.findViewById(R.id.progress_tasks);

        //Refresh
        swipeLayout = fView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                afterRefresh();
                swipeLayout.setRefreshing(false);
                //((MainActivity)getActivity()).refreshGroupsAndUsers();
            }
        });
        swipeLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()),
                getResources().getColor(R.color.colorAccentDark, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()));

        // Tasks
        RecyclerView groupsRecycler = fView.findViewById(R.id.tasks_list);
        tasksAdapter = new TasksAdapter(tasks, new TasksClickListener() {
            @Override
            public void onClick(int position) {

            }
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        groupsRecycler.setLayoutManager(mLayoutManager);
        groupsRecycler.setItemAnimator(new DefaultItemAnimator());
        groupsRecycler.setAdapter(tasksAdapter);

        Button newTaskButton = fView.findViewById(R.id.new_task_button);
        newTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //((MainActivity)getActivity()).startFragment(NewGroupFragment.class);
            }
        });

        showProgress(true);
        //((MainActivity)getActivity()).refreshGroupsAndUsers();
        afterRefresh();

        return fView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    /**
     * Update data being displayed.
     */
    @Override
    public void afterRefresh(){
        //List<Tasks> tasks = ((MainActivity)getActivity()).getTasks();
        tasksAdapter.update(tasks);
        Log.d(TAG, "after refresh");
    }
}