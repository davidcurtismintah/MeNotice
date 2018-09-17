package com.technativ.menotice.main.models;

public class Notice {

    private int notice_id;
    private String title;
    private String publisher;
    private String date_published;
    private String image;
    private String location;
    private String date;
    private String contact;
    private String email;
    private String about;

    public Notice() {
    }

    public Notice(int notice_id, String title, String publisher) {
        this.notice_id = notice_id;
        this.title = title;
        this.publisher = publisher;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDatePublished() {
        return date_published;
    }

    public void setDatePublished(String datePublished) {
        this.date_published = datePublished;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getNoticeId() {
        return notice_id;
    }

    public void setNoticeId(int notice_id) {
        this.notice_id = notice_id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}
