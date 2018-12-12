package com.budziaszek.tabmate.view.listener;

public interface DataChangeListener {
    void tasksChanged();
    void groupsChanged();
    void invitationsChanged();
    void transactionsChanged();
    void finished();
    void informAboutDataSynchronization();
}
