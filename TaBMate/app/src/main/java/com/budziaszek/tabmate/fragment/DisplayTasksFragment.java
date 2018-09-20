package com.budziaszek.tabmate.fragment;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.view.adapter.TasksPagesAdapter;

public class DisplayTasksFragment extends BasicFragment {

    private static final String TAG = "DisplayTasksProcedure";

    //private FirestoreRequests firestoreRequests = new FirestoreRequests();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fView = inflater.inflate(R.layout.tasks_pager, container, false);


        // Pager initilization
        ViewPager viewPager = (ViewPager)  fView.findViewById(R.id.viewpager);
        TasksPagesAdapter adapter = new TasksPagesAdapter(getActivity(), getChildFragmentManager());
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) fView.findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

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
        getActivity().getMenuInflater().inflate(R.menu.menu_tasks, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_add_task){
            ((MainActivity)getActivity()).startFragment(AddTaskFragment.class);
            return true;
        }
        return false;
    }
}