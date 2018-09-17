package com.budziaszek.tabmate.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.firestoreData.Task;

import java.util.ArrayList;
import java.util.List;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.MyViewHolder> {

    private TasksClickListener tasksClickListener;
    private List<Task> tasksList;
    //private int selectedItem;
    private ArrayList<RelativeLayout> layoutList = new ArrayList<>();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView taskName;
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
                    return false;
                }
            });
            //if(selectedItem == getAdapterPosition())
            //    taskItemLayout.callOnClick();

            taskName = view.findViewById(R.id.task_title);
        }
    }


    public TasksAdapter(List<Task> groupsList, TasksClickListener tasksClickListener) {
        this.tasksList = groupsList;
        this.tasksClickListener = tasksClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Task task = tasksList.get(position);
        holder.taskName.setText(task.getTitle());
        layoutList.add(holder.taskItemLayout);
    }

    @Override
    public int getItemCount() {
        return tasksList.size();
    }

    public void update(List<Task> data) {
        tasksList.clear();
        tasksList.addAll(data);
        notifyDataSetChanged();
    }
}