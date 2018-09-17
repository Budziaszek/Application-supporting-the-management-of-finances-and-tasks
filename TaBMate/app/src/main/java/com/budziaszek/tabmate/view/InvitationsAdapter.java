package com.budziaszek.tabmate.view;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.firestoreData.Group;

import java.util.List;

public class InvitationsAdapter extends RecyclerView.Adapter<InvitationsAdapter.MyViewHolder> {

    private InvitationClickListener invitationClickListener;
    private List<String> invitationsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView groupName;
        private FloatingActionButton acceptInvitation;
        private FloatingActionButton removeInvitation;

        private MyViewHolder(View view) {
            super(view);
            groupName = view.findViewById(R.id.group_name);

            acceptInvitation = view.findViewById(R.id.accept_button);
            acceptInvitation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    invitationClickListener.onAcceptClicked(getAdapterPosition());
                }
            });

            removeInvitation = view.findViewById(R.id.remove_button);
            removeInvitation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    invitationClickListener.onRemoveClicked(getAdapterPosition());
                }
            });
        }
    }


    public InvitationsAdapter(List<String> invitationsList, InvitationClickListener invitationClickListener) {
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

        FirestoreRequests firestoreRequests = new FirestoreRequests();
        firestoreRequests.getGroup(invitation,
                (DocumentSnapshot) -> holder.groupName.setText(DocumentSnapshot.toObject(Group.class).getName()));
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