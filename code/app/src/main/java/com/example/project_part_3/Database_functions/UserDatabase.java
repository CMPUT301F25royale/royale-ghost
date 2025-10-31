package com.example.project_part_3.Database_functions;

import android.widget.Toast;

import com.example.project_part_3.Users.Admin;
import com.example.project_part_3.Users.Entrant;
import com.example.project_part_3.Users.Organizer;
import com.example.project_part_3.Users.User;

import java.util.ArrayList;

public class UserDatabase {

    //singleton pattern remove later for Firebase db
    private static UserDatabase instance;
    private ArrayList<User> database;

    private UserDatabase(){
        database = new ArrayList<>();
        database.add(new Entrant("john_doe", "test", "john@gmail.com", "7"));
        database.add(new Organizer("jack_doe", "test", "jack@gmail.com", "8"));
        database.add(new Admin("jane_doe", "test", "jane@gmail.com", "9"));
    }

    public static synchronized UserDatabase getInstance() {
        if (instance == null) {
            instance = new UserDatabase();
        }
        return instance;
    }
    //singleton pattern remove later for Firebase db

    // change to accomodate firebase
    public Boolean addUser(String name, String password, String email, String phone, String usertype){ // add user to database change later for firebase integration
        if (userExists(email)){ // return false if user already their else adds them to the database
            return false;
        }
        switch (usertype) {
            case "Admin":
                User admin = new Admin(name, password, email, phone);
                database.add(admin);
                return true;
            case "Organizer":
                User organizer= new Organizer(name, password, email, phone);
                database.add(organizer);
                return true;
            case "Entrant":
                User entrant= new Entrant(name, password, email, phone);
                database.add(entrant);
                return true;
            default:
                return false;
        }
    }
    // change to accomodate firebase
    public boolean checkUser(String name, String password){
        for (User user : database) {
            if (user.getName().equals(name) && user.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }
    // change to accomodate firebase
    public boolean userExists(String email) {// return True if user already their
        for (User user : database) {
            if (user.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }
    public User getUser(String name, String password){
        for (User user : database) {
            if (user.getName().equals(name) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    };
}
