package com.budziaszek.tabmate.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.view.listener.GroupClickListener;
import com.google.protobuf.StringValue;

import java.util.List;

public class GroupsItemsAdapter extends RecyclerView.Adapter<GroupsItemsAdapter.MyViewHolder> {

    private GroupClickListener groupClickListener;
    private List<Group> groupsList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView groupName;
        private TextView groupBudget;
        private RelativeLayout groupItemLayout;

        private MyViewHolder(View view) {
            super(view);

            groupItemLayout = view.findViewById(R.id.group_item_layout);
            groupItemLayout.setOnClickListener(view1 ->
                groupClickListener.onItemClicked(getAdapterPosition()));
            groupItemLayout.setOnLongClickListener(view12 -> {
                groupClickListener.onItemLongClicked(getAdapterPosition());
                return true;
            });

            groupName = view.findViewById(R.id.group_name);
            groupBudget = view.findViewById(R.id.group_budget);

            view.findViewById(R.id.group_budget_button).setOnClickListener(view12 -> {
                groupClickListener.onButtonClicked(getAdapterPosition());
            });
        }
    }


    public GroupsItemsAdapter(List<Group> groupsList, GroupClickListener groupClickListener) {
        this.groupsList = groupsList;
        this.groupClickListener = groupClickListener;
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
        holder.groupBudget.setText(group.getStringBudgetBalance());
    }

    @Override
    public int getItemCount() {
        return groupsList.size();
    }

    public void update(List<Group> data) {
        groupsList = data;
    }
}