package com.budziaszek.tabmate.view.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.budziaszek.tabmate.firestoreData.UserTask;
import com.budziaszek.tabmate.fragment.TaskPageFragment;

import java.util.ArrayList;
import java.util.List;

public class TasksPagesAdapter extends FragmentStatePagerAdapter {

    private List<String> tabs = new ArrayList<>();

    public TasksPagesAdapter(FragmentManager fm) {
        super(fm);
        tabs.add("ToDo");
        tabs.add("Doing");
        tabs.add("Done");
    }

    @Override
    public Fragment getItem(int index) {
        TaskPageFragment taskPageFragment = new TaskPageFragment();
        Bundle bundle = new Bundle();
        switch (index) {
            case 0:
                bundle.putString("status", UserTask.Status.TODO.name());
                break;
            case 1:
                bundle.putString("status", UserTask.Status.DOING.name());
                break;
            case 2:
                bundle.putString("status", UserTask.Status.DONE.name());
                break;
            case 3:
                bundle.putString("status", UserTask.Status.ARCHIVED.name());
                break;
        }
        taskPageFragment.setArguments(bundle);
        return taskPageFragment;
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs.get(position);
    }

    public void changeArchivedVisibility() {
        if(tabs.contains("Archived"))
            tabs.remove("Archived");
        else
            tabs.add("Archived");
        notifyDataSetChanged();
    }

}

