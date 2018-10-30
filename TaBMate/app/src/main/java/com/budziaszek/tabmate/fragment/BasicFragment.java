package com.budziaszek.tabmate.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.view.listener.DataChangeListener;

public class BasicFragment extends Fragment implements DataChangeListener {

    protected View fView;
    protected View mDisplayView;
    protected View mProgressView;
    protected SwipeRefreshLayout swipeLayout;

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
        //Check network
        if (checkNetworkConnection()) {
            fView.findViewById(R.id.no_network_connection).setVisibility(View.GONE);
        } else {
            fView.findViewById(R.id.no_network_connection).setVisibility(View.VISIBLE);
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
    public void finished() {
        swipeLayout.setRefreshing(false);
        showProgress(false);
    }

}
