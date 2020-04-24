package com.example.linkfbla;

import java.util.ArrayList;
import java.util.HashMap;

public class Chapter {
    static final String DATABASE_PATH = "Chapters/";

    public HashMap<String, String> positions = new HashMap<>();
    public HashMap<String, UserProfile> members = new HashMap<>();
    public HashMap<String, String> adminPermissions = new HashMap<>();
    public ArrayList<ChapterPost> posts = new ArrayList<>();
    public HashMap<String, ArrayList<String>> upcomingEvents = new HashMap<>();
    public HashMap<String, CompetitiveEvent> competitiveEvents = new HashMap<>();
    public ArrayList<QuestionAnswer> questions = new ArrayList<>();

    // Default constructor explicitly stated, for firebase
    public Chapter() { }
}