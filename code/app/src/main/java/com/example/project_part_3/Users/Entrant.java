package com.example.project_part_3.Users;

import java.util.ArrayList;
import java.util.List;

public class Entrant extends User {
    List<String> eventsAppliedFor; // the IDs of events the user has applied for

    public Entrant() {
        super();
        eventsAppliedFor = new ArrayList<>();
    }

    public Entrant(String name, String password, String email, String phone) {
        super(name, password, email, phone);
        eventsAppliedFor = new ArrayList<>();
    }

    public Entrant(String name, String password, String email) {
        super(name, password, email);
        eventsAppliedFor = new ArrayList<>();
    }
    public List<String> getEventsAppliedFor() {
        return eventsAppliedFor;
    }

    public void applyForEvent(String eventID) {
        eventsAppliedFor.add(eventID);
    }

    public void removeEvent(String eventID) {
        eventsAppliedFor.remove(eventID);
    }

    public String getUserType(){
        return "Entrant";
    }
}
