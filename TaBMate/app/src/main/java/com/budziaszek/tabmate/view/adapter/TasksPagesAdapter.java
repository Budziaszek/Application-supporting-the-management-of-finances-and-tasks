package com.budziaszek.tabmate.view.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.budziaszek.tabmate.firestoreData.UserTask;
import com.budziaszek.tabmate.fragment.TaskPageFragment;

public class TasksPagesAdapter extends FragmentStatePagerAdapter {

    private String[] tabs = {"ToDo", "Doing", "Done"};

    public TasksPagesAdapter(FragmentManager fm) {
        super(fm);
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
        }
        taskPageFragment.setArguments(bundle);
        return taskPageFragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[position];
    }

}

