package com.budziaszek.tabmate.view;

public interface GroupsClickListener {
    void onItemLongClicked(int position);
    void onItemClicked(int position);
    void onEditClicked(int position);
    void onLeaveClicked(int position);
}
