package com.budziaszek.tabmate.firestoreData;

import android.app.Activity;
import android.util.Log;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.view.DataChangeListener;
import com.budziaszek.tabmate.view.InformUser;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Singleton class that gets data from Firestore, refresh it, filtrate and allow to access it.
 **/
public class DataManager {

    private final static String TAG = "DataManagerProcedure";

    private static DataManager instance;

    private int refreshCounter = 0;
    // Observers, that will be informed about data changes
    private List<DataChangeListener> observers = new ArrayList<>();
    private Boolean groupsChanged = false;
    private Boolean tasksChanged = false;
    private Boolean invitationsChanged = false;

    // All data from database
    private Map<String, Group> groups;
    private Map<String, User> users;
    private Map<String, UserTask> tasks;
    private List<String> invitations;

    // Filtrated data and filters
    private Map<String, UserTask> filtratedTasks;
    private List<String> selectedGroupsIds;
    private List<String> selectedUsersIds;

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    private DataManager() {

    }

    /**
     * Creates class instance if it wasn't created before and returns (singleton) instance.
     * @return instance, that allow to access data
     */
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    /**
     * Informs observers about changed tasks data.
     */
    private void informObserversTasksChanged() {
        for (DataChangeListener listener : observers) {
            listener.tasksChanged();
        }
    }

    /**
     * Informs observers about changed groups ata.
     */
    private void informObserversGroupsChanged() {
        for (DataChangeListener listener : observers) {
            listener.groupsChanged();
        }
    }

    /**
     * Informs observers about changed invitations data.
     */
    private void informObserversInvitationsChanged() {
        for (DataChangeListener listener : observers) {
            listener.invitationsChanged();
        }
    }

    /**
     * Informs observers about refresh finished.
     */
    private void informObserversRefreshFinished() {
        Log.d(TAG, "Refresh finished");
        for (DataChangeListener listener : observers) {
            listener.finished();
        }
    }

    public Boolean isRefreshFinished(){
        return refreshCounter == 0;
    }

    /**
     * Add new observer, that will be informed about data changes.
     *
     * @param listener observer, that implements DataChangeListener
     */
    public void addObserver(DataChangeListener listener) {
        if (!observers.contains(listener))
            observers.add(listener);
    }

    private void decreaseRefreshCounter(){
        refreshCounter--;
        if(refreshCounter == 0) {
            informObserversRefreshFinished();
            if (tasksChanged) {
                informObserversTasksChanged();
                tasksChanged = false;
            }
            if (groupsChanged) {
                informObserversGroupsChanged();
                groupsChanged = false;
            }
            if (invitationsChanged) {
                informObserversInvitationsChanged();
                invitationsChanged = false;
            }
        }
    }

    public List<Group> getGroups() {
        if (groups == null)
            return null;
        return new ArrayList<>(groups.values());
    }

    public Group getGroup(String gid){
        return groups.get(gid);
    }

    /*public UserTask getTask(String tid){
        return tasks.get(tid);
    }*/

    public List<String> getSelectedGroupsIds() {
        return selectedGroupsIds;
    }

    public List<String> getSelectedUsersIds() {
        return selectedUsersIds;
    }

    public List<User> getUsers() {
        if (users == null)
            return null;
        return new ArrayList<>(users.values());
    }

    public Map<String, User> getUsersInMap(){
        return users;
    }

    public List<UserTask> getTasks() {
        if (tasks == null)
            return null;
        return new ArrayList<>(tasks.values());
    }

    public List<UserTask> getFiltratedTasks() {
        if (filtratedTasks == null)
            return getTasks();
        return new ArrayList<>(filtratedTasks.values());
    }

    public List<String> getInvitations() {
        return invitations;
    }

    /**
     * Adds group to selected in filtration, so tasks from this group will be
     * included in filtrated tasks set.
     */
    public void addFiltrationOptionGroup(String gid) {
        for (UserTask task : tasks.values()) {
            if (gid.equals(task.getGroup())) {
                filtratedTasks.put(task.getId(), task);
            }
        }
        selectedGroupsIds.add(gid);
    }

    /**
     * Adds user to selected in filtration, so tasks where this user is mentioned will be
     * included in filtrated tasks set.
     */
    public void addFiltrationOptionUser(String uid) {
        for (UserTask task : filtratedTasks.values()) {
            if (task.getDoers().contains(uid)) {
                filtratedTasks.put(task.getId(), task);
            }
        }
        selectedUsersIds.add(uid);
    }

    /**
     * Removes group from selected in filtration, so tasks from this group will no longer be
     * included in filtrated tasks set.
     */
    public void removeFiltrationOptionGroup(String gid) {
        Map<String, UserTask> newFiltratedTasks = new HashMap<>();
        for (UserTask task : filtratedTasks.values()) {
            if (!gid.equals(task.getGroup())) {
                newFiltratedTasks.put(task.getId(), task);
            }
        }
        filtratedTasks = newFiltratedTasks;
        selectedGroupsIds.remove(gid);
    }

    /**
     * Removes user from selected in filtration, so tasks where user is mentioned will no longer be
     * included in filtrated tasks set.
     */
    public void removeFiltrationOptionUser(String uid) {
        Map<String, UserTask> newFiltratedTasks = new HashMap<>();
        for (UserTask task : filtratedTasks.values()) {
            if (!task.getDoers().contains(uid)) {
                newFiltratedTasks.put(task.getId(), task);
            }
        }
        filtratedTasks = newFiltratedTasks;
        selectedUsersIds.remove(uid);
    }

    /**
     * Prepares maps and lists for new data, refreshes groups, users and tasks.
     */
    public void refresh(String uid) {
        refreshCounter++;
        firestoreRequests.getUser(uid, this::checkUser);
        if (groups == null) {
            groups = new TreeMap<>();
            selectedGroupsIds = new ArrayList<>();
            selectedUsersIds = new ArrayList<>();
            users = new TreeMap<>();
            tasks = new TreeMap<>();
            filtratedTasks = new HashMap<>();
        }
        filtratedTasks.clear();
        groups.clear();
        tasks.clear();

        refreshCounter++;
        firestoreRequests.getGroupByField("members", uid, this::checkGroupsTask);
    }

    /**
     * Prepares list for new data, refreshes invitations.
     */
    public void refreshInvitations(String uid) {
        if (invitations == null) {
            invitations = new ArrayList<>();
        }
        invitations.clear();

        refreshCounter++;
        firestoreRequests.getUser(uid, this::checkAndManageInvitations);
    }

    /**
     * Prepares map and list for new data, refreshes tasks for all groups.
     */
    public void refreshAllGroupsTasks() {
        if (tasks == null) {
            tasks = new TreeMap<>();
            filtratedTasks = new HashMap<>();
        }
        tasks.clear();
        filtratedTasks.clear();
        List<String> gid = new ArrayList<>();

        if (groups == null)
            return;
        for (Group group : groups.values()) {
            gid.add(group.getId());
        }
        for (String id : gid) {
            Log.d(TAG, "Refresh tasks for group: " + id);
            refreshCounter++;
            firestoreRequests.getGroupTasks(id, this::checkTasksTask);
        }
    }

    /**
     * Refresh task for one group.
     */
    private void refreshGroupTasks(String gid) {
        Log.d(TAG, "Refresh tasks for group: " + gid);
        refreshCounter++;
        firestoreRequests.getGroupTasks(gid, this::checkTasksTask);
    }

    /**
     * Check if Firestore task was successful and call function to proceed documents or inform about exception.
     */
    private void checkGroupsTask(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            if (task.getResult().getDocuments().isEmpty()) {
                Log.d(TAG, "No group found");

            } else {
                addGroups(task.getResult().getDocuments());
            }
        } else {
            Exception exception = task.getException();
            if (exception != null)
                Log.d(TAG, exception.getMessage());
        }
        decreaseRefreshCounter();
    }

    /**
     * Check if Firestore task was successful and call function to proceed documents or inform about exception.
     */
    private void checkTasksTask(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            if (!task.getResult().getDocuments().isEmpty()) {
                addTasks(task.getResult().getDocuments());
            }
        } else {
            Exception exception = task.getException();
            if (exception != null)
                Log.d(TAG, exception.getMessage());
        }
        decreaseRefreshCounter();
    }

    /**
     * Proceeds document, checks and adds invitations.
     */
    private void checkAndManageInvitations(DocumentSnapshot documentSnapshot) {
        User user = documentSnapshot.toObject(User.class);
        if (user != null) {
            if (user.getInvitations() != null) {
                invitations = user.getInvitations();
                Log.d(TAG, "Invitations " + invitations.toString());
                invitationsChanged = true;
                //informObserversInvitationsChanged();
            }
        }
        decreaseRefreshCounter();
    }

    /**
     * Proceeds documents, checks and adds groups. Calls functions to refresh tasks and users.
     */
    private void addGroups(List<DocumentSnapshot> documents) {
        for (DocumentSnapshot document : documents) {
            Group group = document.toObject(Group.class);

            if (group != null) {
                group.setId(document.getId());
                groups.put(document.getId(), group);
                selectedGroupsIds.add(group.getId());
                Log.d(TAG, "User group: " + group.getId());
                refreshGroupTasks(group.getId());
                for (String uid : group.getMembers()) {
                    refreshCounter++;
                    firestoreRequests.getUser(uid, this::checkUser);
                }
            }
        }
        //informObserversGroupsChanged();
        groupsChanged = true;
    }

    /**
     * Proceeds documents, checks and adds tasks, apply filtration.
     */
    private void addTasks(List<DocumentSnapshot> documents) {
        for (DocumentSnapshot document : documents) {
            UserTask task = document.toObject(UserTask.class);
            if (task != null) {
                task.setId(document.getId());
                if(!tasks.containsKey(document.getId())) {
                    tasks.put(document.getId(), task);
                    checkIfMatchFiltration(task);
                }
                Log.d(TAG, "Task: " + task.getTitle() + " (" + task.getGroup() + ")");
            }
        }
        //informObserversTasksChanged();
        tasksChanged = true;
    }

    /**
     * Proceeds document, checks and adds user.
     */
    private void checkUser(DocumentSnapshot documentSnapshot) {
        User user = documentSnapshot.toObject(User.class);
        if (user != null) {
            users.put(user.getId(), user);
            if(!selectedUsersIds.contains(user.getId()))
                selectedUsersIds.add(user.getId());
        }
        decreaseRefreshCounter();
    }

    /**
     * Removes group and informs about result.
     */
    public void removeGroup(String group, Activity activity) {
        firestoreRequests.removeGroup(group,
                (aVoid) -> InformUser.inform(activity, R.string.left_group),
                (e) -> InformUser.informFailure(activity, e)
        );
    }

    /**
     * Removes group and informs about result.
     */
    public void removeTask(UserTask task, Activity activity) {
        firestoreRequests.removeTask(task,
                (aVoid) -> InformUser.inform(activity, R.string.task_removed),
                (e) -> InformUser.informFailure(activity, e)
        );
    }

    /**
     * Removes user from group and informs about result.
     */
    public void removeGroupMember(String user, String group, Activity activity) {
        firestoreRequests.removeGroupMember(group,
                user,
                (aVoid) -> InformUser.inform(activity, R.string.left_group),
                (e) -> InformUser.informFailure(activity, e)
        );
    }

    /**
     * Check if task matches filtration and add to filtrated set if so.
     */
    private void checkIfMatchFiltration(UserTask task) {
        if (selectedGroupsIds.contains(task.getGroup())) {
            for(String uid : task.getDoers())
                if(selectedUsersIds.contains(uid)) {
                    filtratedTasks.put(task.getId(), task);
                }
        }
    }
}
