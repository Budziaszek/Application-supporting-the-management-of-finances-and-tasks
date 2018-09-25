package com.budziaszek.tabmate.firestoreData;

import android.app.Activity;
import android.app.Fragment;
import android.util.Log;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.fragment.MainPageFragment;
import com.budziaszek.tabmate.view.DataChangeListener;
import com.budziaszek.tabmate.view.InformUser;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DataManager {

    private static DataManager instance;

    private List<DataChangeListener> observers = new ArrayList<>();
    private final static String TAG = "DataManagerProcedure";

    private Map<String, Group> groups;
    private Map<String, User> users;
    private Map<String, UserTask> tasks;
    private List<String> invitations;

    private List<UserTask> filtratedTasks;
    private List<String> groupsSelected;

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    private DataManager() {

    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    private void informObserversTasksChanged() {
        for (DataChangeListener listener : observers) {
            listener.tasksChanged();
        }
    }

    private void informObserversGroupsChanged() {
        for (DataChangeListener listener : observers) {
            listener.groupsChanged();
        }
    }

    private void informObserversInvitationsChanged() {
        for (DataChangeListener listener : observers) {
            listener.invitationsChanged();
        }
    }


    public void addObserver(DataChangeListener listener) {
        observers.add(listener);
    }

    public List<Group> getGroups() {
        if (groups == null)
            return null;
        return new ArrayList<>(groups.values());
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public List<UserTask> getTasks() {
        if (tasks == null)
            return null;
        return new ArrayList<>(tasks.values());
    }

    public List<String> getGroupsSelected(){
        return groupsSelected;
    }

    public List<UserTask> getFiltratedTasks() {
        if (filtratedTasks == null)
            return getTasks();
        return filtratedTasks;
    }

    public void addFiltrationOptionGroup(String gid){
         for(UserTask task: tasks.values()){
            if(gid.equals(task.getGroup())){
                filtratedTasks.add(task);
            }
         }
         groupsSelected.add(gid);
    }

    public void removeFiltrationOptionGroup(String gid){
        ArrayList<UserTask> newFiltratedTasks = new ArrayList<>();
        for(UserTask task: filtratedTasks){
            if(!gid.equals(task.getGroup())){
                newFiltratedTasks.add(task);
            }
        }
        filtratedTasks = newFiltratedTasks;
        groupsSelected.remove(gid);
    }

    public List<String> getInvitations() {
        return invitations;
    }

    public void refreshGroupsAndUsers(String uid) {
        firestoreRequests.getGroupByField("members", uid, this::checkGroups);
    }

    public void refreshInvitations(String uid) {
        firestoreRequests.getUser(uid, this::checkAndManageInvitations);
    }

    /**
     * Proceeds document, check and add invitations, if found manage their acceptation or removal.
     */
    private void checkAndManageInvitations(DocumentSnapshot documentSnapshot) {
        User user = documentSnapshot.toObject(User.class);
        if (user != null) {
            if (user.getInvitations() != null) {
                invitations = user.getInvitations();
                Log.d(TAG, "Invitations " + invitations.toString());
                informObserversInvitationsChanged();
            }
        }
    }

    public void refreshAllGroupsTasks() {
        tasks = new TreeMap<>();
        filtratedTasks = new ArrayList<>();

        List<String> gid = new ArrayList<>();
        //TODO null pointer fix
        if(groups == null)
            return;
        for (Group group : groups.values()) {
            gid.add(group.getId());
        }
        for (String id : gid) {
            Log.d(TAG, "Refresh tasks for group: " + id);
            firestoreRequests.getGroupTasks(id, this::checkTasks);
        }
    }

    public void refreshGroupTasks(String gid) {
        Log.d(TAG, "Refresh tasks for group: " + gid);
        firestoreRequests.getGroupTasks(gid, this::checkTasks);
    }

    public void removeGroupMember(String user, String group, Activity activity) {
        firestoreRequests.removeGroupMember(group,
                user,
                (aVoid) -> {
                    InformUser.inform(activity, R.string.left_group);
                },
                (e) -> InformUser.informFailure(activity, e)
        );
    }

    public void removeGroup(String group, Activity activity) {
        firestoreRequests.removeGroup(group,
                (aVoid) -> {
                    InformUser.inform(activity, R.string.left_group);
                },
                (e) -> InformUser.informFailure(activity, e)
        );
    }

    private void checkGroups(Task<QuerySnapshot> task) {
        groups = new TreeMap<>();
        groupsSelected = new ArrayList<>();
        users = new TreeMap<>();
        tasks = new TreeMap<>();
        filtratedTasks = new ArrayList<>();

        if (task.isSuccessful()) {
            if (task.getResult().getDocuments().isEmpty()) {
                Log.d(TAG, "No group found");
            } else {
                addGroups(task.getResult().getDocuments());
            }
        } else {
            Exception exception = task.getException();
            //if(exception != null)
            //InformUser.informFailure(activity, exception);
            Log.d(TAG, exception.getMessage());
        }
    }

    private void addGroups(List<DocumentSnapshot> documents) {
        for (DocumentSnapshot document : documents) {
            Group group = document.toObject(Group.class);
            if (group != null) {
                group.setId(document.getId());
                groups.put(document.getId(), group);
                groupsSelected.add(group.getId());
                informObserversGroupsChanged();
                Log.d(TAG, "User group: " + group.getId());
                refreshGroupTasks(group.getId());

                for (String uid : group.getMembers()) {
                    firestoreRequests.getUser(uid, this::addUserToList);
                }
            }
        }
    }

    private void addUserToList(DocumentSnapshot documentSnapshot) {
        User user = documentSnapshot.toObject(User.class);
        if (user != null) {
            users.put(user.getId(), user);
        }
    }

    private void checkTasks(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            if (task.getResult().getDocuments().isEmpty()) {
            } else {
                addTasks(task.getResult().getDocuments());
            }
        } else {
            Exception exception = task.getException();
            //if(exception != null)
            //InformUser.informFailure(activity, exception);
            Log.d(TAG, exception.getMessage());
        }
    }

    private void addTasks(List<DocumentSnapshot> documents) {
        for (DocumentSnapshot document : documents) {
            UserTask task = document.toObject(UserTask.class);
            if (task != null) {
                task.setId(document.getId());
                tasks.put(document.getId(), task);
                filtratedTasks.add(task);
                informObserversTasksChanged();
                Log.d(TAG, "Task: " + task.getTitle() + " (" + task.getGroup() + ")");

            }
        }
    }
}
