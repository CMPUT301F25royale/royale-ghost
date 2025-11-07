package com.example.project_part_3.Users;

import com.example.project_part_3.Events.Event;

import java.util.ArrayList;
import java.util.List;

public class Entrant extends User {
    List<Event> eventsAppliedFor;

    public Entrant(String name, String password, String email, String phone) {
        super(name, password, email, phone);
        eventsAppliedFor = new ArrayList<>();
    }

    public Entrant(String name, String password, String email) {
        super(name, password, email);
        eventsAppliedFor = new ArrayList<>();
    }

    public String getUserType(){
        return "Entrant";
    }
}
