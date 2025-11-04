package com.example.project_part_3.Database_functions;

import com.example.project_part_3.Events.Event;
import com.example.project_part_3.Users.User;

import java.util.ArrayList;

public class EventDatabase {

    private static EventDatabase instance;
    private ArrayList<Event> database;

    private static synchronized EventDatabase getInstance() {
        if (instance == null) {
            instance = new EventDatabase();
        }
        return instance;
    }
    private EventDatabase(){
        database = new ArrayList<>();
    }






}
