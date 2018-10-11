package com.budziaszek.tabmate.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.firestoreData.UserTask;
import com.budziaszek.tabmate.view.listener.TasksClickListener;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class TasksItemsAdapter extends RecyclerView.Adapter<TasksItemsAdapter.MyViewHolder> {

    private TasksClickListener tasksClickListener;
    private List<UserTask> tasksList;
    private Animation animation;
    private Context context;
    private int color;
    private int deadlineVisible = View.GONE;
    private int preDeadlineTime = 3;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView taskName;
        private TextView taskDescription;
        private TextView taskDeadline;
        private RelativeLayout taskItemLayout;

        private MyViewHolder(View view) {
            super(view);

            animation = AnimationUtils.loadAnimation(context, R.anim.blinking_animation);

            taskItemLayout = view.findViewById(R.id.task_item_layout);
            taskItemLayout.setOnClickListener(view12 -> tasksClickListener.onClick(getAdapterPosition()));
            taskItemLayout.setOnLongClickListener(view1 -> {
                tasksClickListener.onLongClick(getAdapterPosition());
                return true;
            });
            taskItemLayout.setBackground(context.getResources().getDrawable(color, context.getTheme()));

            taskName = view.findViewById(R.id.task_title);
            taskDescription = view.findViewById(R.id.nick);
            taskDeadline = view.findViewById(R.id.task_deadline);
        }
    }


    public TasksItemsAdapter(List<UserTask> groupsList, Context context, int color, TasksClickListener tasksClickListener) {
        //this.taskItemLayouts = new ArrayList<>();
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

    @SuppressLint("WrongConstant")
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        UserTask task = tasksList.get(position);
        holder.taskName.setText(task.getTitle());
        holder.taskDeadline.setText(task.getDateString());

        //Check deadline
        Calendar calendar = Calendar.getInstance();

        holder.taskDeadline.setVisibility(deadlineVisible);
        holder.taskDeadline.setTextColor(Color.GREEN);
        holder.taskDeadline.clearAnimation();
        if (task.getStatus() != UserTask.Status.ARCHIVED && task.getStatus() != UserTask.Status.DONE && task.getDate() != null) {
            if ((calendar.getTime().after(task.getDate()))) {
                holder.taskDeadline.setVisibility(View.VISIBLE);
                holder.taskDeadline.setTextColor(Color.RED);
                holder.taskDeadline.startAnimation(animation);
            } else if (calendar.getTime().after(new Date(task.getDate().getTime() - (preDeadlineTime * 86400 * 1000)))) {
                holder.taskDeadline.setVisibility(View.VISIBLE);
                holder.taskDeadline.setTextColor(Color.rgb(255, 165, 0));
                holder.taskDeadline.startAnimation(animation);
            }
        }

        String description = task.getDescription();
        if (!description.equals("")) {
            holder.taskDescription.setText(description);
            holder.taskDescription.setVisibility(View.VISIBLE);
        } else {
            holder.taskDescription.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return tasksList.size();
    }

    public void update(List<UserTask> data) {
        tasksList = data;
    }

    public void setDeadlineVisible(int visibility) {
        deadlineVisible = visibility;
    }

    public void setPreDeadlineTime(int time) {
        preDeadlineTime = time;
    }
}