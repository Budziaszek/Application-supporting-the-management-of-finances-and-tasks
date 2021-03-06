package com.budziaszek.tabmate.data;

import android.app.Activity;
import android.util.Log;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.view.listener.DataChangeListener;
import com.budziaszek.tabmate.view.helper.InformUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Singleton class that gets data from Firestore, refresh it, filtrate and allow to access it.
 **/
public class DataManager {

    private final static String TAG = "DataManagerProcedure";

    private static DataManager instance;
    private Boolean dataHasChanged = false;

    private int refreshCounter = 0;

    // Observers, that will be informed about data changes
    private List<DataChangeListener> observers = new ArrayList<>();
    private Boolean groupsChanged = false;
    private Boolean tasksChanged = false;
    private Boolean transactionsChanged = false;

    // All data from database
    private Map<String, Group> groups;
    private Map<String, User> users;
    private Map<String, Task> tasks;
    private List<String> invitations;
    private Map<String, Transaction> transactions;

    // Filtrated data and filters
    private Map<String, Task> filtratedTasks;
    private Set<String> selectedGroupsIds;
    private Set<String> selectedUsersIds;
    //private Set<String> selectedCategories;
    private Date selectedDate;
    private Boolean isUserUnspecifiedSelected = true;

    private DataManager() {

    }

    public void clear() {
        dataHasChanged = false;
        refreshCounter = 0;
        //observers.clear();
        groupsChanged = false;
        tasksChanged = false;
        //invitationsChanged = false;
        transactionsChanged = false;
        groups = null;
        users = null;
        tasks = null;
        invitations = null;
        transactions = null;
        filtratedTasks = null;
        selectedGroupsIds = null;
        selectedUsersIds = null;
        isUserUnspecifiedSelected = true;
    }

//    public void setSelectedCategories(Set<String> selectedCategories) {
//        this.selectedCategories = selectedCategories;
//    }

    public Boolean getDataHasChanged() {
        return dataHasChanged;
    }

    public void setDataHasChanged(Boolean hasChanged) {
        dataHasChanged = hasChanged;
        if (dataHasChanged) {
            for (DataChangeListener listener : observers) {
                listener.informAboutDataSynchronization();
            }
        }
    }

    /**
     * Creates class instance if it wasn't created before and returns (singleton) instance.
     *
     * @return instance, that allow to access data
     */
    public static DataManager getInstance() {
        if (instance == null) {
            synchronized (DataManager.class) {
                if (instance == null) {
                    instance = new DataManager();
                }
            }
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
     * Informs observers about changed transactions data.
     */
    private void informObserversTransactionsChanged() {
        for (DataChangeListener listener : observers) {
            listener.transactionsChanged();
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
     * Informs observers about refresh refreshFinished.
     */
    private void informObserversRefreshFinished() {
        Log.d(TAG, "Refresh refreshFinished");
        for (DataChangeListener listener : observers) {
            listener.refreshFinished();
        }
    }

    public Boolean isRefreshFinished() {
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

    private void decreaseRefreshCounter() {
        refreshCounter--;
        if (refreshCounter == 0) {
            for (Task task : tasks.values()) {
                checkIfMatchFiltration(task);
            }
            informObserversRefreshFinished();

            for (DataChangeListener listener : observers) {
                Log.d("SyncProcedure", "Stop");
                DataManager.getInstance().setDataHasChanged(false);
                listener.informAboutDataSynchronization();
            }

            if (tasksChanged) {
                informObserversTasksChanged();
                tasksChanged = false;
            }
            if (groupsChanged) {
                informObserversGroupsChanged();
                groupsChanged = false;
            }
            if (transactionsChanged) {
                informObserversTransactionsChanged();
                transactionsChanged = false;
            }
        }
    }

    public List<Group> getGroups() {
        if (groups == null)
            return null;
        return new ArrayList<>(groups.values());
    }

    public Group getGroup(String gid) {
        if (gid != null)
            return groups.get(gid);
        return null;
    }

    /*public Task getTask(String tid){
        return tasks.get(tid);
    }*/

    public List<String> getSelectedGroupsIds() {
        return new ArrayList<>(selectedGroupsIds);
    }

    public List<String> getSelectedUsersIds() {
        return new ArrayList<>(selectedUsersIds);
    }

    public List<User> getUsers() {
        if (users == null)
            return null;
        return new ArrayList<>(users.values());
    }

    public List<User> getSelectedUsers() {
        if (users == null)
            return null;
        List<User> selected = new ArrayList<>();
        for (User user : users.values()) {
            if (selectedUsersIds.contains(user.getId()))
                selected.add(user);
        }
        return selected;
    }

//    public void removeSelectedCategory(String category){
//        selectedCategories.remove(category);
//    }
//
//    public void addSelectedCategory(String category){
//        selectedCategories.add(category);
//    }
//
//    public Set<String> getSelectedCategories() {
//        if(selectedCategories == null)
//            return new HashSet<>();
//        return selectedCategories;
//    }

    public Map<String, User> getUsersInMap() {
        return users;
    }

    public List<Task> getTasks() {
        if (tasks == null)
            return null;
        return new ArrayList<>(tasks.values());
    }

    public List<Transaction> getTransactions() {
        if (transactions == null)
            return null;
        return new ArrayList<>(transactions.values());
    }

    public List<Transaction> getFiltratedTransactions() {
        if (transactions == null)
            return null;
        List<Transaction> filtratedTransactions = new ArrayList<>();
        for (Transaction transaction : transactions.values()) {
            if (selectedGroupsIds.contains(transaction.getGroup()))
                if (selectedUsersIds.contains(transaction.getUid())
                        || (transaction.getUid() == null && isUserUnspecifiedSelected))
                    filtratedTransactions.add(transaction);
        }
        return filtratedTransactions;
    }

    public List<Task> getFiltratedTasks() {
        if (filtratedTasks == null)
            return getTasks();
        return new ArrayList<>(filtratedTasks.values());
    }

    public List<String> getInvitations() {
        return invitations;
    }

    public Boolean getUserUnspecifiedSelected() {
        return isUserUnspecifiedSelected;
    }

    /**
     * Changes display unspecified selected users in filtration.
     */
    public void setUserUnspecifiedSelected(Boolean userUnspecifiedSelected) {
        isUserUnspecifiedSelected = userUnspecifiedSelected;
        refreshFiltratedTasks();
    }

    /**
     * Adds group to selected in filtration.
     */
    public void addFiltrationOptionGroup(String gid) {
        selectedGroupsIds.add(gid);
        refreshFiltratedTasks();
    }

    /**
     * Adds user to selected in filtration.
     */
    public void addFiltrationOptionUser(String uid) {
        selectedUsersIds.add(uid);
        refreshFiltratedTasks();
    }

    /**
     * Removes group from selected in filtration.
     */
    public void removeFiltrationOptionGroup(String gid) {
        selectedGroupsIds.remove(gid);
        refreshFiltratedTasks();
    }

    /**
     * Removes user from selected in filtration, so tasks where user is mentioned will no longer be
     * included in filtrated tasks set.
     */
    public void removeFiltrationOptionUser(String uid) {
        selectedUsersIds.remove(uid);
        refreshFiltratedTasks();
    }

    //TODO refresh on close filtration
    private void refreshFiltratedTasks() {
        filtratedTasks.clear();
        for (Task task : tasks.values()) {
            checkIfMatchFiltration(task);
        }
        informObserversTasksChanged();
    }


    /**
     * Prepares maps and lists for new data, refreshes groups, users and tasks.
     */
    public void refresh(String uid) {
        refreshCounter++;
        FirestoreRequests.getUser(uid, this::checkUser);
        if (groups == null) {
            groups = new TreeMap<>();
            selectedGroupsIds = new HashSet<>();
            selectedUsersIds = new HashSet<>();
            users = new TreeMap<>();
            tasks = new TreeMap<>();
            transactions = new TreeMap<>();
            filtratedTasks = new HashMap<>();
        }
        filtratedTasks.clear();
        groups.clear();
        tasks.clear();
        transactions.clear();

        refreshCounter++;
        FirestoreRequests.getGroup("members", uid, this::checkGroupsTask);
    }

    /**
     * Prepares list for new data, refreshes invitations.
     */
    public void refreshInvitations(String uid) {
        if (invitations == null) {
            invitations = new ArrayList<>();
        }
        invitations.clear();
        FirestoreRequests.getUser(uid, this::checkAndManageInvitations);
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
            FirestoreRequests.getTasks(id, this::checkTasksTask);
        }
    }

    /**
     * Refresh task for one group.
     */
    private void refreshGroupTasks(String gid) {
        Log.d(TAG, "Refresh tasks for group: " + gid);
        refreshCounter++;
        FirestoreRequests.getTasks(gid, this::checkTasksTask);
    }

    /**
     * Check if Firestore task was successful and call function to proceed documents or inform about exception.
     */
    private void checkGroupsTask(com.google.android.gms.tasks.Task<QuerySnapshot> task) {
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
    private void checkTasksTask(com.google.android.gms.tasks.Task<QuerySnapshot> task) {
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
     * Check if Firestore task was successful and call function to proceed documents or inform about exception.
     */
    private void checkTransactions(com.google.android.gms.tasks.Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            if (!task.getResult().getDocuments().isEmpty()) {
                addTransactions(task.getResult().getDocuments());
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
                //invitationsChanged = true;
                informObserversInvitationsChanged();
            }
        }
        //decreaseRefreshCounter();
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
                //addFiltrationOptionGroup(group.getId());
                Log.d(TAG, "User group: " + group.getId());
                refreshGroupTasks(group.getId());
                for (String uid : group.getMembers()) {
                    refreshCounter++;
                    FirestoreRequests.getUser(uid, this::checkUser);
                }
                refreshCounter++;
                FirestoreRequests.getTransactions(group.getId(), this::checkTransactions);
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
            Task task = document.toObject(Task.class);
            if (task != null) {
                task.setId(document.getId());
                if (!tasks.containsKey(document.getId())) {
                    tasks.put(document.getId(), task);
                    //checkIfMatchFiltration(task);
                }
                Log.d(TAG, "Task: " + task.getTitle() + " (" + task.getGroup() + ")");
            }
        }
        //informObserversTasksChanged();
        tasksChanged = true;
    }

    private void addTransactions(List<DocumentSnapshot> documents) {
        for (DocumentSnapshot document : documents) {
            Transaction transaction = document.toObject(Transaction.class);
            if (transaction != null) {
                if (!transactions.containsKey(document.getId())) {
                    transactions.put(document.getId(), transaction);
                    //checkIfMatchFiltration(task);
                }
                Log.d(TAG, "Transaction: " + transaction.getTitle() + " (" + transaction.getGroup() + ")");
            }
        }
        //informObserversTasksChanged();
        transactionsChanged = true;
    }

    /**
     * Proceeds document, checks and adds user.
     */
    private void checkUser(DocumentSnapshot documentSnapshot) {
        User user = documentSnapshot.toObject(User.class);
        if (user != null) {
            users.put(user.getId(), user);
            if (!selectedUsersIds.contains(user.getId()))
                selectedUsersIds.add(user.getId());
            Log.d(TAG, "User: " + user.getId() + " (" + user.getName() + ")");
        }
        decreaseRefreshCounter();
    }

    /**
     * Removes group and informs about result.
     */
    public void removeGroup(String group, Activity activity) {
        FirestoreRequests.removeGroup(group,
                (aVoid) -> InformUser.inform(activity, R.string.left_group),
                (e) -> InformUser.informFailure(activity, e)
        );
    }

    /**
     * Removes group and informs about result.
     */
    public void removeTask(Task task, Activity activity) {
        FirestoreRequests.removeTask(task,
                (aVoid) -> InformUser.inform(activity, R.string.task_removed),
                (e) -> InformUser.informFailure(activity, e)
        );
    }

    /**
     * Removes user from group and informs about result.
     */
    public void removeGroupMember(String user, String group, Activity activity) {
        FirestoreRequests.removeGroupMember(group,
                user,
                (aVoid) -> InformUser.inform(activity, R.string.left_group),
                (e) -> InformUser.informFailure(activity, e)
        );
    }

    /**
     * Check if task matches filtration and add to filtrated set if so.
     */
    private void checkIfMatchFiltration(Task task) {
        if (selectedGroupsIds.contains(task.getGroup())) {
            if (task.getDoers().size() == 0 && isUserUnspecifiedSelected) {
                filtratedTasks.put(task.getId(), task);
                return;
            }
            for (String uid : task.getDoers())
                if (selectedUsersIds.contains(uid)) {
                    filtratedTasks.put(task.getId(), task);
                    return;
                }
        }
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }
}
