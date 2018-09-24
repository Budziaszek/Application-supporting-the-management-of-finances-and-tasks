package com.budziaszek.tabmate.firestoreData;

import com.budziaszek.tabmate.R;

import java.util.ArrayList;

public class UserTask {

    private String title;
    private String description;
    private String group;
    private ArrayList<String> tag;
    private Status status;
    //private String[] requirements;
    //time needed

    public enum Status {

        TODO(0, "ToDo", R.color.item_todo),
        DOING(1, "Doing", R.color.item_doing),
        DONE(2, "Done", R.color.item_done),
        UNKNOWN(3, "Unknown", R.color.item_todo);

        public int status;
        public String name;
        public int color;

        Status(int status, String name, int color) {
            this.status = status;
            this.name = name;
            this.color = color;
        }
    }

    public UserTask(){
        this.title = null;
        this.description = null;
        this.group = null;
        this.tag = null;
        this.status = Status.UNKNOWN;
        //this.requirements = null;
    }

    public UserTask(String title, String description, String group) {
        this.title = title;
        this.description = description;
        this.group = group;
        this.status = Status.TODO;
    }

    public void addTag(String tag){
        this.tag.add(tag);
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
}
