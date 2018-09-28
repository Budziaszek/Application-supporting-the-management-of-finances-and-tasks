package com.budziaszek.tabmate.view.adapter;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.firestoreData.User;
import com.budziaszek.tabmate.view.listener.MemberClickListener;

import java.util.List;

public class MembersItemsAdapter extends RecyclerView.Adapter<MembersItemsAdapter.MyViewHolder> {

    private MemberClickListener clickListener;
    private List<User> usersList;
    private String currentUser;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView email;
        private FloatingActionButton leaveButton;

        private MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.group_name);
            email = view.findViewById(R.id.email_item);
            leaveButton = view.findViewById(R.id.leave_button);
            leaveButton.setOnClickListener(view1 -> clickListener.onLeaveClicked(getAdapterPosition()));
        }
    }

    public MembersItemsAdapter(List<User> usersList, MemberClickListener clickListener, String currentUser) {
        this.usersList = usersList;
        this.clickListener = clickListener;
        this.currentUser = currentUser;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User user = usersList.get(position);
        holder.title.setText(user.getName());
        holder.email.setText(user.getEmail());
        if(user.getId().equals(currentUser)){
            holder.leaveButton.setVisibility(View.VISIBLE);
        }else{
            holder.leaveButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public void update(List<User> data) {
        usersList.clear();
        usersList.addAll(data);
        notifyDataSetChanged();
    }
}