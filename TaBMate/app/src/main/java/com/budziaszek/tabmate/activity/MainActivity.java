package com.budziaszek.tabmate.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.data.DataManager;
import com.budziaszek.tabmate.data.Group;
import com.budziaszek.tabmate.data.Task;
import com.budziaszek.tabmate.data.Transaction;
import com.budziaszek.tabmate.fragment.BudgetFragment;
import com.budziaszek.tabmate.fragment.DashboardFragment;
import com.budziaszek.tabmate.fragment.TasksPagerFragment;
import com.budziaszek.tabmate.fragment.MainPageFragment;

import com.budziaszek.tabmate.fragment.UserFragment;
import com.budziaszek.tabmate.view.helper.InformUser;
import com.budziaszek.tabmate.view.helper.KeyboardManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * Activity with drawer. Starts new fragments, allow switching between drawer and back mode.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = " MainActivityProcedure";

    // Drawer
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private TextView user_email;


    // For fragments and informative
    private Class newFragment = null;
    private boolean doubleBackToExitPressedOnce = false;
    private Boolean isListenerRegistered;

    private FirebaseUser user = null;
    private Group currentGroup;
    private Task currentTask;
    private Transaction currentTransaction;
    private Boolean isArchiveVisible = false;
    private Boolean filtrateGroups = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        Log.d(TAG, "Created");

        setContentView(R.layout.nav_main);
        initializeDrawer();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);
        user_email = headerLayout.findViewById(R.id.user_email);
        user = FirebaseAuth.getInstance().getCurrentUser();
        user_email.setText(getCurrentUserEmail());

        startFragment(MainPageFragment.class);
    }

    @Override
    public void onBackPressed() {
        setBackEnabled(false);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if(fragments != null){
            for(Fragment fragment : fragments){
                if(fragment != null && fragment.isVisible())
                    if(fragment instanceof MainPageFragment || fragment instanceof DashboardFragment
                            || fragment instanceof TasksPagerFragment || fragment instanceof BudgetFragment
                             || fragment instanceof UserFragment){
                        if (doubleBackToExitPressedOnce)
                            finish();
                        else{
                            this.doubleBackToExitPressedOnce = true;
                            InformUser.inform(this, R.string.double_back);
                            new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
                        }
                        return;
                    } else {
                        super.onBackPressed();
                        return;
                    }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.

        int id = item.getItemId();
        if (id == R.id.nav_home) {
            newFragment = MainPageFragment.class;
        } else if (id == R.id.nav_dashboard) {
            newFragment = DashboardFragment.class;
        } else if (id == R.id.nav_tasks) {
            newFragment = TasksPagerFragment.class;
        } else if (id == R.id.nav_budget) {
            newFragment = BudgetFragment.class;
        } else if (id == R.id.nav_profile) {
            startFragment(UserFragment.class);
        } else if (id == R.id.nav_logOut) {
            alertAndLogOut();
        }

        // Highlight the selected item has been done by NavigationView
        if(id == R.id.nav_logOut || id == R.id.nav_profile){
            //TODO check
            //item.setChecked(true);
        }else {
            item.setChecked(true);
        }
        // Set action bar title
        if(id != R.id.nav_logOut)
            setTitle(item.getTitle());

        // Close the navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);


        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return false;
    }

    public String getCurrentUserEmail() {
        if (user != null)
            return user.getEmail();
        else
            finish();
        return null;
    }

    public String getCurrentUserId() {
        if (user != null)
            return user.getUid();
        else
            finish();
        return null;
    }

    public Group getCurrentGroup() {
        if(currentGroup == null)
            return DataManager.getInstance().getGroups().get(0);
        return currentGroup;
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public Transaction getCurrentTransaction() {
        return currentTransaction;
    }

    public void updateUser() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        user_email.setText(getCurrentUserEmail());

    }

    public Boolean getIsArchivedVisible() {
        return isArchiveVisible;
    }

    public void changeIsArchivedVisible() {
        isArchiveVisible = !isArchiveVisible;
    }

    public void setCurrentGroup(Group group) {
        currentGroup = group;
    }

    public void setCurrentTask(Task task) {
        currentTask = task;
    }

    public void setCurrentTransaction(Transaction transaction) {
        currentTransaction = transaction;
    }

    /**
     * Switches between drawer and back mode.
     *
     * @param enable if true displays back icon instead of drawer (hamburger)
     */
    public void setBackEnabled(boolean enable) {
        if (enable) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            // Remove hamburger
            toggle.setDrawerIndicatorEnabled(false);
            // Show back button
            ActionBar bar = getSupportActionBar();
            if (bar != null) {
                bar.setDisplayHomeAsUpEnabled(true);
            }
            if (!isListenerRegistered) {
                toggle.setToolbarNavigationClickListener(v -> {
                    onBackPressed();
                });
                isListenerRegistered = true;
            }

        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            // Remove back button
            ActionBar bar = getSupportActionBar();
            if (bar != null) {
                bar.setDisplayHomeAsUpEnabled(false);
            }
            // Show hamburger
            toggle.setDrawerIndicatorEnabled(true);
            // Remove the/any drawer toggle listener
            toggle.setToolbarNavigationClickListener(null);
            isListenerRegistered = false;
        }
    }

    /**
     * Starts new fragment.
     *
     * @param fragmentClass class of fragment that will be created and replaced.
     */
    public void startFragment(Class fragmentClass) {
        KeyboardManager.hideKeyboard(this);
        try {
            getSupportFragmentManager().beginTransaction().replace(R.id.flContent, (Fragment) fragmentClass.newInstance())
                    .addToBackStack("Fragment")
                    .commit();
        } catch (Exception e) {
            Log.e(TAG, "Error in fragment transaction " + e.getMessage());
        }
    }

    private void initializeDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        // Respond when the drawer's position changes
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Respond when the drawer is opened
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Respond when the drawer is closed
                        if (newFragment != null) {
                            startFragment(newFragment);
                            newFragment = null;
                        }
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        // Respond when the drawer motion state changes
                    }
                }
        );

        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        setBackEnabled(false);
    }

    /**
     * Shows alert when user choose log out action. If action is confirmed Firebase signOut is called
     * and activity finishes.
     */
    private void alertAndLogOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);

        builder.setTitle(R.string.log_out)
                .setMessage(R.string.confirm_log_out)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
                    MainActivity.this.startActivity(myIntent);
                    Log.d(TAG, "User logged out.");
                    DataManager.getInstance().clear();
                    user = null;
                    finish();
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public Boolean getFiltrateGroups() {
        return filtrateGroups;
    }

    public void setFiltrateGroups(Boolean filtrateGroups) {
        this.filtrateGroups = filtrateGroups;
    }
}
