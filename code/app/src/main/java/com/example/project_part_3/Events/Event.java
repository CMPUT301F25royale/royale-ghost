package com.example.project_part_3.Events;

import android.graphics.Bitmap;
import android.media.Image;

import com.example.project_part_3.Users.Organizer;
import com.example.project_part_3.Users.User;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class Event {
    public String title;
    public String description;
    public Date date_open;
    public Date date_close;
    public Organizer organizer;
    public Integer price = 0;
    public String location;
    public Integer capacity;
    public Bitmap poster;
    public Timestamp time;
    public ArrayList<User> attendant_list;
    public Integer attendees;



    public Event(String title, String description, ArrayList<User> attendees, Timestamp time , Date date_open, Date date_close, Organizer organizer, Integer price, String location, Integer capacity, Bitmap poster){
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
    public Integer getPrice(){
        return price;
    }
    public String getLocation(){
        return location;
    }
    public Integer getCapacity(){
        return capacity;
    }
    public Bitmap getPoster(){
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
    public void EditPrice(Integer price){
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
