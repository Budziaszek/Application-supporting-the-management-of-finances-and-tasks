package com.budziaszek.tabmate.activity;

import android.app.Activity;
import android.util.Log;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.firestoreData.User;
import com.budziaszek.tabmate.view.InformUser;
import com.budziaszek.tabmate.view.ProgressInform;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GroupsManager implements ProgressInform {

    private Activity activity;
    private static final String TAG = "GroupsManagerProcedure";

    private List<Group> groups = new ArrayList<>();
    private Map<String, User> users = new TreeMap<>();

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    GroupsManager(Activity activity){
        this.activity = activity;
        //refresh();
    }

    public List<Group> getGroups() {
        return groups;
    }

    public Map<String, User> getUsers() {
        return users;
    }

    @Override
    public void informInProgress(Boolean isInProgress){
        ((MainActivity)activity).informInProgress(isInProgress);
    }

    public void refresh(){
        groups = new ArrayList<>();
        firestoreRequests.getGroupByField("members", ((MainActivity)activity).getCurrentUserId(), this::checkGroups);
    }

    public void removeGroupMember(String user, String group){
        firestoreRequests.removeGroupMember(group,
                user,
                (aVoid) -> {
                    InformUser.inform(activity, R.string.left_group);
                    informInProgress(true);
                    refresh();
                },
                (e) -> InformUser.informFailure(activity, e)
        );
    }

    public void removeGroup(String group){
        firestoreRequests.removeGroup(group,
                (aVoid) -> {
                    InformUser.inform(activity, R.string.left_group);
                    informInProgress(true);
                    refresh();
                },
                (e) -> InformUser.informFailure(activity, e)
        );
    }

    private void checkGroups(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            if(task.getResult().getDocuments().isEmpty()){
                Log.d(TAG, "No group found");
            }
            else{
                addGroups(task.getResult().getDocuments());
            }
        } else {
            Exception exception = task.getException();
            if(exception != null)
                InformUser.informFailure(activity, exception);
        }
    }

    private void addGroups(List<DocumentSnapshot> documents){
        for (DocumentSnapshot document : documents) {
            Group group = document.toObject(Group.class);
            groups.add(group);
            if(group!=null) {
                Log.d(TAG, "User group: " + group.getId());

                for (String uid : group.getMembers()) {
                    firestoreRequests.getUser(uid, this::addUserToList);
                }
            }
        }
        informInProgress(false);
    }

    private void addUserToList(DocumentSnapshot documentSnapshot) {
        User user = documentSnapshot.toObject(User.class);
        if (user != null){
            users.put(user.getId(), user);
        }
    }
}
