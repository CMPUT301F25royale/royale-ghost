package com.example.project_part_3.Events;

import android.graphics.Bitmap;

import com.example.project_part_3.Users.Organizer;
import com.example.project_part_3.Users.User;
import com.google.firebase.firestore.Exclude;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class Event {
    private String id; // required for firebase, created when added to database
    private String title;
    private String description;
    private Date date_open;
    private Date date_close;
    private Organizer organizer;
    private Float price;
    private String location;
    private Integer capacity;
    private Bitmap poster;
    private Timestamp time;
    private ArrayList<User> attendant_list;
    private Integer attendees;

    public void addAttendant(User user){
        attendant_list.add(user);
        attendees++;
    }

    public void removeAttendant(User user){
        attendant_list.remove(user);
        attendees--;
    }

    public Event(){
        // required for firebase
        this.attendant_list = new ArrayList<User>();
        this.attendees = 0;
    }

    public Event(String title, String description, ArrayList<User> attendees, Timestamp time , Date date_open, Date date_close, Organizer organizer, Float price, String location, Integer capacity, Bitmap poster){
        this.time = time;
        this.price = price;
        this.title = title;
        this.description = description;
        this.date_open = date_open;
        this.date_close = date_close;
        this.organizer = organizer;
        this.location = location;
        this.capacity = capacity;
        this.poster = poster;
        this.attendant_list = attendees; // Attendees should be held within events so that the same user can' be in the same event for hard enforcement
        this.attendees = attendees.size();// current people signed up for events as in number of people who signed up this gets updatded frequently
    }

    public Event(String title, String description, ArrayList<User> attendees, Timestamp time, Date date_open, Date date_close, Organizer organizer, String location, Integer capacity, Bitmap poster){
        this.time = time;
        this.title = title;
        this.description = description;
        this.date_open = date_open;
        this.date_close = date_close;
        this.organizer = organizer;
        this.location = location;
        this.capacity = capacity;
        this.poster = poster;
        this.attendant_list = attendees;
        this.attendees = attendees.size();
    }

    public Event(String title, String description, Timestamp time, Date date_open, Date date_close, Organizer organizer, Float price, String location, Integer capacity, Bitmap poster){
        this.time = time;
        this.price = price;
        this.title = title;
        this.description = description;
        this.date_open = date_open;
        this.date_close = date_close;
        this.organizer = organizer;
        this.location = location;
        this.capacity = capacity;
        this.poster = poster;
        this.attendant_list = new ArrayList<User>();
        this.attendees = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle(){
        return title;
    }
    public String getDescription(){
        return description;
    }
    public Date getDate_open(){
        return date_open;
    }
    public Date getDate_close(){
        return date_close;
    }
    public Organizer getOrganizer(){
        return organizer;
    }
    public Float getPrice(){
        return price;
    }
    public String getLocation(){
        return location;
    }
    public Integer getCapacity(){
        return capacity;
    }
    @Exclude
    public Bitmap getPoster(){
        // too large to be stored in Firebase object, we will store it in firebase storage instead
        return poster;
    }
    public Timestamp getTime(){
        return time;
    }
    public ArrayList<User> getAttendant_list(){ return attendant_list;}

    public Integer getAttendees(){return attendees;}

    public void EditTitle(String title){
        this.title = title;
    }
    public void EditDescription(String description){
        this.description = description;
    }
    public void EditDate_open(Date date_open){
        this.date_open = date_open;
    }
    public void EditDate_close(Date date_close){
        this.date_close = date_close;
    }
    public void EditPrice(Float price){
        this.price = price;
    }
    public void EditLocation(String location){
        this.location = location;
    }
    public void EditCapacity(Integer capacity){
        this.capacity = capacity;
    }
    public void EditPoster(Bitmap poster){
        this.poster = poster;
    }


}
