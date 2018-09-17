package com.budziaszek.tabmate.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;

import com.budziaszek.tabmate.view.ProgressInform;

public class BasicFragment extends Fragment implements ProgressInform {

    protected View mDisplayView;
    protected View mProgressView;
    protected SwipeRefreshLayout swipeLayout;

    @Override
    public void informInProgress(Boolean isInProgress){
        Log.d("BasicFragmentProcedure", "inform progress "  + isInProgress);
        showProgress(isInProgress);
        if(swipeLayout != null) {
            swipeLayout.setRefreshing(false);
            Log.d("BasicFragmentProcedure", "setRefreshing false");
        }
        afterRefresh();

    }

    public void afterRefresh(){
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    protected void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

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
}
