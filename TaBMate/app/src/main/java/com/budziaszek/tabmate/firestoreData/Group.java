package com.budziaszek.tabmate.firestoreData;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private String id;
    private String name;
    private String description;
    private List<String> members;
    //private List<UserTask> tasks = new ArrayList<>();
    //private List<String> invitationsSent;

    public Group() {
    }

    public Group(String name, String description) {
        this.name = name;
        this.description = description;
        this.members = new ArrayList<>();
        //this.invitationsSent = new ArrayList<>();
    }

    public Group(String name, String description, List<String> members) {//, List<String> invitationsSent) {
        this.name = name;
        this.description = description;
        this.members = members;
        //this.invitationsSent = invitationsSent;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public void addMember(String memberId) {
        members.add(memberId);
    }

    //public List<UserTask> getTasks() {
    //    return tasks;
    //}

    /*public void setInvitationsSent(List<String> invitationsSent) {
        this.invitationsSent = invitationsSent;
    }*/

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getMembers() {
        return members;
    }

    //public void setTasks(List<UserTask> tasks) {
    //    this.tasks = tasks;
    //}

    /*public List<String> getInvitationsSent() {
        return invitationsSent;
    }*/

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() != Group.class)
            return false;

        if(id.equals(((Group)obj).id))
            if(name.equals(((Group)obj).name))
               return (description.equals(((Group)obj).description));

        return false;
    }

}
