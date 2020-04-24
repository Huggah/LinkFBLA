package com.example.linkfbla;

import java.util.ArrayList;

/**
 * Easy access to other's user info
 */
public class UserProfile {
    public String name;
    public String email;
    public String photoUrl;
    public ArrayList<String> competitiveEvents = new ArrayList<>();

    // Default constructor for firebase
    public UserProfile() {}

    public UserProfile(String name, String email, String photoUrl) {
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
    }
}