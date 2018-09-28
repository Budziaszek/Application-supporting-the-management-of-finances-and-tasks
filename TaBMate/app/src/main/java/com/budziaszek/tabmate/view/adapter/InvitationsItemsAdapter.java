package com.budziaszek.tabmate.view.adapter;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.view.listener.InvitationClickListener;

import java.util.List;

public class InvitationsItemsAdapter extends RecyclerView.Adapter<InvitationsItemsAdapter.MyViewHolder> {

    private FirestoreRequests firestoreRequests = new FirestoreRequests();
    private InvitationClickListener invitationClickListener;
    private List<String> invitationsList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView groupName;
        private FloatingActionButton acceptInvitation;
        private FloatingActionButton removeInvitation;

        private MyViewHolder(View view) {
            super(view);
            groupName = view.findViewById(R.id.group_name);

            acceptInvitation = view.findViewById(R.id.accept_button);
            acceptInvitation.setOnClickListener(view1 -> invitationClickListener.onAcceptClicked(getAdapterPosition()));

            removeInvitation = view.findViewById(R.id.remove_button);
            removeInvitation.setOnClickListener(view12 -> invitationClickListener.onRemoveClicked(getAdapterPosition()));
        }
    }


    public InvitationsItemsAdapter(List<String> invitationsList, InvitationClickListener invitationClickListener) {
        this.invitationsList = invitationsList;
        this.invitationClickListener = invitationClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invitation, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String invitation = invitationsList.get(position);

        firestoreRequests.getGroup(invitation,
                (DocumentSnapshot) -> {
                        Group group = DocumentSnapshot.toObject(Group.class);
                        if(group != null){
                            holder.groupName.setText(group.getName());
                        }
                });
    }

    @Override
    public int getItemCount() {
        return invitationsList.size();
    }

    public void update(List<String> data) {
        invitationsList.clear();
        invitationsList.addAll(data);
        notifyDataSetChanged();
    }
}