package com.budziaszek.tabmate.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import com.budziaszek.tabmate.view.listener.DataChangeListener;

public class BasicFragment extends Fragment implements DataChangeListener {

    protected View mDisplayView;
    protected View mProgressView;
    protected SwipeRefreshLayout swipeLayout;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    protected void showProgress(final boolean show) {
        if(getActivity() == null)
            return;

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        if(mDisplayView == null || mProgressView == null)
            return;

        mDisplayView.setVisibility(show ? View.GONE : View.VISIBLE);
        mDisplayView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mDisplayView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void groupsChanged(){

    }

    @Override
    public void tasksChanged(){

    }


    @Override
    public void invitationsChanged(){

    }

    @Override
    public void groupItemInserted(int position) {

    }

    @Override
    public void groupItemRemoved(int position) {

    }
}
