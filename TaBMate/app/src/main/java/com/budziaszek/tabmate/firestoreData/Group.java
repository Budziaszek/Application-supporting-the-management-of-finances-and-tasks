package com.budziaszek.tabmate.firestoreData;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private String id;
    private String name;
    private String description;
    private List<String> members;
    private Double budgetBalance;
    private String currency;

    public Group() {
    }

    public Group(String name, String description) {
        this.name = name;
        this.description = description;
        this.members = new ArrayList<>();
    }

    public Group(String name, String description, List<String> members) {
        this.name = name;
        this.description = description;
        this.members = members;
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

    public void setBudgetBalance(Double budgetBalance) {
        this.budgetBalance = budgetBalance;
    }

    public void addMember(String memberId) {
        members.add(memberId);
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

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

    public Double getBudgetBalance() {
        return budgetBalance;
    }

    public String getStringBudgetBalance() {
        String currency;
        if(getCurrency() == null){
            currency = "";
        }else{
            currency = getCurrency();
        }
        if(budgetBalance == null)
            return 0.0 + " " + currency;
        else
            return budgetBalance + " " + currency;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != Group.class)
            return false;

        if (id.equals(((Group) obj).id))
            if (name.equals(((Group) obj).name))
                return (description.equals(((Group) obj).description));

        return false;
    }
}
