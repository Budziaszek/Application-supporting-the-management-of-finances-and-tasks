package com.budziaszek.tabmate.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.data.DataManager;
import com.budziaszek.tabmate.data.FirestoreRequests;
import com.budziaszek.tabmate.data.Group;
import com.budziaszek.tabmate.data.Task;
import com.budziaszek.tabmate.view.adapter.GroupsItemsAdapter;
import com.budziaszek.tabmate.view.adapter.TasksItemsAdapter;
import com.budziaszek.tabmate.view.listener.GroupClickListener;
import com.budziaszek.tabmate.view.helper.InformUser;
import com.budziaszek.tabmate.view.listener.InvitationClickListener;
import com.budziaszek.tabmate.view.adapter.InvitationsItemsAdapter;
import com.budziaszek.tabmate.view.listener.TaskClickListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainPageFragment extends BasicFragment {

    private static final String TAG = "MainPageFragmentProcedure";

    private GroupsItemsAdapter groupsAdapter;
    private List<Group> groups = new ArrayList<>();

    //private Activity activity;

    @SuppressLint("UseSparseArrays")
    private Map<Integer, TasksItemsAdapter> tasksItemsAdapterMap = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private Map<Integer, List<Task>> tasks = new HashMap<>();

    private InvitationsItemsAdapter invitationsAdapter;
    private List<String> invitationsList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Created");
        fView = inflater.inflate(R.layout.main_page, container, false);

        activity = getActivity();

        mDisplayView = fView.findViewById(R.id.show_groups_layout);
        mProgressView = fView.findViewById(R.id.progress_groups);

        tasks.put(R.drawable.ripple_effect_todo, new ArrayList<>());
        tasks.put(R.drawable.ripple_effect_doing, new ArrayList<>());
        tasks.put(R.drawable.ripple_effect_done, new ArrayList<>());

        //Refresh
        swipeLayout = fView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(() -> {
            if (!DataManager.getInstance().isRefreshFinished())
                return;

            Log.d(TAG, "Ask for refresh groups and users");
            DataManager.getInstance().refresh(((MainActivity) activity).getCurrentUserId());

            Log.d(TAG, "Ask for refresh invitations");
            DataManager.getInstance().refreshInvitations(((MainActivity) activity).getCurrentUserId());

            informAboutNetworkConnection(); informAboutDataSynchronization();
        });
        swipeLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()),
                getResources().getColor(R.color.colorAccentDark, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()));

        // Groups
        setRecyclerGroups();
        setRecyclerInvitations();
        setRecyclerTasks(R.id.user_tasks_list_doing, R.drawable.ripple_effect_doing);
        setRecyclerTasks(R.id.user_tasks_list_todo, R.drawable.ripple_effect_todo);
        setRecyclerTasks(R.id.user_tasks_list_done, R.drawable.ripple_effect_done);

        DataManager instance = DataManager.getInstance();
        instance.addObserver(this);
        if (instance.getGroups() == null) {
            showProgress(true);
            Log.d(TAG, "Ask for refresh groups and users");
            instance.refresh(((MainActivity) activity).getCurrentUserId());
        } else {
            groupsChanged();
            tasksChanged();
        }

        Log.d(TAG, "Ask for refresh invitations");
        DataManager.getInstance().refreshInvitations(((MainActivity) activity).getCurrentUserId());

        informAboutNetworkConnection(); informAboutDataSynchronization();
        return fView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    private void setRecyclerGroups() {
        RecyclerView groupsRecycler = fView.findViewById(R.id.groups_list);
        groupsAdapter = new GroupsItemsAdapter(groups, new GroupClickListener() {
            @Override
            public void onItemLongClicked(int position) {
                ((MainActivity) activity).setCurrentGroup(groups.get(position));
                ((MainActivity) activity).startFragment(BudgetFragment.class);
            }
            @Override
            public void onItemClicked(int position) {
                ((MainActivity) activity).setCurrentGroup(groups.get(position));
                ((MainActivity) activity).startFragment(GroupFragment.class);
            }
            @Override
            public void onButtonClicked(int position) {
                ((MainActivity) activity).setCurrentGroup(groups.get(position));
                ((MainActivity)activity).startFragment(TransactionFragment.class);
            }
        });

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        groupsRecycler.setLayoutManager(mLayoutManager);
        groupsRecycler.setItemAnimator(new DefaultItemAnimator());
        groupsRecycler.setAdapter(groupsAdapter);

        Button newGroupButton = fView.findViewById(R.id.new_group_button);
        newGroupButton.setOnClickListener(view -> {
            ((MainActivity) activity).setBackEnabled(true);
            ((MainActivity) activity).setCurrentGroup(null);
            ((MainActivity) activity).startFragment(GroupFragment.class);
        });

//        Button allTasksButton = fView.findViewById(R.id.all_tasks_button);
//        allTasksButton.setOnClickListener(view -> ((MainActivity) activity).startFragment(TasksPagerFragment.class));

        Button selectTasksButton = fView.findViewById(R.id.select_tasks_button);
        selectTasksButton.setOnClickListener(view -> ((MainActivity) activity).startFragment(AssignTasksFragment.class));

//        Button addTaskButton = fView.findViewById(R.id.add_task_button);
//        addTaskButton.setOnClickListener(view -> {
//            ((MainActivity) activity).setCurrentTask(null);
//            ((MainActivity) activity).startFragment(TaskFragment.class);
//        });
    }

    private void setRecyclerInvitations() {
        RecyclerView invitationsRecycler = fView.findViewById(R.id.invitations_list);
        invitationsAdapter = new InvitationsItemsAdapter(invitationsList, new InvitationClickListener() {
            @Override
            public void onAcceptClicked(int position) {
                showProgress(true);
                FirestoreRequests.addGroupMember(invitationsList.get(position), ((MainActivity) activity).getCurrentUserId(),
                        (aVoid) -> {
                            showProgress(false);
                            Log.d(TAG, "Invitation accepted.");
                        },
                        (e) -> {
                            InformUser.inform(getActivity(), R.string.invitation_incorrect);
                            showProgress(false);
                        });
                FirestoreRequests.removeInvitation(invitationsList.get(position), ((MainActivity) activity).getCurrentUserId(),
                        (aVoid) -> {
                            showProgress(false);
                            Log.d(TAG, "(Invalid) Invitation removed.");
                        },
                        (e) -> {
                            InformUser.informFailure(getActivity(), e);
                            Log.e(TAG, e.getMessage());
                            showProgress(false);
                        });
                DataManager.getInstance().refreshInvitations(((MainActivity) getActivity()).getCurrentUserId());
                DataManager.getInstance().refresh(((MainActivity) activity).getCurrentUserId());

            }

            @Override
            public void onRemoveClicked(int position) {
                FirestoreRequests.removeInvitation(invitationsList.get(position), ((MainActivity) activity).getCurrentUserId(),
                        (aVoid) -> {
                            showProgress(false);
                            //TODO snackbar
                            Log.d(TAG, "Invitation removed.");
                            DataManager.getInstance().refreshInvitations(((MainActivity) getActivity()).getCurrentUserId());
                        },
                        (e) -> {
                            InformUser.informFailure(getActivity(), e);
                            Log.e(TAG, e.getMessage());
                            showProgress(false);
                        });
            }
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        invitationsRecycler.setLayoutManager(mLayoutManager);
        invitationsRecycler.setItemAnimator(new DefaultItemAnimator());
        invitationsRecycler.setAdapter(invitationsAdapter);
    }

    private void setRecyclerTasks(int recycler, int color) {
        RecyclerView tasksRecycler = fView.findViewById(recycler);
        tasksItemsAdapterMap.put(color, new TasksItemsAdapter(tasks.get(color), getContext(), color,
                new TaskClickListener() {
                    @Override
                    public void onClick(int position) {
                        ((MainActivity) activity).setCurrentTask(tasks.get(color).get(position));
                        ((MainActivity) activity).startFragment(TaskFragment.class);
                    }

                    @Override
                    public void onLongClick(int position) {
                        Task task = tasks.get(color).get(position);
                        if(task.getStatus() == Task.Status.ARCHIVED) {
                            return;
                        }
                        task.setNextStatus();
                        FirestoreRequests.updateTask(task,
                                (aVoid) -> {},
                                (e) -> Log.d(TAG, e.getMessage())
                        );
                        DataManager.getInstance().refresh(((MainActivity)activity).getCurrentUserId());
                        InformUser.inform(activity, R.string.task_moved);
                    }
                }, ((MainActivity)activity).getCurrentUserId()));

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        tasksRecycler.setLayoutManager(mLayoutManager);
        tasksRecycler.setItemAnimator(new DefaultItemAnimator());
        tasksRecycler.setAdapter(tasksItemsAdapterMap.get(color));
    }

    @Override
    public void invitationsChanged() {
        invitationsList = DataManager.getInstance().getInvitations();
        invitationsAdapter.update(invitationsList);
    }

    @Override
    public void groupsChanged() {
        List<Group> newGroups = DataManager.getInstance().getGroups();
        List<Group> oldGroups = groups;
        newGroups.sort(Comparator.comparing(Group::getName));
        groups = newGroups;
        groupsAdapter.update(groups);

        for (int i = 0; i < newGroups.size(); i++) {
            if (oldGroups.size() <= i) {
                groupsAdapter.notifyItemInserted(i);
            } else {
                Group newGroup = newGroups.get(i);
                Group oldGroup = oldGroups.get(i);
                if (!oldGroup.equals(newGroup)) {
                    groupsAdapter.notifyItemChanged(i);
                }
            }
        }
        for (int i = newGroups.size(); i < oldGroups.size(); i++) {
            groupsAdapter.notifyItemRemoved(i);
        }
    }

    @Override
    public void tasksChanged() {
        List<Task> allTasks = DataManager.getInstance().getTasks();
        tasks.get(R.drawable.ripple_effect_doing).clear();
        tasks.get(R.drawable.ripple_effect_done).clear();
        tasks.get(R.drawable.ripple_effect_todo).clear();

        String uid = ((MainActivity) activity).getCurrentUserId();

        for (Task task : allTasks) {
            if (task.getDoers().contains(uid)) {
                if (task.getStatus().name.equals(Task.Status.DOING.name))
                    tasks.get(R.drawable.ripple_effect_doing).add(task);
                else if (task.getStatus().name.equals(Task.Status.DONE.name))
                    tasks.get(R.drawable.ripple_effect_done).add(task);
                else if (task.getStatus().name.equals(Task.Status.TODO.name))
                    tasks.get(R.drawable.ripple_effect_todo).add(task);
            }
        }

        for(Integer k:tasks.keySet()){
            tasks.get(k).sort(Comparator.comparing(Task::getTitle));
            tasksItemsAdapterMap.get(k).updateAll(tasks.get(k));
            //tasksItemsAdapterMap.get(k).notifyDataSetChanged();
        }
    }
}