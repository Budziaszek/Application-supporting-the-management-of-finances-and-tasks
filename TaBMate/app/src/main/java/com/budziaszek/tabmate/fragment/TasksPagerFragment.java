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

public class TasksPagerFragment extends BasicFragment {

    private static final String TAG = "TasksPagerFragmentProcedure";
    //private Activity activity;
    private TasksPagesAdapter adapter;
    private ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "Created");
        fView = inflater.inflate(R.layout.tasks_pager, container, false);

        informAboutNetworkConnection(); informAboutDataSynchronization();
        activity = getActivity();

        // Pager initialization
        viewPager = fView.findViewById(R.id.viewpager);
        adapter = new TasksPagesAdapter(getChildFragmentManager(), getContext());
        viewPager.setAdapter(adapter);
        if (((MainActivity) activity).getIsArchivedVisible()) {
            adapter.changeArchivedVisibility();
            viewPager.setCurrentItem(adapter.getCount() - 1);
        }

        TabLayout tabLayout = fView.findViewById(R.id.sliding_tabs);
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

        if (id == R.id.action_add_task) {
            ((MainActivity) activity).setBackEnabled(true);
            ((MainActivity) activity).setCurrentTask(null);
            ((MainActivity) activity).startFragment(TaskFragment.class);
            return true;
        } else if (id == R.id.action_find_tasks) {
            ((MainActivity) activity).setFiltrateGroups(true);
            ((MainActivity) activity).setBackEnabled(true);
            ((MainActivity) activity).startFragment(FiltrateFragment.class);
            return true;
        } else if (id == R.id.action_archived_tasks) {
            adapter.changeArchivedVisibility();
            viewPager.setCurrentItem(adapter.getCount() - 1);
            ((MainActivity) activity).changeIsArchivedVisible();
            return true;
        }
        return false;
    }
}