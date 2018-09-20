package com.budziaszek.tabmate.view.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.budziaszek.tabmate.fragment.TaskPageFragment;

public class TasksPagesAdapter extends FragmentStatePagerAdapter {

    private Context mContext;
    private String[] tabs = { "ToDo", "Doing", "Done" };

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
                bundle.putString("status", TaskPageFragment.Status.TODO.name());
                break;
            case 1:
                bundle.putString("status", TaskPageFragment.Status.DOING.name());
                break;
            case 2:
                bundle.putString("status", TaskPageFragment.Status.DONE.name());
                break;
        }
        taskPageFragment.setArguments(bundle);
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

