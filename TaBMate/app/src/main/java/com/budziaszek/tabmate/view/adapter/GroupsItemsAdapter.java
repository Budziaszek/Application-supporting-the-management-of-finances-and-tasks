package com.budziaszek.tabmate.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.view.listener.GroupsClickListener;

import java.util.ArrayList;
import java.util.List;

public class GroupsItemsAdapter extends RecyclerView.Adapter<GroupsItemsAdapter.MyViewHolder> {

    private GroupsClickListener groupsClickListener;
    private List<Group> groupsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView groupName;
        private RelativeLayout groupItemLayout;

        public MyViewHolder(View view) {
            super(view);

            groupItemLayout = view.findViewById(R.id.group_item_layout);
            groupItemLayout.setOnClickListener(view1 ->
                    groupsClickListener.onItemClicked(getAdapterPosition()));
            groupItemLayout.setOnLongClickListener(view12 -> {
                groupsClickListener.onItemLongClicked(getAdapterPosition());
                return true;
            });

            groupName = view.findViewById(R.id.group_name);
        }
    }

    public GroupsItemsAdapter(List<Group> groupsList, GroupsClickListener groupsClickListener) {
        this.groupsList = groupsList;
        this.groupsClickListener = groupsClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Group group = groupsList.get(position);
        holder.groupName.setText(group.getName());
        //layoutList.add(holder.groupItemLayout);
    }

    @Override
    public int getItemCount() {
        if(groupsList == null)
            return 0;
        return groupsList.size();
    }

    public void update(List<Group> data) {
        if(groupsList == null)
            return;
        groupsList.clear();
        groupsList.addAll(data);
        notifyDataSetChanged();
    }

    public void addItem(int position, Group group) {
        if(groupsList == null)
            return;
        groupsList.add(position, group);
        notifyItemInserted(position);
    }

    public void removeItem(int position){
        if(groupsList == null)
            return;
        groupsList.remove(position);
        notifyItemRemoved(position);
    }
}