package com.example.linkfbla;

import java.util.ArrayList;

// Class for events list in database and storing team info
public class CompetitiveEvent {

    public String name;
    public boolean team;
    public boolean approved;
    public ArrayList<String> teamMembers = new ArrayList<>();

    // Default constructor, for firebase
    public CompetitiveEvent() { }

    public CompetitiveEvent(String name, boolean team) {
        this.name = name;
        this.team = team;
    }
}