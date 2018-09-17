package com.technativ.menotice.main.models;

import java.util.ArrayList;

public class Category {

    private int category_id;
    private String name;
    private ArrayList<Notice> notices;

    public Category() {

    }

    public Category(int category_id, String name, ArrayList<Notice> notices) {
        this.category_id = category_id;
        this.name = name;
        this.notices = notices;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Notice> getNotices() {
        return notices;
    }

    public void setNotices(ArrayList<Notice> notices) {
        this.notices = notices;
    }

    public int getCategoryId() {
        return category_id;
    }

    public void setCategoryId(int categoryId) {
        this.category_id = category_id;
    }
}
