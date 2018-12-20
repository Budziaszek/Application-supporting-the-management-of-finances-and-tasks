package com.budziaszek.tabmate.firestoreData;

import android.annotation.SuppressLint;
import android.util.Log;

import com.budziaszek.tabmate.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class UserTask {

    private String id;
    private String title;
    private String description;
    private String group;
    private ArrayList<String> doers;
    private Status status;
    private Status statusBeforeArchive;
    private Date date;
    private Date completionDate;
    private Map<String, Integer> timeEstimationVote;
    private Integer estimatedTime = 0;
    private Long timeSpent = (long)0.0;
    private Integer priority;
    private Date playDate;
    private Map<String, Boolean> subtasks = new HashMap<>();
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private Map<String, Long> timeSpentByDate = new HashMap<>();

    public enum Status {

        TODO(0, "ToDo", R.drawable.ripple_effect_todo),
        DOING(1, "Doing",  R.drawable.ripple_effect_doing),
        DONE(2, "Done",  R.drawable.ripple_effect_done),
        ARCHIVED(3, "Archived",  R.drawable.ripple_effect_archived);

        public int status;
        public String name;
        public int color;

        Status(int status, String name, int color) {
            this.status = status;
            this.name = name;
            this.color = color;
        }
    }

    public static Status getNextStatus(Status status){
        if(status == Status.TODO)
            return Status.DOING;
        if(status == Status.DOING)
            return Status.DONE;
        if(status == Status.DONE)
            return Status.ARCHIVED;
        if(status == Status.ARCHIVED)
            return Status.TODO;
        else
            return Status.TODO;
    }

    public void setNextStatus(){
        if(status == Status.TODO )
            stop();
        if(status == Status.TODO)
            status = Status.DOING;
        else if(status == Status.DOING)
            status = Status.DONE;
        else if(status == Status.DONE)
            setArchived();
        else if(status == Status.ARCHIVED)
            status = Status.TODO;
        else
            status = Status.TODO;
        playDate = null;
        if(status == Status.DONE){
            setCompletionDate(Calendar.getInstance().getTime());
        }
    }

    public void setArchived(){
        statusBeforeArchive = status;
        status = Status.ARCHIVED;
    }

    public void setId(String id){
        this.id = id;
    }

    public void setEstimatedTime(Integer estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public void setDoers(ArrayList<String> doers) {
        this.doers = doers;
    }

    public void setStatusBeforeArchive(Status statusBeforeArchive) {
        this.statusBeforeArchive = statusBeforeArchive;
    }

    public void setTimeEstimationVote(Map<String, Integer> timeEstimationVote) {
        this.timeEstimationVote = timeEstimationVote;
    }

    public void setTimeSpent(Long timeSpent) {
        this.timeSpent = timeSpent;
    }

    public void setPlayDate(Date playDate) {
        this.playDate = playDate;
    }

    public void setSubtasks(Map<String, Boolean> subtasks) {
        this.subtasks = subtasks;
    }

    public void putSubtask(String subtask, Boolean isDone){
        subtasks.put(subtask, isDone);
    }

    public void setTimeSpentByDate(Map<String, Long> timeSpentByDate) {
        this.timeSpentByDate = timeSpentByDate;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public Map<String, Integer> getTimeEstimationVote() {
        return timeEstimationVote;
    }

    public Integer getEstimatedTime() {
        return estimatedTime;
    }

    public String getId(){
        return id;
    }

    public UserTask(){
        this.title = null;
        this.description = null;
        this.group = null;
        this.status = Status.TODO;
        this.doers = new ArrayList<>();
        this.timeEstimationVote = new TreeMap<>();
        this.priority = 5;
        this.estimatedTime = 0;
        this.timeSpent = (long)0.0;
    }

    public UserTask(String title, String description, String group) {
        this.title = title;
        this.description = description;
        this.group = group;
        this.status = Status.TODO;
        this.doers = new ArrayList<>();
        this.timeEstimationVote = new TreeMap<>();
        this.priority = 5;
        this.estimatedTime = 0;
        this.timeSpent = (long)0.0;
    }

    public UserTask(String id, String title, String description, String group, /*ArrayList<String> tag,*/ ArrayList<String> doers, Status status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.group = group;
        this.doers = new ArrayList<>();
        this.timeEstimationVote = new TreeMap<>();
        this.priority = 5;
        this.doers = doers;
        this.status = status;
    }

    public void addDoer(String id){
        if(!this.doers.contains(id))
            this.doers.add(id);
    }

    public void addTimeEstimationVote(String uid, Integer time){
        timeEstimationVote.put(uid, time);
        estimatedTime = 0;
        for(Integer timeVote : timeEstimationVote.values()){
            estimatedTime += timeVote;
        }
        estimatedTime = estimatedTime /timeEstimationVote.size();
    }

    public void removeDoer(String id){
        this.doers.remove(id);
    }

    public void setPriority(Integer priority){
        this.priority = priority;
    }

    public List<String> getDoers(){
        if(doers == null) {
            this.doers = new ArrayList<>();
        }
        return doers;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getGroup() {
        return group;
    }

    public Status getStatus() {
        return status;
    }

    public Status getStatusBeforeArchive() {
        return statusBeforeArchive;
    }

    public Date getDate() {
        return date;
    }

    public Date getDateForSort() {
        if(date != null)
            return date;
        return new Date(0);
    }

    public Integer getPriority() {
        return priority;
    }

    public Date getPlayDate() {
        return playDate;
    }

    public Map<String, Long> getTimeSpentByDate() {
        return timeSpentByDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setStatus(Status status) {
        this.status = status;
        if(status == UserTask.Status.DONE)
            setCompletionDate(Calendar.getInstance().getTime());
        if(status == Status.TODO)
            stop();
        playDate = null;
    }

    public void setDate(Date date){
        this.date = date;
    }

    public String dateString(Boolean deadline){
        if((deadline && date == null) || (!deadline && completionDate == null)){
            return "Unknown";
        }

        Calendar calendar = Calendar.getInstance();
        if(deadline)
            calendar.setTime(date);
        else
            calendar.setTime(completionDate);

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        return dateFormat.format(calendar.getTime());
    }

    public Long getTimeSpent() {
        return timeSpent;
    }

    public String timeSpentString() {
        if(timeSpent == null){
            timeSpent = (long)0.0;
        }
        if(playDate != null) {
            playDate = Calendar.getInstance().getTime();
            timeSpent += (Calendar.getInstance().getTimeInMillis() - playDate.getTime()) / 1000 / 60;
        }
        return (int)Math.floor(timeSpent/60) + " h " + timeSpent%60 + " min";
    }

    public Map<String, Boolean> getSubtasks() {
        return subtasks;
    }

    public void play(){
        playDate = Calendar.getInstance().getTime();
    }

    public void stop(){
        if(playDate == null){
            return;
        }
        long newTime = (Calendar.getInstance().getTimeInMillis() - playDate.getTime())/1000/60;
        timeSpent += newTime;
        String date = simpleDateFormat.format(Calendar.getInstance().getTime());
        if(timeSpentByDate.containsKey(date)){
            long timeSpentTotal = timeSpentByDate.get(date) + newTime;
            timeSpentByDate.put(date, timeSpentTotal);
        }else{
            timeSpentByDate.put(date, newTime);
        }
        playDate = null;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() != UserTask.class)
            return false;

        if(id.equals(((UserTask)obj).id))
            if(title.equals(((UserTask)obj).title))
                if (description.equals(((UserTask)obj).description))
                    return (group.equals(((UserTask)obj).group));

        return false;
    }
}
