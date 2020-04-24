package com.example.linkfbla;

import java.util.ArrayList;
import java.util.Date;

public class ChapterPost {
    public String author;
    public String content;
    public String photoUrl;
    public Date timestamp;
    public Meeting meeting; // can be null

    public ChapterPost(String author, String content, String photoUrl, Date timestamp) {
        this.author = author;
        this.content = content;
        this.photoUrl = photoUrl;
        this.timestamp = timestamp;
    }

    // Default constructor, for firebase
    public ChapterPost() { }
}

class Meeting {
    public ArrayList<String> attendees = new ArrayList<>();
    public int day, month, year;

    // Default constructor, for firebase
    public Meeting() { }

    public Meeting(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }
}