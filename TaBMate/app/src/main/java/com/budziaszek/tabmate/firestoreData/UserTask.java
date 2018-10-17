package com.budziaszek.tabmate.firestoreData;

import android.annotation.SuppressLint;

import com.budziaszek.tabmate.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private Map<String, Integer> timeEstimationVote;
    private Map<String, Integer> readinessVote;
    private Integer estimatedTime;

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

    public void setReadinessVote(Map<String, Integer> readinessVote) {
        this.readinessVote = readinessVote;
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
        this.readinessVote = new TreeMap<>();
        this.estimatedTime = 0;
    }

    public UserTask(String title, String description, String group) {
        this.title = title;
        this.description = description;
        this.group = group;
        this.status = Status.TODO;
        this.doers = new ArrayList<>();
        this.timeEstimationVote = new TreeMap<>();
        this.readinessVote = new TreeMap<>();
        this.estimatedTime = 0;
    }

    public UserTask(String id, String title, String description, String group, /*ArrayList<String> tag,*/ ArrayList<String> doers, Status status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.group = group;
        this.doers = new ArrayList<>();
        this.timeEstimationVote = new TreeMap<>();
        this.readinessVote = new TreeMap<>();
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

    public void addReadinessVote(String uid, Integer readiness){
        readinessVote.put(uid, readiness);
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

    public Map<String, Integer> getReadinessVote() {
        return readinessVote;
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
    }

    public void setDate(Date date){
        this.date = date;
    }

    public String getDateString(){
        if(date == null){
            return "Unknown";
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        return dateFormat.format(calendar.getTime());
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
