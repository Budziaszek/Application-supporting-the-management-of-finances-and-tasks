package com.budziaszek.tabmate.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.firestoreData.UserTask;
import com.budziaszek.tabmate.view.listener.TasksClickListener;

import java.util.ArrayList;
import java.util.List;

public class TasksItemsAdapter extends RecyclerView.Adapter<TasksItemsAdapter.MyViewHolder> {

    private TasksClickListener tasksClickListener;
    private List<UserTask> tasksList;
    //private int selectedItem;
    private Context context;
    private int color;
    private ArrayList<RelativeLayout> layoutList = new ArrayList<>();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView taskName;
        private TextView taskDescription;
        private RelativeLayout taskItemLayout;

        private MyViewHolder(View view) {
            super(view);

            taskItemLayout = view.findViewById(R.id.task_item_layout);
            taskItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tasksClickListener.onClick(getAdapterPosition());
                }
            });
            taskItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    tasksClickListener.onLongClick(getAdapterPosition());
                    return true;
                }
            });
            taskItemLayout.setBackground(context.getResources().getDrawable(color, context.getTheme()));

            taskName = view.findViewById(R.id.task_title);
            taskDescription = view.findViewById(R.id.task_description);
        }
    }


    public TasksItemsAdapter(List<UserTask> groupsList, Context context, int color, TasksClickListener tasksClickListener) {
        this.tasksList = groupsList;
        this.tasksClickListener = tasksClickListener;
        this.context = context;
        this.color = color;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        UserTask task = tasksList.get(position);
        holder.taskName.setText(task.getTitle());

        String description = task.getDescription();
        if(!description.equals("")) {
            holder.taskDescription.setText(description);
            holder.taskDescription.setVisibility(View.VISIBLE);
        }
        else{
            holder.taskDescription.setVisibility(View.GONE);
        }
        layoutList.add(holder.taskItemLayout);
    }

    @Override
    public int getItemCount() {
        return tasksList.size();
    }

    public void update(List<UserTask> data) {
        tasksList.clear();
        tasksList.addAll(data);
        notifyDataSetChanged();
    }
}