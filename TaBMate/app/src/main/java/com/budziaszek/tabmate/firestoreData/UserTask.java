package com.budziaszek.tabmate.firestoreData;

import android.annotation.SuppressLint;

import com.budziaszek.tabmate.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class UserTask {

    private String id;
    private String title;
    private String description;
    private String group;
    private ArrayList<String> tag;
    private ArrayList<String> doers;
    private Status status;
    private Date date;
    //private String[] requirements;
    //time needed

    public enum Status {

        TODO(0, "ToDo", R.drawable.ripple_effect_todo),
        DOING(1, "Doing",  R.drawable.ripple_effect_doing),
        DONE(2, "Done",  R.drawable.ripple_effect_done),
        UNKNOWN(3, "Unknown",  R.drawable.ripple_effect_todo);

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
        if(status== Status.DONE)
            return Status.TODO;
        else
            return Status.UNKNOWN;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getId(){
        return id;
    }

    public UserTask(){
        this.title = null;
        this.description = null;
        this.group = null;
        this.tag = null;
        this.doers = new ArrayList<>();
        this.status = Status.UNKNOWN;
        //this.requirements = null;
    }

    public UserTask(String title, String description, String group) {
        this.title = title;
        this.description = description;
        this.group = group;
        this.status = Status.TODO;
    }

    public UserTask(String id, String title, String description, String group, ArrayList<String> tag, ArrayList<String> doers, Status status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.group = group;
        this.tag = tag;
        this.doers = doers;
        this.status = status;
    }

    /*public void addTag(String tag){
        this.tag.add(tag);
    }*/

    public void addDoer(String id){
        if(!this.doers.contains(id))
            this.doers.add(id);
    }

    public void removeDoer(String id){
        this.doers.remove(id);
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

    public ArrayList<String> getTag() {
        return tag;
    }

    public Status getStatus() {
        return status;
    }

    public Date getDate() {
        return date;
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

    public void setTag(ArrayList<String> tag) {
        this.tag = tag;
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
}
