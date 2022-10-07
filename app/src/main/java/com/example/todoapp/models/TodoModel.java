package com.example.todoapp.models;

import android.widget.Toast;

public class TodoModel {
    private String id;
    private String title;
    private String description;

    public TodoModel(String id,String title,String description){
        this.id=id;
        this.title=title;
        this.description=description;

    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
