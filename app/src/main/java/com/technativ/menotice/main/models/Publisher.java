package com.technativ.menotice.main.models;

import java.util.List;

public class Publisher {

    private int publisher_id;
    private String name;
    private String logo;
    private String last_publish_time;
    private String about;

    public Publisher() {
    }

    public Publisher(int publisher_id, String name, String logo) {
        this.publisher_id = publisher_id;
        this.name = name;
        this.logo = logo;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return last_publish_time;
    }

    public void setTime(String time) {
        this.last_publish_time = time;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public int getPublisherId() {
        return publisher_id;
    }

    public void setPublisherId(int organisation_id) {
        this.publisher_id = organisation_id;
    }
}
