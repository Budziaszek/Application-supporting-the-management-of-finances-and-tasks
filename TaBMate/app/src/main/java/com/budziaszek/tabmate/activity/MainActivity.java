package com.budziaszek.tabmate.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
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
import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.firestoreData.User;
import com.budziaszek.tabmate.fragment.AddGroupFragment;
import com.budziaszek.tabmate.fragment.BasicFragment;
import com.budziaszek.tabmate.fragment.DisplayGroupFragment;
import com.budziaszek.tabmate.fragment.DisplayTasksFragment;
import com.budziaszek.tabmate.fragment.MainPageFragment;
import com.budziaszek.tabmate.view.InformUser;
import com.budziaszek.tabmate.view.ProgressInform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ProgressInform {

    private static final String TAG =  "MainProcedure";

    private Class newFragment = null;

    private GroupsManager groupsManager;
    private Integer currentGroupIndex = 0;

    private FirebaseUser user = null;

    public String getCurrentUserEmail(){
        if(user!=null)
            return user.getEmail();
        else
            finish();
        return null;
    }

    public String getCurrentUserId(){
        if(user!=null)
            return user.getUid();
        else
            finish();
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initializeDrawer();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);
        TextView user_email = headerLayout.findViewById(R.id.user_email);
        user = FirebaseAuth.getInstance().getCurrentUser();

        user_email.setText(getCurrentUserEmail());

        groupsManager = new GroupsManager(this);
        startFragment(MainPageFragment.class);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id == R.id.nav_home){
            newFragment = MainPageFragment.class;
        }else if (id == R.id.nav_dashboard){
            //TODO add dashboard fragment
        }else if (id == R.id.nav_tasks) {
            newFragment = DisplayTasksFragment.class;
        }else if (id == R.id.nav_group) {
            newFragment = DisplayGroupFragment.class;
        }else if (id == R.id.nav_logOut) {
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
    public void informInProgress(Boolean isInProgress){
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if(fragments != null){
            for(Fragment fragment : fragments){
                if(fragment != null && fragment.isVisible())
                    ((BasicFragment)fragment).informInProgress(isInProgress);
            }
        }
    }

    public void startFragment(Class fragmentClass){
        try {
            getSupportFragmentManager().beginTransaction().replace(R.id.flContent, (Fragment) fragmentClass.newInstance())
                    .addToBackStack("fragment").commit();
        } catch (Exception e) {
            Log.e(TAG, "Error in fragment transaction " + e.getMessage());
        }
    }

    public void startEditFragment(){
        try {
            AddGroupFragment newFragment = AddGroupFragment.class.newInstance();
            newFragment.setEdit();
            getSupportFragmentManager().beginTransaction().replace(R.id.flContent,  newFragment).addToBackStack("fragment")
                    .commit();

        } catch (Exception e) {
            Log.e(TAG, "Error in fragment transaction " + e.getMessage());
        }
    }

    public void refreshGroupsAndUsers(){
        groupsManager.refresh();
    }

    public Map<String, User> getUsers(){
        return groupsManager.getUsers();
    }

    public List<Group> getGroups(){
        return groupsManager.getGroups();
    }

    public Group getCurrentGroup(){
        return groupsManager.getGroups().get(currentGroupIndex);
    }

    public int getCurrentGroupIndex(){
        return currentGroupIndex;
    }

    public void setCurrentGroupIndex(int index){
        currentGroupIndex = index;
    }

    public Boolean setNextGroup(){
        if(currentGroupIndex < groupsManager.getGroups().size()) {
            currentGroupIndex++;
        }
        return currentGroupIndex == groupsManager.getGroups().size() - 1;
    }

    public Boolean setPreviousGroupIndex(){
        if(currentGroupIndex != 0) {
            currentGroupIndex--;
        }
        return currentGroupIndex == 0;
    }

    private void initializeDrawer(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
                        if(newFragment != null) {
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

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * Shows alert when user choose log out action. If action is confirmed Firebase signOut is called
     * and activity finishes.
     */
    private void alertAndLogOut(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);

        builder.setTitle(R.string.log_out)
                .setMessage(R.string.confirm_log_out)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
                        MainActivity.this.startActivity(myIntent);
                        Log.d(TAG, "User logged out.");
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Displays alert and removes user group group if submitted.
     */
    public void alertLeaveGroup(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);

        builder.setTitle(R.string.leave_group)
                .setMessage(R.string.confirm_leave_group)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Group currentGroup = getCurrentGroup();
                        if(currentGroup.getMembers().size() > 1) {
                            //Remove only user
                            groupsManager.removeGroupMember(getCurrentUserId(), currentGroup.getId());
                        }
                        else{
                            //Remove whole group
                            groupsManager.removeGroup(currentGroup.getId());
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
