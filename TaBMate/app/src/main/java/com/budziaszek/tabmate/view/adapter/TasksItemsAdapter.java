package com.budziaszek.tabmate.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.firestoreData.DataManager;
import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.firestoreData.UserTask;
import com.budziaszek.tabmate.view.listener.TaskClickListener;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class TasksItemsAdapter extends RecyclerView.Adapter<TasksItemsAdapter.MyViewHolder> {

    private TaskClickListener taskClickListener;
    private List<UserTask> tasksList;
    private Animation animation;
    private Context context;
    private int color;
    private int deadlineVisible = View.GONE;
    private int preDeadlineTime = 3;
    private String uid;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView taskName;
        private TextView taskDescription;
        private TextView taskDeadline;
        private Switch taskSwitch;
        private RelativeLayout taskItemLayout;

        private MyViewHolder(View view) {
            super(view);

            animation = AnimationUtils.loadAnimation(context, R.anim.blinking_animation);

            taskItemLayout = view.findViewById(R.id.task_item_layout);
            taskItemLayout.setOnClickListener(view12 -> taskClickListener.onClick(getAdapterPosition()));
            taskItemLayout.setOnLongClickListener(view1 -> {
                taskClickListener.onLongClick(getAdapterPosition());
                return true;
            });
            taskItemLayout.setBackground(context.getResources().getDrawable(color, context.getTheme()));

            taskName = view.findViewById(R.id.task_title);
            taskDescription = view.findViewById(R.id.task_description);
            taskDeadline = view.findViewById(R.id.task_date);
            taskSwitch = view.findViewById(R.id.switch_play);
        }
    }


    public TasksItemsAdapter(List<UserTask> tasksList, Context context, int color, TaskClickListener taskClickListener, String uid) {
        //this.taskItemLayouts = new ArrayList<>();
        this.tasksList = tasksList;
        this.taskClickListener = taskClickListener;
        this.context = context;
        this.color = color;
        this.uid = uid;
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
        holder.taskDeadline.setText(task.dateString(true));

        if(task.getPlayDate() != null)
            holder.taskSwitch.setChecked(true);
        else
            holder.taskSwitch.setChecked(false);

        holder.taskSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                task.play();
            }else{
                task.stop();
            }
            FirestoreRequests firestoreRequests = new FirestoreRequests();
            firestoreRequests.updateTask(task, v -> {}, e -> Log.d("Error", e.getMessage()));
        });


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
        if(task.getStatus() != UserTask.Status.DOING || !task.getDoers().contains(uid)){
            holder.taskSwitch.setVisibility(View.INVISIBLE);
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
        if (tasksList != null)
            return tasksList.size();
        return 0;
    }

    public void updateAll(List<UserTask> data){
        tasksList = data;
        notifyDataSetChanged();
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