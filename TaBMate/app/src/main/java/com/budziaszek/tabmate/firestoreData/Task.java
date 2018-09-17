package com.budziaszek.tabmate.firestoreData;

import java.util.ArrayList;

public class Task {

    private String title;
    private String description;
    private ArrayList<String> tag;
    private Status status;
    //private String[] requirements;
    //time needed

    public enum Status {

        TODO(0),
        DOING(1),
        DONE(2),
        UNKNOWN(3);

        int status;

        Status(int status) {
            this.status = status;
        }
    }

    public Task(){
        this.title = null;
        this.description = null;
        this.tag = null;
        this.status = Status.UNKNOWN;
        //this.requirements = null;
    }

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
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

    public void setTag(ArrayList<String> tag) {
        this.tag = tag;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
