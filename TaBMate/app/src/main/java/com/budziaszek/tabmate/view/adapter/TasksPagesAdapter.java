package com.budziaszek.tabmate.view.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.firestoreData.UserTask;
import com.budziaszek.tabmate.fragment.TaskPageFragment;

import java.util.ArrayList;
import java.util.List;

public class TasksPagesAdapter extends FragmentStatePagerAdapter {

    private List<String> tabs = new ArrayList<>();
    private Context contxt;

    public TasksPagesAdapter(FragmentManager fm, Context contxt) {
        super(fm);
        this.contxt = contxt;
        tabs.add(contxt.getResources().getString(R.string.task_todo));//("ToDo");
        tabs.add(contxt.getResources().getString(R.string.task_doing));//("Doing");
        tabs.add(contxt.getResources().getString(R.string.task_done));//("Done");
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
        if(tabs.contains(contxt.getResources().getString(R.string.task_archived)))//("Archived"))
            tabs.remove(contxt.getResources().getString(R.string.task_archived));//("Archived");
        else
            tabs.add(contxt.getResources().getString(R.string.task_archived));//("Archived");
        notifyDataSetChanged();
    }

}

