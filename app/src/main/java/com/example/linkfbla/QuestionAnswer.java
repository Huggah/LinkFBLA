package com.example.linkfbla;

public class QuestionAnswer {
    public String author;
    public String content;
    public QuestionAnswer reply; // null unless has answer. Replies should not have another reply.

    // Default contructor for firebase
    public QuestionAnswer() { }

    public QuestionAnswer(String author, String content) {
        this.author = author;
        this.content = content;
    }
}
