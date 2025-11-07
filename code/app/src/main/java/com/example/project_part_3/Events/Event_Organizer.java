package com.example.project_part_3.Events;

import java.util.Date;

public class Event_Organizer {
    private String name;
    private String location;
    private String registration_status;
    private Date date;
    private String capacity;

    public Event_Organizer(String name, String location, String registration_status, Date date, String capacity) {
        this.name = name;
        this.location = location;
        this.registration_status = registration_status;
        this.date = date;
        this.capacity = capacity;
    }
    public String getName() {
        return name;
    }
    public String getLocation() { return location;}
    public String getRegStatus() { return registration_status;}
    public Date getDate() { return date;}
    public String getCapacity() { return capacity;}

}
