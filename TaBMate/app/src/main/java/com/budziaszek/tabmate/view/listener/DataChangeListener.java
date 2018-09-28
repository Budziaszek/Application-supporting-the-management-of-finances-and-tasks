package com.budziaszek.tabmate.view.listener;

public interface DataChangeListener {
    void tasksChanged();
    void groupsChanged();
    void invitationsChanged();

    void groupItemInserted(int position);
    void groupItemRemoved(int position);

}
