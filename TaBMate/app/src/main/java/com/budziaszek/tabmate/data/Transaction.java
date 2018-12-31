package com.budziaszek.tabmate.data;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
//TODO use or remove uid
public class Transaction {

    private String id;
    private Double amount;
    private String title;
    private String description;
    private Date date;
    private String category;
    private String subcategory;
    private String uid;
    private String group;
    private Double amountBeforeTransaction;

    public Transaction() {
    }

    public Transaction(Double amount, String title, String description, Date date, String uid) {
        this.amount = amount;
        this.title = title;
        this.description = description;
        this.date = date;
        this.uid = uid;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setAmountBeforeTransaction(Double amountBeforeTransaction) {
        this.amountBeforeTransaction = amountBeforeTransaction;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getId() {
        return id;
    }

    public Double getAmount() {
        return amount;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate() {
        return date;
    }

    public String getUid() {
        return uid;
    }

    public String getGroup() {
        return group;
    }

    public Double getAmountBeforeTransaction() {
        return amountBeforeTransaction;
    }

    public String getCategory() {
        return category;
    }

    public String getSubcategory() {
        return subcategory;
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
