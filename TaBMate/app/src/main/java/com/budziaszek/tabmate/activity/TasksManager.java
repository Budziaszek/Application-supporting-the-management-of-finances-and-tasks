package com.budziaszek.tabmate.activity;

import android.app.Activity;
import android.util.Log;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.firestoreData.User;
import com.budziaszek.tabmate.firestoreData.UserTask;
import com.budziaszek.tabmate.view.InformUser;
import com.budziaszek.tabmate.view.ProgressInform;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TasksManager implements ProgressInform {

    private Activity activity;
    private static final String TAG = "TasksManagerProcedure";

    private List<TasksManager> tasks = new ArrayList<>();

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    TasksManager(Activity activity) {
        this.activity = activity;
        //refresh();
    }

    public List<TasksManager> getTasks() {
        return tasks;
    }

    @Override
    public void informInProgress(Boolean isInProgress) {
        ((MainActivity) activity).informInProgress(isInProgress);
    }

    public void refreshGroupsAndUsers() {
        tasks = new ArrayList<>();
        //firestoreRequests.getTaskByField("", ((MainActivity)activity).getCurrentUserId(), this::checkTasks);
    }


    private void checkTasks(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            if (task.getResult().getDocuments().isEmpty()) {
                Log.d(TAG, "No task found");
                informInProgress(false);
            } else {
                addTasks(task.getResult().getDocuments());
            }
        } else {
            Exception exception = task.getException();
            if (exception != null)
                InformUser.informFailure(activity, exception);
            informInProgress(false);
        }
    }

    private void addTasks(List<DocumentSnapshot> documents) {
        for (DocumentSnapshot document : documents) {
            UserTask userTask = document.toObject(UserTask.class);
            if (userTask != null) {
                Log.d(TAG, "User task: " + userTask.getTitle());

            }
        }
        informInProgress(false);
    }

}
