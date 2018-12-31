package com.budziaszek.tabmate.data;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String id;
    private String name;
    private String email;
    private List<String> invitations;

    public User() {
        invitations = new ArrayList<>();
    }

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.invitations = new ArrayList<>();
    }

    public List<String> getInvitations() {
        return invitations;
    }

    public void setInvitations(List<String> invitations) {
        this.invitations = invitations;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
