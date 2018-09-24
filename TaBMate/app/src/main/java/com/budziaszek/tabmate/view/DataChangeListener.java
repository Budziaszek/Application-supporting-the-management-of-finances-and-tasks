package com.budziaszek.tabmate.view;

import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.firestoreData.User;
import com.budziaszek.tabmate.firestoreData.UserTask;

public interface DataChangeListener {

    /*void tasksCleaned();
    void taskAdded(UserTask task);

    void groupsCleaned();
    void groupAdded(Group group);*/
    void tasksChanged();
    void groupsChanged();
    void invitationsChanged();

}
