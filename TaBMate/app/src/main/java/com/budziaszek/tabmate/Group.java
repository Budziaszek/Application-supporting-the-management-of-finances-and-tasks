package com.budziaszek.tabmate;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private String id;
    private String name;
    private String description;
    private List<String> members;

    public Group() {}

    public Group(String id,  String name, String description) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.members = new ArrayList<>();
    }

    public Group(String id,  String name, String description, List<String> members) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.members = members;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public void addMember(String memberId){
        members.add(memberId);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public List<String> getMembers() {
        return members;
    }
}
