package com.budziaszek.tabmate.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.firestoreData.UserTask;
import com.budziaszek.tabmate.fragment.TasksPagerFragment;
import com.budziaszek.tabmate.fragment.MainPageFragment;

import com.budziaszek.tabmate.fragment.UserFragment;
import com.budziaszek.tabmate.view.InformUser;
import com.budziaszek.tabmate.view.KeyboardManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.function.Consumer;


/**
 * Activity with drawer. Starts new fragments, allow switching between drawer and back mode.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = " MainActivityProcedure";

    // Drawer
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private Boolean isListenerRegistered;

    // Fragments
    private Class newFragment = null;

    // User data
    private Group currentGroup;
    private UserTask currentTask;
    private FirebaseUser user = null;
    private Boolean isArchiveVisible = false;
    private TextView user_email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        Log.d(TAG, "Created");

        setContentView(R.layout.activity_main);
        initializeDrawer();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);
        user_email = headerLayout.findViewById(R.id.user_email);
        user = FirebaseAuth.getInstance().getCurrentUser();
        user_email.setText(getCurrentUserEmail());

        headerLayout.setOnClickListener(view -> {
            startFragment(UserFragment.class);
            drawer.closeDrawers();
        });

        startFragment(MainPageFragment.class);
    }

    @Override
    public void onBackPressed() {
        enableBack(false);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.

        int id = item.getItemId();
        if (id == R.id.nav_home) {
            newFragment = MainPageFragment.class;
        } else if (id == R.id.nav_dashboard) {
            Log.d(TAG, "dashboard fragment");
            //TODO add dashboard fragment
        } else if (id == R.id.nav_tasks) {
            newFragment = TasksPagerFragment.class;
        } else if (id == R.id.nav_logOut) {
            alertAndLogOut();
        }

        // Highlight the selected item has been done by NavigationView
        item.setChecked(true);
        // Set action bar title
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
        if (id == R.id.action_edit) {
            return false;
        }
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
        return currentGroup;
    }

    public UserTask getCurrentTask() {
        return currentTask;
    }

    public void updateUser(){
        user = FirebaseAuth.getInstance().getCurrentUser();
        user_email.setText(getCurrentUserEmail());

    }

    public Boolean getIsArchivedVisible(){
        return isArchiveVisible;
    }

    public void changeIsArchivedVisible(){
        isArchiveVisible = !isArchiveVisible;
    }

    public void setCurrentGroup(Group group) {
        currentGroup = group;
    }

    public void setCurrentTask(UserTask task) {
        currentTask = task;
    }

    /**
     * Switches between drawer and back mode.
     *
     * @param enable if true displays back icon instead of drawer (hamburger)
     */
    public void enableBack(boolean enable) {
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
                    Log.d(TAG, "Back arrow clicked");
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

    public void setDrawerVisible(Boolean visible){
        toggle.setDrawerIndicatorEnabled(visible);
        drawer.setDrawerLockMode(visible ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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
        enableBack(false);
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
                    finish();
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
