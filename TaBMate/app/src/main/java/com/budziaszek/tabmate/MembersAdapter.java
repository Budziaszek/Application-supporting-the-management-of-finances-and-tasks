package com.budziaszek.tabmate;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MyViewHolder> {

    private List<User> usersList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView email;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            email = view.findViewById(R.id.email_item);
        }
    }

    public MembersAdapter(List<User> usersList) {
        this.usersList = usersList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.member_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User user = usersList.get(position);
        holder.title.setText(user.getName());
        holder.email.setText(user.getEmail());
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