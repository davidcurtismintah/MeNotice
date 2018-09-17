package com.technativ.menotice.main.models;

import java.util.List;

public class PublisherData {

    private String publisher_id;
    private String about;
    private List<Notice> active_notices;

    public String getPublisheId() {
        return publisher_id;
    }

    public void setPublisherId(String publisher_id) {
        this.publisher_id = publisher_id;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public List<Notice> getActiveNotices() {
        return active_notices;
    }

    public void setActiveNotices(List<Notice> activeNotices) {
        this.active_notices = activeNotices;
    }

}
