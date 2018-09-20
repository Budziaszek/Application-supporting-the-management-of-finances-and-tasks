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

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.firestoreData.UserTask;
import com.budziaszek.tabmate.view.adapter.TasksItemsAdapter;
import com.budziaszek.tabmate.view.listener.TasksClickListener;

import java.util.ArrayList;
import java.util.List;

public class TaskPageFragment extends BasicFragment {

    private static final String TAG = "TaskPageProcedure";

    private TasksItemsAdapter tasksAdapter;
    private List<UserTask> tasks = new ArrayList<>();
    private String key;

    public enum Status {

        TODO("ToDo"),
        DOING("Doing"),
        DONE("Done");

        String name;

        private Status(String s) {
            name = s;
        }
    }


    public TaskPageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fView = inflater.inflate(R.layout.fagment_display_tasks, container, false);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            key = bundle.getString("status");
        }

        mDisplayView = fView.findViewById(R.id.show_tasks_layout);
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
        tasksAdapter = new TasksItemsAdapter(tasks, new TasksClickListener() {
            @Override
            public void onClick(int position) {

            }
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        groupsRecycler.setLayoutManager(mLayoutManager);
        groupsRecycler.setItemAnimator(new DefaultItemAnimator());
        groupsRecycler.setAdapter(tasksAdapter);


        showProgress(false);
        //((MainActivity)getActivity()).refreshGroupsAndUsers();
        //afterRefresh();

        return fView;
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
