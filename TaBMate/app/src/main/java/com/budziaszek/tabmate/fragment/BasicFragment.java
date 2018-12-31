package com.budziaszek.tabmate.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.data.DataManager;
import com.budziaszek.tabmate.view.listener.DataChangeListener;

public class BasicFragment extends Fragment implements DataChangeListener {

    protected View fView;
    protected View mDisplayView;
    protected View mProgressView;
    protected SwipeRefreshLayout swipeLayout;
    protected Activity activity;

    @Override
    public void onResume() {
        super.onResume();
        informAboutNetworkConnection();
        informAboutDataSynchronization();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    protected void showProgress(final boolean show) {
        if (getActivity() == null)
            return;

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        if (mDisplayView == null || mProgressView == null)
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

    protected Boolean checkNetworkConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public void informAboutNetworkConnection() {
        if (fView == null || fView.findViewById(R.id.no_network_connection) == null)
            return;
        //Check network
        if (checkNetworkConnection()) {
            fView.findViewById(R.id.no_network_connection).setVisibility(View.GONE);
        } else {
            fView.findViewById(R.id.no_network_connection).setVisibility(View.VISIBLE);
        }
    }

    public void informAboutDataSynchronization() {
        if (fView == null || fView.findViewById(R.id.data_has_changed) == null || (swipeLayout!= null && swipeLayout.isRefreshing()))
            return;
        if (DataManager.getInstance().getDataHasChanged()) {
            fView.findViewById(R.id.data_has_changed).setVisibility(View.VISIBLE);
        } else {
            fView.findViewById(R.id.data_has_changed).setVisibility(View.GONE);
        }
    }

    @Override
    public void groupsChanged() {

    }

    @Override
    public void tasksChanged() {

    }


    @Override
    public void invitationsChanged() {

    }

    @Override
    public void transactionsChanged() {

    }

    @Override
    public void refreshFinished() {
        swipeLayout.setRefreshing(false);
        showProgress(false);
    }

}
