package com.example.project_part_3.Users;

import java.util.ArrayList;
import java.util.List;

public class Entrant extends User {
     private List<String> eventsAppliedFor; // the IDs of events the user has applied for
     private ArrayList<String> interests;// the interests of an entrant
     List<String> eventsAppliedFor;

    public Entrant() {
        super();
        this.eventsAppliedFor = new ArrayList<>();
        this.interests = new ArrayList<>();
    }

    public Entrant(String name, String password, String email, String phone, ArrayList<String> interests) {
        super(name, password, email, phone);
        this.eventsAppliedFor = new ArrayList<>();
        this.interests = (interests != null) ? interests : new ArrayList<>();
    }

    public Entrant(String name, String password, String email, ArrayList<String> interests) {
        super(name, password, email);
        this.eventsAppliedFor = new ArrayList<>();
        this.interests = (interests != null) ? interests : new ArrayList<>();
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

    public ArrayList<String> getInterests() {
        return interests;
    }

    public void setInterests(ArrayList<String> interests) {
        this.interests = interests;
    }

    public void addInterest(String newInterest){interests.add(newInterest);}

    public void removeInterest(String oldInterest){interests.remove(oldInterest);}

    public String getUserType(){
        return "Entrant";
    }
}
