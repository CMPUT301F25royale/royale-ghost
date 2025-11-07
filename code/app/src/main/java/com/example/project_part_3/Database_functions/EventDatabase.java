package com.example.project_part_3.Database_functions;

import android.graphics.Bitmap;
import android.media.Image;

import com.example.project_part_3.Events.Event;
import com.example.project_part_3.Events.Event_Organizer;
import com.example.project_part_3.Users.Organizer;
import com.example.project_part_3.Users.User;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;


/**
 * we might combine this database with UserDatabase and Image database for a central database
 * ultimately we need to have the Events as a weak key of the Organizer that way we can then ensure
 * that deleting a Organizer will delete all of their events. It must also be that
 */
public class EventDatabase {

    private static EventDatabase instance;
    private ArrayList<Event> database;

    private EventDatabase() {
        database = new ArrayList<>();
        Organizer sampleOrganizer = new Organizer("Campus Events Committee", "events@campus.edu", "123-456-7890", "Admin"); // dummy organizer will not appear in profiles as EventDatabase addUser should only be called in organizer class
        long now = System.currentTimeMillis();
        Date dateOpen = new Date(now);
        Date dateClose = new Date(now + 1000 * 60 * 60 * 24 * 7);
        Date futureDate = new Date(now + 1000 * 60 * 60 * 24 * 14);
        Timestamp eventTime1 = new Timestamp(futureDate.getTime());
        Timestamp eventTime2 = new Timestamp(futureDate.getTime() + 1000 * 60 * 60 * 24);
        addEvent("Spring Fling Festival", "Annual campus spring festival with music, food, and games.", new ArrayList<>(), eventTime1, dateOpen, dateClose, sampleOrganizer, "Main Quad", 500, null);
        addEvent("Tech Career Fair", "Meet with top tech companies looking to hire interns and graduates.", new ArrayList<>(), eventTime2, dateOpen, dateClose, sampleOrganizer, 0F, "Engineering Hall", 300, null);
        addEvent("Art Exhibit Opening", "Showcasing student artwork from the past semester.", new ArrayList<>(), new Timestamp(now + 1000 * 60 * 60 * 24 * 20), dateOpen, dateClose, sampleOrganizer, "Fine Arts Gallery", 150, null);
        addEvent("Outdoor Movie Night: The Avengers", "Free outdoor screening of the classic Marvel movie. Bring a blanket!", new ArrayList<>(), new Timestamp(now + 1000 * 60 * 60 * 24 * 5), dateOpen, new Date(now + 1000 * 60 * 60 * 24 * 4), sampleOrganizer, "Lawn by the Lake", 1000, null);
        addEvent("Charity 5K Run", "A fun run to raise money for local charities. All fitness levels welcome.", new ArrayList<>(), new Timestamp(now + 1000 * 60 * 60 * 24 * 30), dateOpen, dateClose, sampleOrganizer, 25F, "Campus Recreation Center", 400, null);
    }

    public static synchronized EventDatabase getInstance() {
        if (instance == null) {
            instance = new EventDatabase();
        }
        return instance;
    }

    public Boolean addEvent(String title, String description, ArrayList<User> attendant, Timestamp time, Date date_open, Date date_close, Organizer organizer, String location, Integer capacity, Bitmap poster) {
        Event newEvent = new Event(title, description, attendant,  time, date_open, date_close, organizer, location, capacity, poster);
        if (eventExists(newEvent.getTitle(), newEvent.getOrganizer())) {
            return false;
        }
        database.add(newEvent);
        return true;
    }

    public Boolean addEvent(String title, String description, ArrayList<User> attendant, Timestamp time, Date date_open, Date date_close, Organizer organizer, Float price, String location, Integer capacity, Bitmap poster) {
        Event newEvent = new Event(title, description, attendant, time, date_open, date_close, organizer, price, location, capacity, poster);
        if (eventExists(newEvent.getTitle(), newEvent.getOrganizer())) {
            return false;
        }
        database.add(newEvent);
        return true;
    }

    public boolean eventExists(String title, Organizer organizer) {
        for (Event e : database) {
            if (e.getTitle().equalsIgnoreCase(title) && Objects.equals(e.getOrganizer().getName(), organizer.getName())) {
                return true;
            }
        }
        return false;
    }

    public Event getEvent(String title, Organizer organizer) {
        for (Event event : database) {
            if (event.getTitle().equalsIgnoreCase(title) && Objects.equals(event.getOrganizer().getName(), organizer.getName())) {
                return event;
            }
        }
        return null;
    }
    public Event getEvent(String title, String organizerName) {
        for (Event event : database) {
            if (event.getTitle().equalsIgnoreCase(title)
                    && event.getOrganizer() != null
                    && organizerName.equals(event.getOrganizer().getName())) {
                return event;
            }
        }
        return null;
    }

    public boolean removeEvent(String title, String organizerName) {
        return database.removeIf(event -> event.getTitle().equalsIgnoreCase(title) && event.getOrganizer().getName().equals(organizerName));
    }

    public ArrayList<Event> getAllEvents() {
        return new ArrayList<>(database);
    }
}

