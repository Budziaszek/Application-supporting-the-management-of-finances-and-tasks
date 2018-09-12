package com.budziaszek.tabmate;

import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class InvitationsAdapter extends RecyclerView.Adapter<InvitationsAdapter.MyViewHolder> {

    ClickListener clickListener;
    private List<String> invitationsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView group_name;
        public FloatingActionButton accept_invitation;
        public FloatingActionButton remove_invitation;

        public MyViewHolder(View view) {
            super(view);
            group_name = view.findViewById(R.id.invitations_group_name);

            accept_invitation = view.findViewById(R.id.accept_button);
            accept_invitation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onAcceptClicked(getAdapterPosition());
                }
            });

            remove_invitation = view.findViewById(R.id.remove_button);
            remove_invitation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onRemoveClicked(getAdapterPosition());
                }
            });
        }
    }


    public InvitationsAdapter(List<String> invitationsList, ClickListener clickListener) {
        this.invitationsList = invitationsList;
        this.clickListener = clickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.invitation_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String invitation = invitationsList.get(position);
        holder.group_name.setText(invitation);
    }

    @Override
    public int getItemCount() {
        return invitationsList.size();
    }
}