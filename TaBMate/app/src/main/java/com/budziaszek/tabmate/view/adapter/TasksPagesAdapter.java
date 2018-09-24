package com.budziaszek.tabmate.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.firestoreData.DataManager;
import com.budziaszek.tabmate.firestoreData.UserTask;
import com.budziaszek.tabmate.fragment.TaskPageFragment;
import com.budziaszek.tabmate.view.DataChangeListener;

import java.util.ArrayList;
import java.util.List;

public class TasksPagesAdapter extends FragmentStatePagerAdapter{

    private Context mContext;
    private String[] tabs = { "ToDo", "Doing", "Done" };
    private List<Fragment> fragments = new ArrayList<>();

    public TasksPagesAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
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
        fragments.add(taskPageFragment);
        return taskPageFragment;
       // return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        //TODO change
        //mContext.getString(R.string.category_places);
        return tabs[position];
    }

}

